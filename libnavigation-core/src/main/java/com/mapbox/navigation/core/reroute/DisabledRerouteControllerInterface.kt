package com.mapbox.navigation.core.reroute

import com.mapbox.navigator.RerouteCallback
import com.mapbox.navigator.RerouteControllerInterface

internal class DisabledRerouteControllerInterface: RerouteControllerInterface {
    override fun reroute(url: String, callback: RerouteCallback) = Unit

    override fun cancel() = Unit
}
