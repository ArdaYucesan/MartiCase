package com.ardayucesan.marticase.map_screen.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardayucesan.marticase.map_screen.domain.UserLocation
import com.ardayucesan.marticase.map_screen.domain.use_case.GetRoutesUseCase
import com.ardayucesan.marticase.map_screen.domain.use_case.GetUserLocationUseCase
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch

class MapsViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getRoutesUseCase: GetRoutesUseCase
) : ViewModel() {
    private val _mapsState = MutableLiveData(MapsState())
    val mapsState: LiveData<MapsState> = _mapsState

    fun onAction(action: MapsAction) {
        when (action) {
            is MapsAction.OnStartLocationTrackerClicked -> {
                viewModelScope.launch {
                    getUserLocation()
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
                            routePaths = PolyUtil.decode(result.data.encodedPolyline)
                        )
                    )
                }
            }
        }
    }

    private suspend fun getUserLocation() {

        // millis under 30000 is unimportant
        getUserLocationUseCase(5000)
            .collect { locationResult ->
                when (locationResult) {
                    is Result.Error -> TODO()
                    is Result.Success -> {
                        // used postValue for updating LiveData from co routine
                        println("location updated ${locationResult.data.latitude}")
                        _mapsState.postValue(_mapsState.value?.copy(userLocation = locationResult.data))
//                        _mapsState.value =
//                            _mapsState.value!!.copy(userLocation = locationResult.data)

                    }
                }
            }

    }
}