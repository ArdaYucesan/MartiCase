package com.ardayucesan.marticase.map_screen.presentation

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardayucesan.marticase.map_screen.domain.AppLocation
import com.ardayucesan.marticase.map_screen.domain.use_case.GetRoutesUseCase
import com.ardayucesan.marticase.map_screen.domain.use_case.GetUserLocationUseCase
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import com.ardayucesan.marticase.map_screen.domain.utils.toAppLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Kullanıcının konumunu dinleyip, harita üzerindeki marker, rota ve adımları yöneten
 * ViewModel katmanıdır.
 *
 * Uygulamanın lifecycle'ına uygun olarak, kullanıcı konumunu sürekli olarak güncel
 * tutar ve rota hesaplama, marker yönetimi gibi işlemleri koordine eder.
 *
 * @author Arda Yücesan
 */
class MapsViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getRoutesUseCase: GetRoutesUseCase
) : ViewModel() {
    // Harita ekranının state yapısını temsil eder.
    private val _mapsState = MutableLiveData(MapsState())
    val mapsState: LiveData<MapsState> = _mapsState

    //ViewModeldan tetiklenmek istenen arayüz eventlerini temsil eder
    private val _events = Channel<MapsEvent>()
    val events = _events.receiveAsFlow()

    // Konum değişimlerini dinleyen observer.
    private val locationObserver = Observer<Result<Location, GpsError>> { gpsResult ->

        when (gpsResult) {
            is Result.Error -> {
                viewModelScope.launch {
                    when (gpsResult.error) {
                        is GpsError.GpsDisabled -> _events.send(MapsEvent.ShowGpsDisabledDialog)
                        is GpsError.MissingLocationPermission -> _events.send(MapsEvent.RequestLocationPermission)
                        is GpsError.NetworkDisabled -> _events.send(MapsEvent.ShowNetworkDisabledDialog)
                        is GpsError.UnknownException -> _events.send(
                            MapsEvent.ShowErrorToast(
                                gpsResult.error.message
                            )
                        )
                    }
                }
            }

            is Result.Success -> {
                _mapsState.postValue(
                    _mapsState.value?.copy(userLocation = gpsResult.data.toAppLocation())
                )
            }
        }
    }

    /**
     * ViewModel'de kullanıcı aksiyonlarına göre ilgili işlevleri yönlendiren ana kontrol noktası.
     * Kullanıcıdan gelen etkileşimleri (buton tıklamaları vb.) handle eder.
     */
    fun onAction(action: MapsAction) {
        when (action) {
            is MapsAction.OnStartLocationTracking -> {
                viewModelScope.launch {
                    startLocationTracking()
                }
            }

            is MapsAction.OnCreateRoute -> {
                viewModelScope.launch {
                    getRoutes(action.destination)
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

    /**
     * Belirtilen hedefe rota hesaplamak için use-case'i tetikler.
     * Rota başarıyla dönerse state güncellenir.
     */
    private suspend fun getRoutes(destination: AppLocation) {
        _mapsState.value?.userLocation?.let { userLocation ->
            when (val result = getRoutesUseCase(origin = userLocation, destination = destination)) {
                is Result.Error -> {
                    println("api error")
                    _events.send(MapsEvent.ShowErrorToast(result.error.message))
                }

                is Result.Success -> {
                    _mapsState.postValue(
                        _mapsState.value?.copy(
                            currentPolyline = result.data,
                        )
                    )
                }
            }
        }
    }

    //Harita üzerindeki mevcut polyline bilgisini sıfırlar.
    private fun clearPolyline() {
        _mapsState.value = _mapsState.value?.copy(currentPolyline = null)
    }

    //Haritada gösterilen adım markerlarını ve lokasyon listesini temizler.
    private fun clearMarkersAndLatLngs() {
        _mapsState.value?.stepMarker?.forEach { marker ->
            marker.remove()
        }

        _mapsState.value = _mapsState.value?.copy(
            stepLatLng = emptyList(),
            stepMarker = emptyList()
        )
    }

    //Adımlar için yeni LatLng bilgisini mevcut state'e ekler.
    private fun addStepLatLngs(latLng: LatLng) {
        _mapsState.value?.let { currentState ->
            _mapsState.value = currentState.copy(stepLatLng = currentState.stepLatLng + latLng)
        }
    }

    //Adım markerlarını state'e ekler.
    private fun addStepMarkers(marker: Marker) {
        _mapsState.value?.let { currentState ->
            // Yeni marker, mevcut listenin sonuna eklenir.
            _mapsState.value = currentState.copy(stepMarker = currentState.stepMarker + marker)
        }
    }

    //Kullanıcı konumunu sürekli takip etmek için observer kaydı yapılır.
    private fun startLocationTracking() {
        getUserLocationUseCase().observeForever(locationObserver)
    }

    //ViewModel yaşam döngüsü sona erdiğinde observer kaydı temizlenir.
    override fun onCleared() {
        super.onCleared()
        getUserLocationUseCase().removeObserver(locationObserver)
    }
}
