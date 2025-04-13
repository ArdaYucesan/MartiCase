package com.ardayucesan.marticase.map_screen.domain.utils


sealed interface NetworkError {
    val message: String

    data class RequestTimeout(override val message: String) : NetworkError
    data class TooManyRequest(override val message: String) : NetworkError
    data class ServerError(override val message: String) : NetworkError
    data class Unknown(override val message: String) : NetworkError
    data class SerializationError(override val message: String) : NetworkError
    data class UnresolvedAddress(override val message : String) : NetworkError
    data class BadRequest(override val message : String) : NetworkError
    data class Unauthorized(override val message : String) : NetworkError
    data class Conflict(override val message : String) : NetworkError
    data class MethodNotAllowed(override val message : String) : NetworkError
}