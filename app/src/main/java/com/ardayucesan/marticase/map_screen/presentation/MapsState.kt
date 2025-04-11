package com.ardayucesan.marticase.map_screen.presentation

import com.ardayucesan.marticase.map_screen.data.network.dto.Polyline
import com.ardayucesan.marticase.map_screen.domain.UserLocation

data class MapsState(
    val userLocation: UserLocation? = null,
    val userLocationTrackerStarted: Boolean = false,
    val currentPolyline: Polyline? = null,
    val loading: Boolean = true,
)