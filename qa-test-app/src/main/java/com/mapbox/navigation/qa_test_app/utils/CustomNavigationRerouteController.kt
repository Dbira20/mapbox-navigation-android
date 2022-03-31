package com.mapbox.navigation.qa_test_app.utils

import com.mapbox.navigation.base.route.toDirectionsRoutes
import com.mapbox.navigation.core.reroute.NavigationRerouteController
import com.mapbox.navigation.core.reroute.RerouteController
import com.mapbox.navigation.core.reroute.RerouteState

class CustomNavigationRerouteController : NavigationRerouteController {

    override fun reroute(callback: NavigationRerouteController.RoutesCallback) {
        
    }

    override fun reroute(routesCallback: RerouteController.RoutesCallback) {
        reroute { routes, _ ->
            routesCallback.onNewRoutes(routes.toDirectionsRoutes())
        }
    }

    override val state: RerouteState = RerouteState.Idle

    override fun interrupt() = Unit

    override fun registerRerouteStateObserver(
        rerouteStateObserver: RerouteController.RerouteStateObserver
    ): Boolean = false

    override fun unregisterRerouteStateObserver(
        rerouteStateObserver: RerouteController.RerouteStateObserver
    ): Boolean  = false
}
