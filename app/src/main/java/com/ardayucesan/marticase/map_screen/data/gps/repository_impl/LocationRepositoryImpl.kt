package com.ardayucesan.marticase.map_screen.data.gps.repository_impl

import android.location.Location
import com.ardayucesan.marticase.map_screen.domain.LocationTracker
import com.ardayucesan.marticase.map_screen.domain.repository.LocationRepository
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import kotlinx.coroutines.flow.Flow

class LocationRepositoryImpl(
    private val locationTracker: LocationTracker
) : LocationRepository {
    override fun getUserLocationUpdates(intervalMillis: Long): Flow<Result<Location, GpsError>> {
        return locationTracker.getLocationUpdates(intervalMillis)
    }
}