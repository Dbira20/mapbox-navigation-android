package com.mapbox.navigation.dropin.component.navigation

import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.dropin.model.Action
import com.mapbox.navigation.dropin.model.Reducer
import com.mapbox.navigation.dropin.model.State
import com.mapbox.navigation.dropin.model.Store

internal class NavigationStateViewModel(store: Store) : UIComponent(), Reducer {
    init {
        store.register(this)
    }

    // TODO get destination and navigation route for initial state

    override fun process(state: State, action: Action): State {
        if (action is NavigationStateAction) {
            return state.copy(
                navigation = processNavigationAction(state.navigation, action)
            )
        }
        return state
    }

    private fun processNavigationAction(
        state: NavigationState,
        action: NavigationStateAction
    ): NavigationState {
        return when (action) {
            is NavigationStateAction.Update -> action.state
        }
    }
}
