package com.mapbox.navigation.dropin.component.navigation

import com.mapbox.navigation.dropin.model.Action

sealed class NavigationStateAction : Action {
    data class Update(val state: NavigationState) : NavigationStateAction()
}
