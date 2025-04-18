package com.ardayucesan.marticase.map_screen.domain

import com.ardayucesan.marticase.map_screen.data.network.dto.LatLngDto
import com.ardayucesan.marticase.map_screen.data.network.dto.LatLngWrapper
import com.ardayucesan.marticase.map_screen.data.network.dto.LocationWrapper
import com.google.android.gms.maps.model.LatLng

data class AppLocation(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
) {
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    fun toLocationWrapper(): LocationWrapper {
        return LocationWrapper(
            location = LatLngWrapper(
                latLng = LatLngDto(
                    latitude = this.latitude,
                    longitude = this.longitude
                )
            )
        )
    }
}
