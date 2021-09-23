package com.mapbox.navigation.dropin

import com.mapbox.maps.Style
import java.lang.IllegalArgumentException

class DropInViewOptions private constructor(
    val renderSpeedLimit: Boolean,
    val renderTripProgress: Boolean,
    val renderManeuvers: Boolean,
    val enableVanishingRouteLine: Boolean,
    val mapStyleUrlDarkTheme: String,
    val mapStyleUrlLightTheme: String,
) {

    fun toBuilder(): Builder = Builder().apply {
        renderSpeedLimit(renderSpeedLimit)
        renderTripProgress(renderTripProgress)
        renderManeuvers(renderManeuvers)
        enableVanishingRouteLine(enableVanishingRouteLine)
        mapStyleUrlDarkTheme(mapStyleUrlDarkTheme)
        mapStyleUrlLightTheme(mapStyleUrlLightTheme)
    }

    class Builder {
        private var renderSpeedLimit: Boolean = true
        private var renderTripProgress: Boolean = true
        private var renderManeuvers: Boolean = true
        private var enableVanishingRouteLine: Boolean = true
        private var mapStyleUrlDarkTheme: String = Style.LIGHT
        private var mapStyleUrlLightTheme: String = Style.DARK

        fun renderSpeedLimit(renderSpeedLimit: Boolean): Builder = apply  {
            this.renderSpeedLimit = renderSpeedLimit
        }

        fun renderTripProgress(renderTripProgress: Boolean): Builder = apply  {
            this.renderTripProgress = renderTripProgress
        }

        fun renderManeuvers(renderManeuvers: Boolean): Builder = apply  {
            this.renderManeuvers = renderManeuvers
        }

        fun enableVanishingRouteLine(enableVanishingRouteLine: Boolean): Builder = apply  {
            this.enableVanishingRouteLine = enableVanishingRouteLine
        }

        fun mapStyleUrlDarkTheme(mapStyleUrlDarkTheme: String): Builder {
            if (mapStyleUrlDarkTheme.isEmpty()) {
                throw IllegalArgumentException("Style url $mapStyleUrlDarkTheme cannot be empty")
            }
            this.mapStyleUrlDarkTheme = mapStyleUrlDarkTheme
            return this
        }

        fun mapStyleUrlLightTheme(mapStyleUrlLightTheme: String): Builder {
            if (mapStyleUrlLightTheme.isEmpty()) {
                throw IllegalArgumentException("Style url $mapStyleUrlLightTheme cannot be empty")
            }
            this.mapStyleUrlLightTheme = mapStyleUrlLightTheme
            return this
        }

        fun build(): DropInViewOptions = DropInViewOptions(
            renderSpeedLimit,
            renderTripProgress,
            renderManeuvers,
            enableVanishingRouteLine,
            mapStyleUrlDarkTheme,
            mapStyleUrlLightTheme
        )
    }
}
