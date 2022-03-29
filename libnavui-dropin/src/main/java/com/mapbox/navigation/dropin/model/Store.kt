package com.mapbox.navigation.dropin.model

import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.dropin.component.audioguidance.AudioGuidanceState
import com.mapbox.navigation.dropin.component.camera.CameraState
import com.mapbox.navigation.dropin.component.destination.Destination
import com.mapbox.navigation.dropin.component.navigation.NavigationState
import com.mapbox.navigation.dropin.component.routefetch.RoutesState
import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterState
import com.mapbox.navigation.utils.internal.logW
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentLinkedQueue

data class State(
    val destination: Destination? = null,
    val location: LocationMatcherResult? = null,
    val navigation: NavigationState = NavigationState.FreeDrive,
    val camera: CameraState = CameraState(),
    val audio: AudioGuidanceState = AudioGuidanceState(),
    val routes: RoutesState = RoutesState.Empty,
    val tripSession: TripSessionStarterState = TripSessionStarterState()
)

interface Action

fun interface Reducer {
    fun process(state: State, action: Action): State
}

internal open class Store {
    protected val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val reducers = ConcurrentLinkedQueue<Reducer>()
    private var isDispatching = false

    fun <T> select(selector: (State) -> T): Flow<T> {
        return state.map { selector(it) }.distinctUntilChanged()
    }

    fun dispatch(action: Action) {
        if (isDispatching) {
            logW(
                "Cannot dispatch new actions during reducer processing. " +
                    "Action dropped: ${action::class.java.simpleName}.",
                "Store"
            )
            return
        }

        isDispatching = true
        reducers.forEach { reducer ->
            _state.value = reducer.process(_state.value, action)
        }
        isDispatching = false
    }

    fun register(vararg reducers: Reducer) {
        this.reducers.addAll(reducers)
    }

    fun unregister(vararg reducers: Reducer) {
        this.reducers.removeAll(reducers)
    }
}
