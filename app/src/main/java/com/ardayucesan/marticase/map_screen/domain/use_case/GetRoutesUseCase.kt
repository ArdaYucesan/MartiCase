package com.ardayucesan.marticase.map_screen.domain.use_case

import com.ardayucesan.marticase.map_screen.data.network.dto.Polyline
import com.ardayucesan.marticase.map_screen.data.network.dto.RouteRequestDto
import com.ardayucesan.marticase.map_screen.domain.AppLocation
import com.ardayucesan.marticase.map_screen.domain.repository.RoutesRepository
import com.ardayucesan.marticase.map_screen.domain.utils.NetworkError
import com.ardayucesan.marticase.map_screen.domain.utils.Result

/* Bu case özelinde gereği olmayan bir sınıf , sadece daha büyük bir projede nasıl kullanacağımı göstermek için kullandım */

//routesRepository inject edilerek , verilen origin ve destination bilgisine göre rota verisi alır ve döner
class GetRoutesUseCase(
    private val routesRepository: RoutesRepository
) {
    suspend operator fun invoke(
        origin: AppLocation,
        destination: AppLocation
    ): Result<Polyline, NetworkError> {
        val result = routesRepository.getRoutes(
            RouteRequestDto(
                origin = origin.toLocationWrapper(),
                destination = destination.toLocationWrapper(),
                computeAlternativeRoutes = false,
            )
        )

        return when (result) {
            is Result.Error -> {
                Result.Error(result.error)
            }

            is Result.Success -> {
                Result.Success(result.data.routes[0].polyline)
            }
        }
    }
}