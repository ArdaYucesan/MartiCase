package com.ardayucesan.marticase.map_screen.domain

import android.location.Location
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import kotlinx.coroutines.flow.Flow

interface LocationTracker {
    fun getLocationUpdates(interval: Long): Flow<Result<Location, GpsError>>

    class LocationException(message: String) : Exception()
}