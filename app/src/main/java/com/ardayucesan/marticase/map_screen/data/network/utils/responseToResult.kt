package com.ardayucesan.marticase.map_screen.data.network.utils


import com.ardayucesan.marticase.map_screen.domain.utils.NetworkError
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import com.ardayucesan.marticase.map_screen.domain.utils.Result

//Api işlemi sonucu dönen http status koduna göre sonucu mapleyen yardımcı fonksiyon
suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, NetworkError> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch (e: NoTransformationFoundException) {
                Result.Error(NetworkError.SerializationError("Serialization error check server response"))
            } catch (e: Exception) {
                Result.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
            }
        }

        408 -> Result.Error(NetworkError.RequestTimeout("Request timeout"))
        429 -> Result.Error(NetworkError.TooManyRequest("Too many requests"))

        in 400..499 -> {
            try {
//                val message = response.body<MachbeeApiError>().message

                if (response.status.value == 400) {
                    return Result.Error(NetworkError.BadRequest("Bad request"))
                } else if (response.status.value == 401) {
                    return Result.Error(NetworkError.Unauthorized("Network call unauthorized"))
                } else if (response.status.value == 405) {
                    return Result.Error(NetworkError.MethodNotAllowed("Method not allowed"))
                } else if (response.status.value == 409) {
                    return Result.Error(NetworkError.Conflict("Conflict"))
                } else {
                    return Result.Error(NetworkError.Unknown("Unknown error ocurred"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.Error(NetworkError.Unknown("Unknown error occurred"))
            }
        }

        in 500..599 -> Result.Error(NetworkError.ServerError("500 Internal Server Error"))
        else -> Result.Error(NetworkError.Unknown("Unknown error occurred"))
    }
}