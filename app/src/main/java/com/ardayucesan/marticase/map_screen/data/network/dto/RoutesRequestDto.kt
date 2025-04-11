package com.ardayucesan.marticase.map_screen.data.network.dto

import com.ardayucesan.marticase.map_screen.core.Constants
import kotlinx.serialization.Serializable

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
    // not used libraries LatLng because of serialization error
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

