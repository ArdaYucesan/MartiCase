package com.ardayucesan.marticase.map_screen.presentation

import com.ardayucesan.marticase.map_screen.domain.UserLocation
import com.google.android.gms.maps.model.LatLng

sealed interface MapsAction {
    data object OnStartLocationTrackerClicked :
        MapsAction

    data class OnCreateRoute(
        val destination: UserLocation,
    ) : MapsAction

    data class OnDecodedPathCreated(
        val route: List<LatLng> = emptyList()
    ) : MapsAction
}