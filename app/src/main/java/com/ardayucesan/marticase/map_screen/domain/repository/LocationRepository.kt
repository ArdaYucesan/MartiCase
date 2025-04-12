package com.ardayucesan.marticase.map_screen.domain.repository

import android.location.Location
import androidx.lifecycle.LiveData
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result

interface LocationRepository {
    fun getUserLocationUpdates(): LiveData<Result<Location, GpsError>>

    fun updateUserLocations(locationResult: Result<Location, GpsError>)
}