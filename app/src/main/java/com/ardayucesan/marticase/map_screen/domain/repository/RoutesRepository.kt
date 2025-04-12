package com.ardayucesan.marticase.map_screen.domain.repository

import com.ardayucesan.marticase.map_screen.data.network.dto.RouteRequestDto
import com.ardayucesan.marticase.map_screen.data.network.dto.RoutesResponseDto
import com.ardayucesan.marticase.map_screen.domain.utils.NetworkError
import com.ardayucesan.marticase.map_screen.domain.utils.Result

interface RoutesRepository {
    suspend fun getRoutes(routesRequestDto: RouteRequestDto): Result<RoutesResponseDto, NetworkError>
}