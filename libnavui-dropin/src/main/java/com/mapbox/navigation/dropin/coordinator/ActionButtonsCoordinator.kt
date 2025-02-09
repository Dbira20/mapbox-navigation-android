package com.mapbox.navigation.dropin.coordinator

import android.view.ViewGroup
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.NavigationViewContext
import com.mapbox.navigation.dropin.binder.ActionButtonBinder
import com.mapbox.navigation.dropin.binder.UIBinder
import com.mapbox.navigation.dropin.lifecycle.UICoordinator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Coordinator for navigation actions.
 * This is the side panel for a portrait view.
 */
@ExperimentalPreviewMapboxNavigationAPI
internal class ActionButtonsCoordinator(
    private val context: NavigationViewContext,
    actionList: ViewGroup
) : UICoordinator<ViewGroup>(actionList) {

    override fun MapboxNavigation.flowViewBinders(): Flow<UIBinder> {
        return context.uiBinders.actionButtonsBinder.map {
            it ?: ActionButtonBinder(context)
        }
    }
}
