package com.mapbox.navigation.core.reroute

import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.navigation.base.route.toDirectionsRoutes
import com.mapbox.navigation.base.route.toNavigationRoutes
import com.mapbox.navigation.navigator.internal.MapboxNativeNavigator
import com.mapbox.navigation.navigator.internal.mapToSdkRouteOrigin
import com.mapbox.navigation.utils.internal.logE
import com.mapbox.navigator.RerouteError
import com.mapbox.navigator.RerouteObserver
import com.mapbox.navigator.RouterOrigin
import java.util.concurrent.CopyOnWriteArraySet

internal class MapboxRerouteControllerFacade(
    private val nativeRerouteController: NativeExtendedRerouteControllerInterface,
) : NavigationRerouteController, RerouteOptionsAdapterManager {

    override var state: RerouteState = RerouteState.Idle
        set(value) {
            if (value == field) return
            field = value
            observers.forEach { it.onRerouteStateChanged(field) }
        }

    private val observers = CopyOnWriteArraySet<RerouteController.RerouteStateObserver>()

    companion object {
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
        nativeRerouteController.forceReroute()

        nativeRerouteController.reroute("") { exptected ->
            exptected.fold({ error ->
                logE(TAG, "Reroutes error: $error")
            }, { value ->
                val directionsResponse = DirectionsResponse.fromJson(value.routeResponse)
                if (directionsResponse.routes().isNotEmpty()) {
                    callback.onNewRoutes(directionsResponse.routes().toNavigationRoutes())
                } else {
                    logE(TAG, "Routes list mustn't be null")
                }
            })
        }
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

    override fun setRerouteOptionsDelegate(rerouteOptionsAdapter: RerouteOptionsAdapter) {
        nativeRerouteController.setRerouteOptionsAdapter(rerouteOptionsAdapter)
    }
}
