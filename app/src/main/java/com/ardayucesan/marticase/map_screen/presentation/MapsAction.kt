package com.ardayucesan.marticase.map_screen.presentation

import com.ardayucesan.marticase.map_screen.domain.UserLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

sealed interface MapsAction {
    data object OnStartLocationTrackerClicked :
        MapsAction


    data class OnCreateRoute(
        val destination: UserLocation,
    ) : MapsAction

    data class OnDecodedPathCreated(
        val route: List<LatLng> = emptyList()
    ) : MapsAction

    data class OnNewStepAdded(val latLng: LatLng, val marker: Marker?) : MapsAction

    data object OnResetPolyline : MapsAction

    data object OnClearMarkersAndLatLngs : MapsAction

//    data class OnMarkerRestore(val latLng: LatLng, val marker: Marker) : MapsAction

}