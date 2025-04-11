package com.ardayucesan.marticase.map_screen.domain.utils


sealed class GpsError {
    abstract val message: String

    // add more spesific error states if needed
    data class UnknownException(override val message: String) : GpsError()
}