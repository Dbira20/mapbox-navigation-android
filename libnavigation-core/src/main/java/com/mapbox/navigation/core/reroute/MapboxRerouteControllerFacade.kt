package com.mapbox.navigation.core.reroute

import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.navigation.base.route.toDirectionsRoutes
import com.mapbox.navigation.base.route.toNavigationRoutes
import com.mapbox.navigation.core.directions.session.DirectionsSession
import com.mapbox.navigation.core.directions.session.RoutesExtra
import com.mapbox.navigation.navigator.internal.mapToSdkRouteOrigin
import com.mapbox.navigator.RerouteError
import com.mapbox.navigator.RerouteObserver
import com.mapbox.navigator.RouterOrigin
import java.util.concurrent.CopyOnWriteArraySet

internal class MapboxRerouteControllerFacade(
    private val directionsSession: DirectionsSession,
    internal val nativeRerouteController: NativeExtendedRerouteControllerInterface,
) : NavigationRerouteController {

    override var state: RerouteState = RerouteState.Idle
        set(value) {
            if (value == field) return
            field = value
            observers.forEach { it.onRerouteStateChanged(field) }
        }

    private val observers = CopyOnWriteArraySet<RerouteController.RerouteStateObserver>()

    private companion object {
        private const val TAG = "MapboxRerouteControllerFacade"
    }

    init {
        nativeRerouteController.addRerouteObserver(object : RerouteObserver {
            override fun onRerouteDetected(routeRequest: String) {
                state = RerouteState.FetchingRoute
            }

            override fun onRerouteReceived(
                routeResponse: String,
                routeRequest: String,
                origin: RouterOrigin
            ) {
                state = RerouteState.RouteFetched(origin.mapToSdkRouteOrigin())
                directionsSession.setRoutes(
                    DirectionsResponse.fromJson(routeResponse).routes().toNavigationRoutes(),
                    routesUpdateReason = RoutesExtra.ROUTES_UPDATE_REASON_REROUTE
                )
                state = RerouteState.Idle
            }

            override fun onRerouteCancelled() {
                state = RerouteState.Interrupted
                state = RerouteState.Idle
            }

            override fun onRerouteFailed(error: RerouteError) {
                state = RerouteState.Failed(error.message)
                state = RerouteState.Idle
            }

            override fun onSwitchToAlternative(
                alternativeId: Int,
                routeResponse: String,
                routeRequest: String,
                origin: RouterOrigin
            ) = Unit
        })
    }

    override fun reroute(callback: NavigationRerouteController.RoutesCallback) {
        if (state is RerouteState.FetchingRoute) {
            interrupt()
        }
        nativeRerouteController.setRerouteCallbackListener { expected ->
            nativeRerouteController.setRerouteCallbackListener(null)
            expected.fold({
                // do nothing
            }, { rerouteInfo ->
                val directionsResponse = DirectionsResponse.fromJson(rerouteInfo.routeResponse)
                val navRoutes = directionsResponse.routes().toNavigationRoutes()
                callback.onNewRoutes(navRoutes, rerouteInfo.origin.mapToSdkRouteOrigin())
            })
        }
        nativeRerouteController.forceReroute()
    }

    override fun reroute(routesCallback: RerouteController.RoutesCallback) {
        reroute { navRoutes, _ ->
            routesCallback.onNewRoutes(
                navRoutes.toDirectionsRoutes()
            )
        }
    }

    override fun interrupt() {
        nativeRerouteController.cancel()
    }

    override fun registerRerouteStateObserver(
        rerouteStateObserver: RerouteController.RerouteStateObserver
    ): Boolean {
        rerouteStateObserver.onRerouteStateChanged(state)
        return observers.add(rerouteStateObserver)
    }

    override fun unregisterRerouteStateObserver(
        rerouteStateObserver: RerouteController.RerouteStateObserver
    ): Boolean =
        observers.remove(rerouteStateObserver)

    fun setRerouteOptionsAdapter(rerouteOptionsAdapter: RerouteOptionsAdapter?) {
        nativeRerouteController.setRerouteOptionsAdapter(rerouteOptionsAdapter)
    }
}
