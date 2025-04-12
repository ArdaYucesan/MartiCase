package com.ardayucesan.marticase.map_screen.data.gps.repository_impl

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ardayucesan.marticase.map_screen.domain.repository.LocationRepository
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result

//Lokasyon verisinin güncellenmesini ve iletilmesini sağlar
class LocationRepositoryImpl : LocationRepository {

    private val _locationLiveData = MutableLiveData<Result<Location, GpsError>>()

    override fun getUserLocationUpdates(): LiveData<Result<Location, GpsError>> {
        return _locationLiveData
    }

    override fun updateUserLocations(locationResult: Result<Location, GpsError>) {
        _locationLiveData.postValue(locationResult)
    }
}