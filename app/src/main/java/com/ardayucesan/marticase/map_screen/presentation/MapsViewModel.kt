package com.ardayucesan.marticase.map_screen.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardayucesan.marticase.map_screen.domain.UserLocation
import com.ardayucesan.marticase.map_screen.domain.use_case.GetRoutesUseCase
import com.ardayucesan.marticase.map_screen.domain.use_case.GetUserLocationUseCase
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import com.ardayucesan.marticase.map_screen.domain.utils.toUserLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch

class MapsViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getRoutesUseCase: GetRoutesUseCase
) : ViewModel() {
    private val _mapsState = MutableLiveData(MapsState())
    val mapsState: LiveData<MapsState> = _mapsState

    private val locationObserver = Observer<Location> { location ->

        println("got location in viewModel observer $location")

        _mapsState.postValue(
            _mapsState.value?.copy(userLocation = location.toUserLocation())
        )
    }

    fun onAction(action: MapsAction) {
        when (action) {
            is MapsAction.OnStartLocationTrackerClicked -> {
                viewModelScope.launch {
                    startLocationTracking()
                }
            }

            is MapsAction.OnCreateRoute -> {
                viewModelScope.launch {
                    getRoutes(action.destination)
                }
            }

            is MapsAction.OnDecodedPathCreated -> {
                viewModelScope.launch {
//                    getUserLocation(action.route)
                }
            }

            is MapsAction.OnNewStepAdded -> {
                addStepLatLngs(action.latLng)
                action.marker?.let { addStepMarkers(it) }
            }

            MapsAction.OnClearMarkersAndLatLngs -> {
                clearMarkersAndLatLngs()
            }

            MapsAction.OnResetPolyline -> {
                clearPolyline()
            }
        }
    }

    private suspend fun getRoutes(destination: UserLocation) {
        _mapsState.value?.userLocation?.let { userLocation ->
            val result = getRoutesUseCase(origin = userLocation, destination = destination)

            when (result) {
                is Result.Error -> {
                    Log.e("_MapsViewModel", "getRoutes:  ${result.error.message}")
                }

                is Result.Success -> {
                    println("polyline -> ${result.data.encodedPolyline}")
                    _mapsState.postValue(
                        _mapsState.value?.copy(
                            currentPolyline = result.data,
                        )
                    )
                }
            }
        }
    }

    private fun clearPolyline() {
        _mapsState.value = _mapsState.value?.copy(currentPolyline = null)
    }

    private fun clearMarkersAndLatLngs() {
        _mapsState.value?.stepMarker?.forEach { marker ->
            marker.remove()
        }

        _mapsState.value = _mapsState.value?.copy(
            stepLatLng = emptyList(),
            stepMarker = emptyList()
        )
        println("should be cleared")
    }

    private fun addStepLatLngs(latLng: LatLng) {
        _mapsState.value?.let { currentState ->
            println("should have added new latLn ${latLng}")
            // Yeni step marker'ı mevcut listeye ekleyip yeni bir kopya oluşturuyoruz
            _mapsState.value = currentState.copy(stepLatLng = currentState.stepLatLng + latLng)
        }
    }

    private fun addStepMarkers(marker: Marker) {
        println("should have added new marker ${marker}")

        _mapsState.value?.let { currentState ->
            // Yeni step marker'ı mevcut listeye ekleyip yeni bir kopya oluşturuyoruz
            _mapsState.value = currentState.copy(stepMarker = currentState.stepMarker + marker)
        }
    }

    private fun startLocationTracking() {
        getUserLocationUseCase().observeForever(locationObserver)
    }

    override fun onCleared() {
        super.onCleared()
        println("observer removed viewmodel")
        getUserLocationUseCase().removeObserver(locationObserver)
    }

//    private fun getUserLocation() {
//        getUserLocationUseCase()
//            .observeForever { location ->
//                _mapsState.postValue(
//                    _mapsState.value?.copy(userLocation = location)
//                )
//            }
////        getUserLocationUseCase().collect { location ->
////            location?.let {
////                _mapsState.update {
////                    it.copy(userLocation = location)
////                }
////            }
////        }
//        // millis under 30000 is unimportant
////        getUserLocationUseCase(5000)
////            .collect { locationResult ->
////                when (locationResult) {
////                    is Result.Error -> TODO()
////                    is Result.Success -> {
////                        // used postValue for updating LiveData from co routine
////                        println("location updated ${locationResult.data.latitude}")
////                        _mapsState.postValue(_mapsState.value?.copy(userLocation = locationResult.data))
//////                        _mapsState.value =
//////                            _mapsState.value!!.copy(userLocation = locationResult.data)
////
////                    }
////                }
////            }
//
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        locationRepository.locationLiveData.removeObserver { } // Optional temizleme
//    }
}