package com.ardayucesan.marticase.map_screen.domain.utils

import android.location.Location
import com.ardayucesan.marticase.map_screen.domain.UserLocation

fun Location.toUserLocation(): UserLocation {
    return UserLocation(
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = this.time
    )
}