package com.ardayucesan.marticase.map_screen.domain.utils

// type-safe hata koruması için Result wrapper
sealed interface Result<out T, out E> {
    data class Success<out T>(val data: T) : Result<T, Nothing>
    data class Error<out E>(val error: E) : Result<Nothing, E>
}