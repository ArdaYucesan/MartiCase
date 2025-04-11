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
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class LocationTrackerImpl(
    private val context: Context, private val client: FusedLocationProviderClient
) : LocationTracker {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Result<Location, GpsError>> {

        //returns callback results as flows
        return callbackFlow {
            if (!context.hasLocationPermission()) {
                //TODO : add new errors for this to GpsError sealed classes
                launch { send(Result.Error(GpsError.UnknownException("Missing location permission"))) }
            }

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGpsEnabled && !isNetworkEnabled) {
                launch { send(Result.Error(GpsError.UnknownException("Gps is disabled"))) }
            }

            client.lastLocation
                .addOnSuccessListener { lastLocation ->

                    launch { send(Result.Success(lastLocation)) }
                }.addOnFailureListener {
                    // UnknownLastLocation error
                    launch { send(Result.Error(GpsError.UnknownException("No last known location"))) }
                }


            //imported as android.gms.location LocationRequest otherwise gives error on requestLocationUpdates function
            val request = LocationRequest.Builder(interval).apply {}.build()

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

            //when launched co routine closed remove the location updates as well
            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}

