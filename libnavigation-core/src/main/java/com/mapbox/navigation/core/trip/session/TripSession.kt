package com.mapbox.navigation.core.trip.session

import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.directions.session.RoutesExtra
import com.mapbox.navigation.core.trip.service.TripService
import com.mapbox.navigation.core.trip.session.eh.EHorizonObserver
import com.mapbox.navigator.FallbackVersionsObserver

internal interface TripSession : TripSessionLocationProvider {

    val tripService: TripService
    fun setRoutes(
        routes: List<NavigationRoute>,
        legIndex: Int,
        @RoutesExtra.RoutesUpdateReason reason: String
    )

    fun getRouteProgress(): RouteProgress?
    fun getState(): TripSessionState

    fun start(withTripService: Boolean, withReplayEnabled: Boolean = false)
    fun stop()
    fun isRunningWithForegroundService(): Boolean

    fun registerRouteProgressObserver(routeProgressObserver: RouteProgressObserver)
    fun unregisterRouteProgressObserver(routeProgressObserver: RouteProgressObserver)
    fun unregisterAllRouteProgressObservers()

    fun registerOffRouteObserver(offRouteObserver: OffRouteObserver)
    fun unregisterOffRouteObserver(offRouteObserver: OffRouteObserver)
    fun unregisterAllOffRouteObservers()

    fun registerStateObserver(stateObserver: TripSessionStateObserver)
    fun unregisterStateObserver(stateObserver: TripSessionStateObserver)
    fun unregisterAllStateObservers()

    fun registerBannerInstructionsObserver(bannerInstructionsObserver: BannerInstructionsObserver)
    fun unregisterBannerInstructionsObserver(bannerInstructionsObserver: BannerInstructionsObserver)
    fun unregisterAllBannerInstructionsObservers()

    fun registerVoiceInstructionsObserver(voiceInstructionsObserver: VoiceInstructionsObserver)
    fun unregisterVoiceInstructionsObserver(voiceInstructionsObserver: VoiceInstructionsObserver)
    fun unregisterAllVoiceInstructionsObservers()

    fun updateLegIndex(legIndex: Int, callback: LegIndexUpdatedCallback)

    fun registerRoadObjectsOnRouteObserver(
        roadObjectsOnRouteObserver: RoadObjectsOnRouteObserver
    )
    fun unregisterRoadObjectsOnRouteObserver(
        roadObjectsOnRouteObserver: RoadObjectsOnRouteObserver
    )
    fun unregisterAllRoadObjectsOnRouteObservers()

    fun registerEHorizonObserver(eHorizonObserver: EHorizonObserver)
    fun unregisterEHorizonObserver(eHorizonObserver: EHorizonObserver)
    fun unregisterAllEHorizonObservers()

    fun registerFallbackVersionsObserver(fallbackVersionsObserver: FallbackVersionsObserver)
    fun unregisterFallbackVersionsObserver(fallbackVersionsObserver: FallbackVersionsObserver)
    fun unregisterAllFallbackVersionsObservers()
}
