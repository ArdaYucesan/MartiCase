package com.ardayucesan.marticase.map_screen.presentation

sealed interface MapsEvent {
    data class ShowErrorToast(val message: String) : MapsEvent

    data object ShowGpsDisabledDialog : MapsEvent

    data object ShowNetworkDisabledDialog : MapsEvent

    data object RequestLocationPermission : MapsEvent
}