package com.ardayucesan.marticase.map_screen.data.gps.repository_impl

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ardayucesan.marticase.map_screen.domain.LocationTracker
import com.ardayucesan.marticase.map_screen.domain.repository.LocationRepository

class LocationRepositoryImpl(
//    private val locationTracker: LocationTracker
) : LocationRepository {

    private val _locationLiveData = MutableLiveData<Location>()
//    val locationLiveData: LiveData<Location> = _locationLiveData

    override fun getUserLocationUpdates(): LiveData<Location> {
        println("user location get in repository impl")

        return _locationLiveData
    }

    override fun updateUserLocations(location: Location) {
        println("user location updated in repository impl")
        _locationLiveData.postValue(location)
    }
}