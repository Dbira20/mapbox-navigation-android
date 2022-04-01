package com.mapbox.navigation.qa_test_app.car

import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.MapPluginProviderDelegate
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.component.camera.CameraAction
import com.mapbox.navigation.dropin.component.camera.CameraViewModel
import com.mapbox.navigation.dropin.component.camera.TargetCameraMode
import com.mapbox.navigation.dropin.component.location.LocationViewModel
import com.mapbox.navigation.dropin.component.navigation.NavigationState
import com.mapbox.navigation.dropin.component.navigation.NavigationStateViewModel
import com.mapbox.navigation.dropin.extensions.flowNavigationCameraState
import com.mapbox.navigation.dropin.extensions.flowRouteProgress
import com.mapbox.navigation.dropin.extensions.flowRoutesUpdated
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.utils.internal.logI
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(MapboxExperimental::class, ExperimentalPreviewMapboxNavigationAPI::class)
class CarCameraComponent constructor(
    private val mapboxMap: MapboxMap,
    private val mapPluginProvider: MapPluginProviderDelegate,
    private val cameraViewModel: CameraViewModel,
    private val locationViewModel: LocationViewModel,
    private val navigationStateViewModel: NavigationStateViewModel,
    private val viewportDataSource: MapboxNavigationViewportDataSource =
        MapboxNavigationViewportDataSource(mapboxMap = mapboxMap),
    private val navigationCamera: NavigationCamera =
        NavigationCamera(
            mapboxMap = mapboxMap,
            cameraPlugin = mapPluginProvider.camera,
            viewportDataSource = viewportDataSource
        ),
) : UIComponent() {

    private var isCameraInitialized = false
    private val gesturesHandler = NavigationBasicGesturesHandler(navigationCamera)

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        mapPluginProvider.camera.addCameraAnimationsLifecycleListener(gesturesHandler)

        cameraViewModel.state.map { it.cameraPadding }.observe {
            viewportDataSource.overviewPadding = it
            viewportDataSource.followingPadding = it
            viewportDataSource.evaluate()
        }

        controlCameraFrameOverrides()
        updateCameraFrame()
        updateCameraLocation()
        onNavigationCameraStateChanged()
        onRouteProgressUpdates(mapboxNavigation)
        onRouteUpdates(mapboxNavigation)
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        mapPluginProvider.camera.removeCameraAnimationsLifecycleListener(gesturesHandler)
    }

    private fun controlCameraFrameOverrides() {
        coroutineScope.launch {
            navigationStateViewModel.state.collect {
                when (it) {
                    is NavigationState.FreeDrive -> {
                        viewportDataSource.followingZoomPropertyOverride(FOLLOWING_ZOOM_OVERRIDE)
                        viewportDataSource.overviewZoomPropertyOverride(OVERVIEW_ZOOM_OVERRIDE)
                    }
                    else -> {
                        viewportDataSource.clearFollowingOverrides()
                        viewportDataSource.clearOverviewOverrides()
                    }
                }
                viewportDataSource.evaluate()
            }
        }
    }

    private fun updateCameraFrame() {
        coroutineScope.launch {
            cameraViewModel.state.collect { state ->
                if (isCameraInitialized) {
                    requestCameraModeTo(cameraMode = state.cameraMode)
                }
            }
        }
    }

    private fun updateCameraLocation() {
        logI("CarCameraComponent updateCameraLocation", "kyle_debug")
        coroutineScope.launch {
            combine(
                locationViewModel.state,
                navigationStateViewModel.state
            ) { location, navigationState ->
                location?.let {
                    viewportDataSource.onLocationChanged(it)
                    viewportDataSource.evaluate()
                    if (!isCameraInitialized) {
                        when (navigationState) {
                            NavigationState.ActiveNavigation,
                            NavigationState.Arrival -> {
                                navigationCamera.requestNavigationCameraToFollowing(
                                    stateTransitionOptions = NavigationCameraTransitionOptions
                                        .Builder()
                                        .maxDuration(0) // instant transition
                                        .build()
                                )
                                cameraViewModel.invoke(
                                    CameraAction.ToFollowing
                                )
                            }
                            else -> {
                                navigationCamera.requestNavigationCameraToOverview(
                                    stateTransitionOptions = NavigationCameraTransitionOptions
                                        .Builder()
                                        .maxDuration(0) // instant transition
                                        .build()
                                )
                                cameraViewModel.invoke(
                                    CameraAction.ToOverview
                                )
                            }
                        }
                    }
                    isCameraInitialized = true
                }
            }.collect()
        }
    }

    private fun onNavigationCameraStateChanged() {
        coroutineScope.launch {
            navigationCamera.flowNavigationCameraState().collect {
                when (it) {
                    NavigationCameraState.IDLE -> {
                        cameraViewModel.invoke(CameraAction.ToIdle)
                    }
                    else -> {
                        // no op
                    }
                }
            }
        }
    }

    private fun onRouteProgressUpdates(mapboxNavigation: MapboxNavigation) {
        coroutineScope.launch {
            mapboxNavigation.flowRouteProgress().collect { routeProgress ->
                viewportDataSource.onRouteProgressChanged(routeProgress)
                viewportDataSource.evaluate()
            }
        }
    }

    private fun onRouteUpdates(mapboxNavigation: MapboxNavigation) {
        coroutineScope.launch {
            combine(
                mapboxNavigation.flowRoutesUpdated(),
                navigationStateViewModel.state
            ) { routeUpdate, navigationState ->
                if (routeUpdate.navigationRoutes.isNotEmpty()) {
                    viewportDataSource.onRouteChanged(routeUpdate.navigationRoutes.first())
                    viewportDataSource.evaluate()
                    when (navigationState) {
                        NavigationState.ActiveNavigation,
                        NavigationState.Arrival -> {
                            cameraViewModel.invoke(CameraAction.ToFollowing)
                        }
                        else -> {
                            cameraViewModel.invoke(CameraAction.ToOverview)
                        }
                    }
                } else {
                    viewportDataSource.clearRouteData()
                    viewportDataSource.evaluate()
                }
            }.collect()
        }
    }

    private fun requestCameraModeTo(cameraMode: TargetCameraMode) {
        when (cameraMode) {
            is TargetCameraMode.Idle -> {
                navigationCamera.requestNavigationCameraToIdle()
            }
            is TargetCameraMode.Overview -> {
                navigationCamera.requestNavigationCameraToOverview()
            }
            is TargetCameraMode.Following -> {
                navigationCamera.requestNavigationCameraToFollowing()
            }
        }
    }

    companion object {
        private const val OVERVIEW_ZOOM_OVERRIDE = 16.5
        private const val FOLLOWING_ZOOM_OVERRIDE = 16.5
    }
}
