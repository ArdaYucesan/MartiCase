package com.ardayucesan.marticase.map_screen.data.network.dto

import com.ardayucesan.marticase.map_screen.core.Constants
import kotlinx.serialization.Serializable

//RouteRepository üzerinden yapılacak post api çağrısı için body data transfer objesi
@Serializable
data class RouteRequestDto(
    val origin: LocationWrapper,
    val destination: LocationWrapper,
    val travelMode: String = Constants.TRAVEL_MODE,
    val routingPreference: String = Constants.ROUTING_PREFERENCE,
    val computeAlternativeRoutes: Boolean,
    val routeModifiers: RouteModifiers? = null,
    val languageCode: String = Constants.ROUTE_LANGUAGE_CODE,
    val units: String = Constants.ROUTE_UNIT
)

@Serializable
data class LocationWrapper(
    val location: LatLngWrapper
)

@Serializable
data class LatLngWrapper(
    // android gms LatLng sınıfını Serializable yapamadığım için kyllanmadım kendi dto sınıfımı yazdım
    val latLng: LatLngDto
)

@Serializable
data class LatLngDto(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class RouteModifiers(
    val avoidTolls: Boolean,
    val avoidHighways: Boolean,
    val avoidFerries: Boolean
)

