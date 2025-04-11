package com.ardayucesan.marticase.map_screen.domain.repository

import android.location.Location
import androidx.lifecycle.LiveData

interface LocationRepository {
    fun getUserLocationUpdates(): LiveData<Location>

    fun updateUserLocations(location: Location)
}