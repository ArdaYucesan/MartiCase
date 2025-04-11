package com.ardayucesan.marticase.map_screen.domain.repository

import android.location.Location
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getUserLocationUpdates(intervalMillis: Long): Flow<Result<Location, GpsError>>
}