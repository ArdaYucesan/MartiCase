package com.ardayucesan.marticase.map_screen.presentation

import com.ardayucesan.marticase.map_screen.domain.AppLocation

sealed interface MapsEvent {
    data class ShowError(val message: String) : MapsEvent
}