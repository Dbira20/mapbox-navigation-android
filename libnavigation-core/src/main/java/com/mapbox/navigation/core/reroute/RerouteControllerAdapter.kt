package com.mapbox.navigation.core.reroute

import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.toNavigationRoutes
import com.mapbox.navigation.navigator.internal.mapToDirectionsResponse
import com.mapbox.navigator.RerouteCallback
import com.mapbox.navigator.RerouteControllerInterface
import com.mapbox.navigator.RerouteInfo
import com.mapbox.navigator.RouterOrigin

internal class RerouteControllerAdapter(
    private val rerouteController: RerouteController,
    private val accessToken: String,
) : RerouteControllerInterface {

    override fun reroute(url: String, callback: RerouteCallback) {
        rerouteController.reroute { directionsRoutes ->
            val navRoutes = directionsRoutes.toNavigationRoutes()

            if (navRoutes.isNotEmpty()){
                callback.run(
                    ExpectedFactory.createValue(
                        RerouteInfo(
                            navRoutes.mapToDirectionsResponse().toJson(),
                            navRoutes.first().routeOptions.toUrl(accessToken).toString(),
                            RouterOrigin.CUSTOM,
                        )
                    )
                )
            }

        }
    }

    override fun cancel() {
        rerouteController.interrupt()
    }
}
