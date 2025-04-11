package com.ardayucesan.marticase.map_screen.domain.use_case

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
    operator fun invoke(
        intervalMillis: Long,
    ): Flow<Result<UserLocation, GpsError>> {
        return locationRepository.getUserLocationUpdates(intervalMillis)
            .map { locationResult ->
                when (locationResult) {
                    is Result.Error -> {
                        Result.Error(locationResult.error)
                    }

                    is Result.Success -> {
                        // converting android Location to apps UserLocation data class
                        Result.Success(locationResult.data.toUserLocation())
                    }
                }
            }
    }
}