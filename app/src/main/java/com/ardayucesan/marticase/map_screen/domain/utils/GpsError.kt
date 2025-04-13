package com.ardayucesan.marticase.map_screen.domain.utils

sealed interface GpsError {
    val message: String

    data class UnknownException(override val message: String) : GpsError

    data class MissingLocationPermission(override val message: String) : GpsError

    data class GpsDisabled(override val message: String) : GpsError

    data class NetworkDisabled(override val message: String) : GpsError
}