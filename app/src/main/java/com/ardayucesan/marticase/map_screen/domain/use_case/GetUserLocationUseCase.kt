package com.ardayucesan.marticase.map_screen.domain.use_case

import android.location.Location
import androidx.lifecycle.LiveData
import com.ardayucesan.marticase.map_screen.domain.repository.LocationRepository
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result

class GetUserLocationUseCase(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): LiveData<Result<Location, GpsError>> {
        return locationRepository.getUserLocationUpdates()
    }
}