package com.ardayucesan.marticase.map_screen.domain.utils

import android.location.Location
import com.ardayucesan.marticase.map_screen.domain.AppLocation

fun Location.toAppLocation(): AppLocation {
    return AppLocation(
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = this.time
    )
}