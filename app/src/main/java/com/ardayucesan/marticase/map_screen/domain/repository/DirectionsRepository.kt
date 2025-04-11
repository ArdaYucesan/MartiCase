package com.ardayucesan.marticase.map_screen.domain.repository

import com.ardayucesan.marticase.map_screen.data.network.dto.GetDirectionsDTO
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import com.google.android.gms.maps.model.LatLng

interface DirectionsRepository {
    //TODO : change error class
    suspend fun getDirections(origin: LatLng, dest: LatLng): Result<GetDirectionsDTO, GpsError>
}