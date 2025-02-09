package com.mapbox.navigation.dropin.component.maneuver

import android.transition.Scene
import android.transition.TransitionManager
import android.view.ViewGroup
import com.mapbox.maps.Style
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.dropin.R
import com.mapbox.navigation.dropin.binder.UIBinder
import com.mapbox.navigation.dropin.databinding.MapboxManeuverGuidanceLayoutBinding
import com.mapbox.navigation.dropin.internal.extensions.reloadOnChange
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class ManeuverViewBinder(
    private val loadedMapStyle: StateFlow<Style?>
) : UIBinder {
    override fun bind(viewGroup: ViewGroup): MapboxNavigationObserver {
        val scene = Scene.getSceneForLayout(
            viewGroup,
            R.layout.mapbox_maneuver_guidance_layout,
            viewGroup.context
        )
        TransitionManager.go(scene)
        val binding = MapboxManeuverGuidanceLayoutBinding.bind(viewGroup)

        return reloadOnChange(loadedMapStyle) {
            if (it != null) ManeuverComponent(binding.maneuverView, it)
            else null
        }
    }
}
