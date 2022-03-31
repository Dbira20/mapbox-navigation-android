package com.mapbox.navigation.core.reroute

import com.mapbox.navigation.core.directions.session.DirectionsSession
import com.mapbox.navigation.navigator.internal.MapboxNativeNavigator
import com.mapbox.navigator.RerouteControllerInterface

internal class RerouteControllersManager private constructor(
    private val accessToken: String?,
    private val directionsSession: DirectionsSession,
    private val navigator: MapboxNativeNavigator,
) {

    // hold initial NN Reroute controller to have option re-set it in runtime
    private var initialNativeRerouteControllerInterface: RerouteControllerInterface

    private var rerouteOptionsAdapter: RerouteOptionsAdapter? = null

    private var rerouteInterfaceSet: RerouteInterfacesSet = RerouteInterfacesSet.Disabled()
        set(value) {
            field = value
            navigator.setRerouteControllerInterface(value.rerouteControllerInterface)
        }

    val rerouteControllerInterface: NavigationRerouteController?
        get() = rerouteInterfaceSet.navigationRerouteController

    init {
        initialNativeRerouteControllerInterface = navigator.getRerouteControllerInterface()

        rerouteInterfaceSet = RerouteInterfacesSet.Internal(
            accessToken,
            initialNativeRerouteControllerInterface,
            navigator,
            directionsSession,
        )
    }

    internal companion object {
        /**
         * Provides Reroute Controllers Manager with initial [RerouteInterfacesSet.Internal] set
         */
        fun provideRerouteManager(
            accessToken: String?,
            directionsSession: DirectionsSession,
            navigator: MapboxNativeNavigator
        ): RerouteControllersManager {

            return RerouteControllersManager(
                accessToken,
                directionsSession,
                navigator,
            )
        }

        private fun wrapNativeRerouteControllerInterface(
            accessToken: String?,
            initialNativeRerouteControllerInterface: RerouteControllerInterface,
            navigator: MapboxNativeNavigator,
        ): NativeExtendedRerouteControllerInterface =
            NativeRerouteControllerWrapper(
                accessToken,
                initialNativeRerouteControllerInterface,
                navigator,
            )
    }

    fun setOuterRerouteController(customerRerouteController: NavigationRerouteController) {
        rerouteInterfaceSet = RerouteInterfacesSet.Outer(
            accessToken,
            customerRerouteController,
        )
    }

    fun disableReroute() {
        rerouteInterfaceSet = RerouteInterfacesSet.Disabled()
    }

    fun resetToDefaultRerouteController() {
        rerouteInterfaceSet = RerouteInterfacesSet.Internal(
            accessToken,
            initialNativeRerouteControllerInterface,
            navigator,
            directionsSession,
        )
    }

    fun interruptReroute() {
        rerouteControllerInterface?.interrupt()
    }

    fun onNavigatorRecreated() {
        initialNativeRerouteControllerInterface = navigator.getRerouteControllerInterface()

        rerouteInterfaceSet = when (val legacyInterfaceSet = rerouteInterfaceSet) {
            is RerouteInterfacesSet.Outer ->
                legacyInterfaceSet.copy(initialNativeRerouteControllerInterface)
            is RerouteInterfacesSet.Disabled ->
                RerouteInterfacesSet.Disabled()
            is RerouteInterfacesSet.Internal ->
                RerouteInterfacesSet.Internal(
                    accessToken,
                    initialNativeRerouteControllerInterface,
                    navigator,
                    directionsSession,
                )
        }
        if (rerouteInterfaceSet is RerouteInterfacesSet.Internal) {
            setRerouteOptionsAdapter(this.rerouteOptionsAdapter)
        }
    }

    fun setRerouteOptionsAdapter(rerouteOptionsAdapter: RerouteOptionsAdapter?) {
        this.rerouteOptionsAdapter = rerouteOptionsAdapter
    }

    private sealed class RerouteInterfacesSet(
        val navigationRerouteController: NavigationRerouteController?,
        val rerouteControllerInterface: RerouteControllerInterface,
    ) {
        class Internal private constructor(
            navigationRerouteController: NavigationRerouteController,
            rerouteControllerInterface: NativeExtendedRerouteControllerInterface,
        ) : RerouteInterfacesSet(navigationRerouteController, rerouteControllerInterface) {
            companion object {
                operator fun invoke(
                    accessToken: String?,
                    initialNativeRerouteControllerInterface: RerouteControllerInterface,
                    navigator: MapboxNativeNavigator,
                    directionsSession: DirectionsSession,
                ): Internal {

                    val nativeRerouteControllerWrapper = wrapNativeRerouteControllerInterface(
                        accessToken,
                        initialNativeRerouteControllerInterface,
                        navigator,
                    )

                    val platformRouteController = MapboxRerouteControllerFacade(
                        directionsSession,
                        nativeRerouteControllerWrapper,
                    )

                    return Internal(
                        platformRouteController,
                        nativeRerouteControllerWrapper
                    )
                }
            }
        }

        class Outer private constructor(
            private val _navigationRerouteController: NavigationRerouteController,
            rerouteControllerInterface: RerouteControllerInterface,
        ) : RerouteInterfacesSet(_navigationRerouteController, rerouteControllerInterface) {
            companion object {
                operator fun invoke(
                    accessToken: String?,
                    customerRerouteController: NavigationRerouteController,
                ): Outer =
                    Outer(
                        customerRerouteController,
                        RerouteControllerAdapter(accessToken, customerRerouteController),
                    )
            }

            fun copy(rerouteControllerInterface: RerouteControllerInterface) = Outer(
                _navigationRerouteController, rerouteControllerInterface
            )
        }

        class Disabled private constructor() : RerouteInterfacesSet(
            null,
            DisabledRerouteControllerInterface(),
        ) {
            companion object {
                operator fun invoke(): Disabled = Disabled()
            }
        }
    }

//    fun interface RerouteInterfacesObserver {
//        fun onNewRerouteInterfaces(
//            navigationRerouteController: NavigationRerouteController,
//            rerouteControllerInterface: RerouteControllerInterface,
//        )
//    }
}
