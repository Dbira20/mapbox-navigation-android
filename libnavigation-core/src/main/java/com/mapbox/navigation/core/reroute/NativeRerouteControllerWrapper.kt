package com.mapbox.navigation.core.reroute

import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.navigator.internal.MapboxNativeNavigator
import com.mapbox.navigator.RerouteCallback
import com.mapbox.navigator.RerouteControllerInterface
import com.mapbox.navigator.RerouteError
import com.mapbox.navigator.RerouteInfo
import com.mapbox.navigator.RerouteObserver
import java.net.URL

internal class NativeRerouteControllerWrapper(
    private val accessToken: String?,
    private val nativeRerouteController: RerouteControllerInterface,
    private val nativeNavigator: MapboxNativeNavigator,
//    private val urlAdapter: (String) -> String,
//    private val rerouteObserver: (result: Expected<RerouteError, RerouteInfo>) -> Unit,
) : NativeExtendedRerouteControllerInterface {

    private var rerouteOptionsAdapter: RerouteOptionsAdapter? = null

    override fun reroute(url: String, callback: RerouteCallback) {
        val newUrl = when (val adapter = rerouteOptionsAdapter) {
            null -> url
            else -> adapter.onRouteOptions(RouteOptions.fromUrl(URL(url)))
                .toUrl(accessToken ?: "")
                .toString()
        }
        nativeRerouteController.reroute(newUrl) { result ->
            callback.run(result)
//            rerouteObserver(result)
        }
    }

    override fun cancel() {
        nativeRerouteController.cancel()
    }

    override fun addRerouteObserver(rerouteObserver: RerouteObserver) {
        nativeNavigator.addRerouteObserver(rerouteObserver)
    }

    override fun forceReroute() {
        nativeNavigator.getRerouteDetector().forceReroute()
    }

    internal fun setRerouteOptionsAdapter(rerouteOptionsAdapter: RerouteOptionsAdapter){
        this.rerouteOptionsAdapter = rerouteOptionsAdapter
    }

}
