package com.mapbox.navigation.qa_test_app.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.qa_test_app.databinding.LayoutActivityRerouteBinding
import com.mapbox.navigation.qa_test_app.utils.Utils
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

class RerouteActivity : AppCompatActivity() {

    private val replayRouteMapper = ReplayRouteMapper()
    private val mapboxReplayer = MapboxReplayer()
    private val navigationLocationProvider = NavigationLocationProvider()

    private var rerouteControllerType = RerouteControllerType.Default
        set(value) {
            if (field == value) return
            field = value

        }

    private val binding by lazy { LayoutActivityRerouteBinding.inflate(layoutInflater) }

    private val mapboxNavigation: MapboxNavigation by lazy {
        MapboxNavigationProvider.create(
            NavigationOptions.Builder(this)
                .accessToken(Utils.getMapboxAccessToken(this))
                .locationEngine(ReplayLocationEngine(mapboxReplayer))
                .build()
        )
    }

    private val locationComponent by lazy {
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
    }

    private val mapCamera: CameraAnimationsPlugin by lazy {
        binding.mapView.camera
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initNavigation()
        initStyle()
        initListeners()
    }

    private fun initNavigation() {
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
//        mapboxNavigation.setRoutes(listOf(getRoute()))
    }

    private fun initStyle() {
        binding.mapView.getMapboxMap().loadStyleUri(
            NavigationStyles.NAVIGATION_DAY_STYLE
        ) { style ->
//            val route = getRoute()
//            routeLineApi.setRoutes(listOf(RouteLine(route, null))) {
//                routeLineView.renderRouteDrawData(style, it)
//            }

//            val routeOrigin = Utils.getRouteOriginPoint(route)
//            val cameraOptions = CameraOptions.Builder().center(routeOrigin).zoom(15.0).build()
//            binding.mapView.getMapboxMap().setCamera(cameraOptions)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initListeners() {
//        binding.startNavigation.setOnClickListener {
//            mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
//            mapboxNavigation.startTripSession()
//            binding.startNavigation.visibility = View.GONE
//            locationComponent.addOnIndicatorPositionChangedListener(onPositionChangedListener)
//        }
    }


    private enum class RerouteControllerType {
        Default,
        Custom,
        Disabled,
    }
}
