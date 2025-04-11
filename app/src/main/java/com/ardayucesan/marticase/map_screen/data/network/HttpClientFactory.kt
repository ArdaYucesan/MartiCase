package com.ardayucesan.marticase.map_screen.data.network

import android.util.Log
import com.ardayucesan.marticase.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(Logging) {
                if (BuildConfig.DEBUG) {
                    level = LogLevel.ALL
                    logger = Logger.ANDROID
                }
            }
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
//            install(HttpTimeout) {
//                requestTimeoutMillis = 20_000 // İsteğin sonsuz süre beklemesini sağlar
//                connectTimeoutMillis = 15_000 // Bağlantı için maksimum bekleme süresi (60 saniye)
//                socketTimeoutMillis = 15_000
//            }
            install(DefaultRequest) {
                header("accept", "application/json")
                contentType(Json.withParameter("charset", "utf-8"))
//                header("authorization", "Bearer sk-618fa212823d4b81994d00e77007d453")
            }
            defaultRequest {
                contentType(Json)
            }
        }
    }
}