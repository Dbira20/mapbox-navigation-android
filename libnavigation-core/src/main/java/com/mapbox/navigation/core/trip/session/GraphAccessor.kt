package com.mapbox.navigation.core.trip.session

import com.mapbox.geojson.Point
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.model.eh.EHorizonEdge
import com.mapbox.navigation.core.trip.model.eh.EHorizonEdgeMetadata
import com.mapbox.navigation.core.trip.model.eh.EHorizonGraphPath
import com.mapbox.navigation.core.trip.model.eh.EHorizonGraphPosition
import com.mapbox.navigation.core.trip.model.eh.mapToEHorizonEdgeMetadata
import com.mapbox.navigation.core.trip.model.eh.mapToGraphPath
import com.mapbox.navigation.core.trip.model.eh.mapToGraphPosition
import com.mapbox.navigation.navigator.internal.MapboxNativeNavigator

/**
 * [MapboxNavigation.graphAccessor] provides methods to get edge (e.g. [EHorizonEdge]) shape and
 * metadata.
 */
class GraphAccessor internal constructor(
    private val navigator: MapboxNativeNavigator,
) {

    /**
     * Returns Graph Edge geometry for the given GraphId of the edge.
     * If edge with given edgeId is not accessible, returns null
     * @param edgeId
     *
     * @return list of Points representing edge shape
     */
    fun getEdgeShape(edgeId: Long): List<Point>? {
        return navigator.graphAccessor?.getEdgeShape(edgeId)
    }

    /**
     * Returns Graph Edge meta-information for the given GraphId of the edge.
     * If edge with given edgeId is not accessible, returns null
     * @param edgeId
     *
     * @return EHorizonEdgeMetadata
     */
    fun getEdgeMetadata(edgeId: Long): EHorizonEdgeMetadata? {
        return navigator.graphAccessor?.getEdgeMetadata(edgeId)?.mapToEHorizonEdgeMetadata()
    }

    /**
     * Returns geometry of given path on graph.
     * If any of path edges is not accessible, returns null.
     */
    fun getPathShape(graphPath: EHorizonGraphPath): List<Point>? {
        return navigator.graphAccessor?.getPathShape(graphPath.mapToGraphPath())
    }

    /**
     * Returns geographical coordinate of given position on graph
     * If position's edge is not accessible, returns null.
     */
    fun getGraphPositionCoordinate(graphPosition: EHorizonGraphPosition): Point? {
        return navigator.graphAccessor?.getPositionCoordinate(graphPosition.mapToGraphPosition())
    }
}
