package com.mapbox.navigation.core.reroute

import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.navigator.internal.mapToDirectionsResponse
import com.mapbox.navigation.navigator.internal.mapToNativeRouteOrigin
import com.mapbox.navigator.RerouteCallback
import com.mapbox.navigator.RerouteControllerInterface
import com.mapbox.navigator.RerouteError
import com.mapbox.navigator.RerouteErrorType
import com.mapbox.navigator.RerouteInfo

internal class RerouteControllerAdapter(
    private val accessToken: String?,
    private val navigationRerouteController: NavigationRerouteController,
) : RerouteControllerInterface {

    private companion object {
        private const val ERROR_EMPTY_NAVIGATION_ROUTES_LIST =
            "List of NavigationRoute mustn't be empty"
    }

    override fun reroute(url: String, callback: RerouteCallback) {
        navigationRerouteController.reroute { navRoutes: List<NavigationRoute>, origin ->
            val expected: Expected<RerouteError, RerouteInfo> = if (navRoutes.isNotEmpty()) {
                ExpectedFactory.createValue(
                    RerouteInfo(
                        navRoutes.mapToDirectionsResponse().toJson(),
                        navRoutes.first().routeOptions.toUrl(accessToken ?: "").toString(),
                        origin.mapToNativeRouteOrigin(),
                    )
                )
            } else {
                ExpectedFactory.createError(
                    RerouteError(
                        ERROR_EMPTY_NAVIGATION_ROUTES_LIST,
                        RerouteErrorType.ROUTER_ERROR,
                    )
                )
            }

            callback.run(expected)
        }
    }

    override fun cancel() {
        navigationRerouteController.interrupt()
    }
}
