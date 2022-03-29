package com.mapbox.navigation.dropin.component.tripsession

import com.mapbox.navigation.dropin.model.Action

sealed class TripSessionStarterAction : Action {
    data class OnLocationPermission(val granted: Boolean) : TripSessionStarterAction()
    object EnableTripSession : TripSessionStarterAction()
    object EnableReplayTripSession : TripSessionStarterAction()
}
