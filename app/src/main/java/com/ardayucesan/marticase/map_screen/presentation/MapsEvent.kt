package com.ardayucesan.marticase.map_screen.presentation

sealed interface MapsEvent {
    data class ShowError(val message: String) : MapsEvent
}