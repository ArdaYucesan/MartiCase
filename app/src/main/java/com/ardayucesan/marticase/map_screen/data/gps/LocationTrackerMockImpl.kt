package com.ardayucesan.marticase.map_screen.data.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.os.SystemClock
import com.ardayucesan.marticase.map_screen.data.gps.utility.mockPaths
import com.ardayucesan.marticase.map_screen.domain.LocationTracker
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import com.ardayucesan.marticase.map_screen.presentation.util.hasLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

//Öncedenb belirlenmiş bir List<LatLng> listesini mock olarak ekleyerek mock gps sinyalleri yollar
//Bu sınıf kullanılacaksa uygulama developer options üzerinden sahte gps uygulaması olarak seçilmelidir.
class LocationTrackerMockImpl(
    private val context: Context, private val client: FusedLocationProviderClient
) : LocationTracker {
    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(
        interval: Long
    ): Flow<Result<Location, GpsError>> {

        return callbackFlow {
            if (!context.hasLocationPermission()) {
                launch { send(Result.Error(GpsError.UnknownException("Missing location permission"))) }
            }

            startMockingLocations(mockPaths, 1000)

            val request = LocationRequest.Builder(1000).apply {
                setMinUpdateIntervalMillis(1000)
            }.build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        //send callback as flow
                        launch { send(Result.Success(location)) }
                    }
                }
            }

            client.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startMockingLocations(decodedPath: List<LatLng>, interval: Long) {
        client.setMockMode(true)

        client.flushLocations()

        scope.launch {
            for (latLng in decodedPath) {
                val location = Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = latLng.latitude
                    longitude = latLng.longitude
                    accuracy = 1f
                    time = System.currentTimeMillis()
                    elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                }
                setMockLocation(location)
                delay(interval)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMockLocation(location: Location) {
        client.setMockLocation(location)
            .addOnSuccessListener { println("mock location added") }
            .addOnFailureListener { e -> println("Failed to set mock location: ${e.message}") }
    }
}