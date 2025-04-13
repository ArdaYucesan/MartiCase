package com.ardayucesan.marticase.map_screen.domain.use_case

import android.location.Location
import androidx.lifecycle.LiveData
import com.ardayucesan.marticase.map_screen.domain.repository.LocationRepository
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result

/* Bu case özelinde gereği olmayan bir sınıf , sadece daha büyük bir projede nasıl kullanacağımı göstermek için kullandım */

//LocationRepository inject edilerek , kullanıcı lokasyon verisini alır ve döner
class GetUserLocationUseCase(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): LiveData<Result<Location, GpsError>> {
        return locationRepository.getUserLocationUpdates()
    }
}