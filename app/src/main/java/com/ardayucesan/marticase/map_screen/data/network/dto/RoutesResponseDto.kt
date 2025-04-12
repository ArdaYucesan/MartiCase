package com.ardayucesan.marticase.map_screen.data.network.dto

import kotlinx.serialization.Serializable

//RouteRepository üzerinden yapılacak post api çağrısı için body data transfer objesi
@Serializable
data class RoutesResponseDto(
    val routes: List<Route>
)

@Serializable
data class Route(
    val distanceMeters: Int,
    val duration: String,
    val polyline: Polyline
)

@Serializable
data class Polyline(
    val encodedPolyline: String
)