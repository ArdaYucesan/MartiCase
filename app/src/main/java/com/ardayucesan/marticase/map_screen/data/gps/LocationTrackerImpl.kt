package com.ardayucesan.marticase.map_screen.data.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.ardayucesan.marticase.map_screen.domain.LocationTracker
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import com.ardayucesan.marticase.map_screen.presentation.util.hasLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class LocationTrackerImpl(
    private val context: Context, private val client: FusedLocationProviderClient
) : LocationTracker {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(
        interval: Long
    ): Flow<Result<Location, GpsError>> {

        //callbackFlow , aslında flow ile çok benzer fakat callback tabanlı veriler için daha uygun , emit() suspend fonksiyonu olduğu için direkt callback içinde kullanamazdım callbackFlow bu avantajı saüğlıyor
        return callbackFlow {
            if (!context.hasLocationPermission()) {
                launch { send(Result.Error(GpsError.MissingLocationPermission("Missing location permission"))) }
            }

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled) {
                launch { send(Result.Error(GpsError.GpsDisabled("Gps is disabled"))) }
            }

            if (!isNetworkEnabled) {
                launch { send(Result.Error(GpsError.NetworkDisabled("Network is disabled"))) }
            }

            val request = LocationRequest.Builder(500).apply {
                setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                setMinUpdateIntervalMillis(500)
            }.build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        //callbackten gelen veriyi callBackFlow sayesinde uyumlu hale
                        launch { send(Result.Success(location)) }
                    }
                }
            }

            client.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper()
            )

            //bağlı olan co routine kapandığında callbacki silip işlem tamamlanır
            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}

