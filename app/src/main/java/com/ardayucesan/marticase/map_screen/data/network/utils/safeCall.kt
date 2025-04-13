package com.ardayucesan.marticase.map_screen.data.network.utils

import com.ardayucesan.marticase.map_screen.domain.utils.NetworkError
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlinx.coroutines.DelicateCoroutinesApi
import com.ardayucesan.marticase.map_screen.domain.utils.Result

// parametre olarak aldığı fonksiyonu try-catch bloğunda çalıştırır , hata yoksa responseToResult fonksiyonuna iletir
@OptIn(DelicateCoroutinesApi::class)
suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, NetworkError> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        return Result.Error(
            NetworkError.UnresolvedAddress(
                e.message ?: "Unresolved address exception"
            )
        )
    } catch (e: SerializationException) {
        return Result.Error(
            NetworkError.SerializationError(
                e.message ?: "Serialization error exception"
            )
        )
    } catch (e: Exception) {
        //TODO: bunu tekrardan incele detaylarına bak
        println("genel exception içindeyim")
        e.printStackTrace()

        //co routine iptal edildiyse CancellationException fırlatır kodu devam ettirmez
        coroutineContext.ensureActive()

        return Result.Error(NetworkError.Unknown(e.message ?: "Unknown error exception"))
    }

    return responseToResult(response)

}