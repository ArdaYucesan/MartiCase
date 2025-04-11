package com.ardayucesan.marticase.map_screen.domain.use_case

import android.location.Location
import androidx.lifecycle.LiveData
import com.ardayucesan.marticase.map_screen.domain.UserLocation
import com.ardayucesan.marticase.map_screen.domain.repository.LocationRepository
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import com.ardayucesan.marticase.map_screen.domain.utils.toUserLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetUserLocationUseCase(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): LiveData<Location> {
        return locationRepository.getUserLocationUpdates()
    }

}