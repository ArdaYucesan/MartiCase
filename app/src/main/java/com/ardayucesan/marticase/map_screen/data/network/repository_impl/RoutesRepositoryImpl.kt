package com.ardayucesan.marticase.map_screen.data.network.repository_impl

import com.ardayucesan.marticase.BuildConfig
import com.ardayucesan.marticase.map_screen.data.network.dto.RouteRequestDto
import com.ardayucesan.marticase.map_screen.data.network.dto.RoutesResponseDto
import com.ardayucesan.marticase.map_screen.data.network.utils.constructUrl
import com.ardayucesan.marticase.map_screen.data.network.utils.safeCall
import com.ardayucesan.marticase.map_screen.domain.repository.RoutesRepository
import com.ardayucesan.marticase.map_screen.domain.utils.NetworkError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

//RoutesRepository arayüzünü implemente eden gerçek repository implementasyonu
class RoutesRepositoryImpl(
    private val httpClient: HttpClient
) : RoutesRepository {
    override suspend fun getRoutes(
        routesRequestDto: RouteRequestDto
    ): Result<RoutesResponseDto, NetworkError> {
        return safeCall<RoutesResponseDto> {

            httpClient.post {
                url(constructUrl(":computeRoutes"))
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header("X-Goog-Api-Key", BuildConfig.GCP_API_KEY)
                header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline")
                setBody(routesRequestDto)
            }
        }
    }
}