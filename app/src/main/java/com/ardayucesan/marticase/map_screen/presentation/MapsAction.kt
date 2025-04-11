package com.ardayucesan.marticase.map_screen.presentation

import com.ardayucesan.marticase.map_screen.domain.UserLocation

sealed interface MapsAction {
    data object OnStartLocationTrackerClicked : MapsAction
    data class OnCreateRoute(val destination: UserLocation) : MapsAction
}