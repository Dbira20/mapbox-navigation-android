package com.mapbox.navigation.core.routealternatives

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag
import com.mapbox.navigation.base.internal.utils.parseNativeDirectionsAlternative
import com.mapbox.navigation.base.route.RouteAlternativesOptions
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.trip.session.TripSession
import com.mapbox.navigation.navigator.internal.MapboxNativeNavigator
import com.mapbox.navigation.navigator.internal.mapToSdkRouteOrigin
import com.mapbox.navigation.utils.internal.ThreadController
import com.mapbox.navigation.utils.internal.logE
import com.mapbox.navigation.utils.internal.logI
import com.mapbox.navigator.RouteAlternative
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit

internal class RouteAlternativesController constructor(
    private val options: RouteAlternativesOptions,
    private val navigator: MapboxNativeNavigator,
    private val tripSession: TripSession,
    private val threadController: ThreadController
) {

    private var lastUpdateOrigin: RouterOrigin = RouterOrigin.Onboard

    private val mainJobControl by lazy { threadController.getMainScopeAndRootJob() }

    private val nativeRouteAlternativesController = navigator.createRouteAlternativesController()
        .apply {
            setRouteAlternativesOptions(
                com.mapbox.navigator.RouteAlternativesOptions(
                    TimeUnit.MILLISECONDS.toSeconds(options.intervalMillis).toDouble(),
                    options.avoidManeuverSeconds.toDouble()
                )
            )
            enableOnEmptyAlternativesRequest(true)
        }

    private val observers = CopyOnWriteArraySet<RouteAlternativesObserver>()

    fun register(routeAlternativesObserver: RouteAlternativesObserver) {
        val isStopped = observers.isEmpty()
        observers.add(routeAlternativesObserver)
        if (isStopped) {
            nativeRouteAlternativesController.addObserver(nativeObserver)
        }
    }

    fun unregister(routeAlternativesObserver: RouteAlternativesObserver) {
        observers.remove(routeAlternativesObserver)
        if (observers.isEmpty()) {
            nativeRouteAlternativesController.removeObserver(nativeObserver)
        }
    }

    fun triggerAlternativeRequest(listener: RouteAlternativesRequestCallback?) {
        nativeRouteAlternativesController.refreshImmediately { expected ->
            val routeProgress = tripSession.getRouteProgress()
                ?: run {
                    listener?.onRouteAlternativesAborted(
                        """
                            |Route progress not available, ignoring alternatives update.
                            |Continuous alternatives are only available in active guidance.
                        """.trimMargin()
                    )
                    return@refreshImmediately
                }

            expected.fold(
                { error ->
                    listener?.onRouteAlternativesAborted(error)
                },
                { value ->
                    processRouteAlternatives(
                        routeProgress,
                        value
                    ) { progress, alternatives, origin ->
                        listener?.onRouteAlternativeRequestFinished(progress, alternatives, origin)
                    }
                }
            )
        }
    }

    fun unregisterAll() {
        nativeRouteAlternativesController.removeAllObservers()
        observers.clear()
    }

    private val nativeObserver = object : com.mapbox.navigator.RouteAlternativesObserver {
        override fun onRouteAlternativesChanged(
            routeAlternatives: List<RouteAlternative>,
            removed: List<RouteAlternative>
        ): List<Int> {
            val routeProgress = tripSession.getRouteProgress()
                ?: return emptyList()

            processRouteAlternatives(
                routeProgress,
                routeAlternatives
            ) { progress, alternatives, origin ->
                observers.forEach {
                    it.onRouteAlternatives(progress, alternatives, origin)
                }
            }

            // This is supposed to be able to filter alternatives
            // but at this point we're not filtering anything.
            return emptyList()
        }

        override fun onError(message: String) {
            logE(TAG, Message("Error: $message"))
        }
    }

    /**
     * @param block invoked with results (on the main thread)
     */
    private fun processRouteAlternatives(
        routeProgress: RouteProgress,
        nativeAlternatives: List<RouteAlternative>,
        block: (RouteProgress, List<DirectionsRoute>, RouterOrigin) -> Unit,
    ) {
        val alternatives: List<DirectionsRoute> = runBlocking {
            nativeAlternatives.mapIndexedNotNull { index, routeAlternative ->
                val expected = parseNativeDirectionsAlternative(
                    ThreadController.IODispatcher,
                    routeAlternative.route.response,
                    routeProgress.route.routeOptions()
                )
                if (expected.isValue) {
                    expected.value
                } else {
                    logE(
                        TAG,
                        Message(
                            """
                                    |unable to parse alternative at index $index;
                                    |failure for response: ${routeAlternative.route.response}
                                """.trimMargin()
                        ),
                        expected.error
                    )
                    null
                }
            }
        }
        logI(TAG, Message("${alternatives.size} alternatives available"))

        mainJobControl.scope.launch {
            val origin = nativeAlternatives.find {
                // looking for the first new route,
                // assuming all new routes come from the same request
                it.isNew
            }?.route?.routerOrigin?.mapToSdkRouteOrigin() ?: lastUpdateOrigin
            block(routeProgress, alternatives, origin)
            lastUpdateOrigin = origin
        }
    }

    private companion object {
        private val TAG = Tag("MbxRouteAlternativesController")
    }
}
