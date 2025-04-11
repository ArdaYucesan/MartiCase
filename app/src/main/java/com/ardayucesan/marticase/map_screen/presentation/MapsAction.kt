package com.ardayucesan.marticase.map_screen.presentation

sealed interface MapsAction {

    data object OnStartLocationTrackerClicked : MapsAction
}