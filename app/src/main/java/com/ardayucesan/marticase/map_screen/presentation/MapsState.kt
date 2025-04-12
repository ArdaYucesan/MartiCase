package com.ardayucesan.marticase.map_screen.presentation

import com.ardayucesan.marticase.map_screen.data.network.dto.Polyline
import com.ardayucesan.marticase.map_screen.domain.AppLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class MapsState(
    val userLocation: AppLocation? = null,
    val userLocationTrackerStarted: Boolean = false,
    val currentPolyline: Polyline? = null,
    val destinationMarker: Marker? = null,
    val stepMarker: List<Marker> = emptyList(),
    val stepLatLng: List<LatLng> = emptyList(),
    val loading: Boolean = true,
)