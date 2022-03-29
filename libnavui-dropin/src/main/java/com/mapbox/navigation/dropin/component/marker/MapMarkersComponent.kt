package com.mapbox.navigation.dropin.component.marker

import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.dropin.model.Store

/**
 * Component for rendering all drop-in UI map markers.
 */
internal open class MapMarkersComponent(
    private val store: Store,
    protected val mapView: MapView,
) : UIComponent() {

    private val mapMarkerFactory by lazy {
        MapMarkerFactory.create(mapView.context)
    }
    private var annotationManager = mapView.annotations.createPointAnnotationManager()

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        store.select { it.destination?.point }
            .observe { point ->
                annotationManager.deleteAll()
                if (point != null) {
                    val annotation = mapMarkerFactory.createPin(point)
                    annotationManager.create(annotation)
                }
            }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        annotationManager.deleteAll()
    }
}
