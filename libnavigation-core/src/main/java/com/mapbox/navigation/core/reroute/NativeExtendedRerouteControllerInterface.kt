package com.mapbox.navigation.core.reroute

import com.mapbox.navigator.RerouteControllerInterface
import com.mapbox.navigator.RerouteObserver

interface NativeExtendedRerouteControllerInterface: RerouteControllerInterface {
    fun addRerouteObserver(rerouteObserver: RerouteObserver)
    fun forceReroute()
}
