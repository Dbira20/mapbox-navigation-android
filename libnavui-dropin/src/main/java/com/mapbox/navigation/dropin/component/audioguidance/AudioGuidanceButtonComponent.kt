package com.mapbox.navigation.dropin.component.audioguidance

import androidx.core.view.isVisible
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.R
import com.mapbox.navigation.dropin.component.navigation.NavigationState
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.dropin.model.Store
import com.mapbox.navigation.dropin.view.MapboxExtendableButton
import com.mapbox.navigation.dropin.view.MapboxExtendableButton.State
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class AudioGuidanceButtonComponent(
    private val store: Store,
    private val soundButton: MapboxExtendableButton,
) : UIComponent() {

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        coroutineScope.launch {
            store.select { it.audio }.collect {
                if (it.isMuted) soundButton.setState(MUTED)
                else soundButton.setState(UN_MUTED)
            }
        }

        coroutineScope.launch {
            store.select { it.navigation }.collect {
                soundButton.isVisible = it == NavigationState.ActiveNavigation
            }
        }

        soundButton.setOnClickListener {
            store.dispatch(AudioAction.Toggle)
        }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        soundButton.setOnClickListener(null)
    }

    companion object ButtonStates {
        private val UN_MUTED = State(R.drawable.mapbox_ic_sound_on)
        private val MUTED = State(R.drawable.mapbox_ic_sound_off)
    }
}
