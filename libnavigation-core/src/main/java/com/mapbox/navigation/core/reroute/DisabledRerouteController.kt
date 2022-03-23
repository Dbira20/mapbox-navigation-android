package com.mapbox.navigation.core.reroute

/**
 *
 */
internal class DisabledRerouteController: RerouteController {
    override val state: RerouteState = RerouteState.Idle

    override fun reroute(routesCallback: RerouteController.RoutesCallback) = Unit

    override fun interrupt()  = Unit

    override fun registerRerouteStateObserver(
        rerouteStateObserver: RerouteController.RerouteStateObserver
    ): Boolean = false

    override fun unregisterRerouteStateObserver(
        rerouteStateObserver: RerouteController.RerouteStateObserver
    ): Boolean = false
}
