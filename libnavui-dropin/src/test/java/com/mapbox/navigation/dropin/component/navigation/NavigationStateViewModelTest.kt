package com.mapbox.navigation.dropin.component.navigation

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.dropin.util.TestStore
import com.mapbox.navigation.testing.MainCoroutineRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class, ExperimentalCoroutinesApi::class)
internal class NavigationStateViewModelTest {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private lateinit var testStore: TestStore

    lateinit var sut: NavigationStateViewModel

    @Before
    fun setUp() {
        mockkObject(MapboxNavigationApp)

        testStore = TestStore()
        sut = NavigationStateViewModel(testStore)
    }

    @After
    fun teardown() {
        unmockkObject(MapboxNavigationApp)
    }

    @Test
    fun `should set new state on Update action`() = coroutineRule.runBlockingTest {
        sut.onAttached(mockMapboxNavigation())

        testStore.dispatch(NavigationStateAction.Update(NavigationState.RoutePreview))

        assertEquals(NavigationState.RoutePreview, testStore.state.value.navigation)
    }

    private fun mockMapboxNavigation(): MapboxNavigation {
        val mapboxNavigation = mockk<MapboxNavigation>()
        every { MapboxNavigationApp.current() } returns mapboxNavigation
        return mapboxNavigation
    }
}
