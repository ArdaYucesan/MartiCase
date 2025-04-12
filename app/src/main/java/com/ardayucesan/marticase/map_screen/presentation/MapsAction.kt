package com.ardayucesan.marticase.map_screen.presentation

import com.ardayucesan.marticase.map_screen.domain.AppLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

sealed interface MapsAction {
    data object OnStartLocationTrackerClicked :
        MapsAction


    data class OnCreateRoute(
        val destination: AppLocation,
    ) : MapsAction

    data class OnNewStepAdded(val latLng: LatLng, val marker: Marker?) : MapsAction

    data object OnResetPolyline : MapsAction

    data object OnClearMarkersAndLatLngs : MapsAction

//    data class OnMarkerRestore(val latLng: LatLng, val marker: Marker) : MapsAction

}