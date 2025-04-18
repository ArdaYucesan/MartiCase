package com.ardayucesan.marticase.map_screen.di

import com.ardayucesan.marticase.map_screen.data.gps.LocationTrackerImpl
import com.ardayucesan.marticase.map_screen.data.gps.repository_impl.LocationRepositoryImpl
import com.ardayucesan.marticase.map_screen.data.network.HttpClientFactory
import com.ardayucesan.marticase.map_screen.data.network.repository_impl.RoutesRepositoryImpl
import com.ardayucesan.marticase.map_screen.domain.LocationTracker
import com.ardayucesan.marticase.map_screen.domain.repository.LocationRepository
import com.ardayucesan.marticase.map_screen.domain.repository.RoutesRepository
import com.ardayucesan.marticase.map_screen.domain.use_case.GetRoutesUseCase
import com.ardayucesan.marticase.map_screen.domain.use_case.GetUserLocationUseCase
import com.ardayucesan.marticase.map_screen.presentation.MapsViewModel
import com.google.android.gms.location.LocationServices
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

// koine sağlanacak moddül , burada tanımlanan bağımlılıklar çağrıldıkları yerde inject edilebilir hale gelirler
val appModule = module {

    single { HttpClientFactory.create(OkHttp.create()) }

    single {
        LocationServices.getFusedLocationProviderClient(androidContext())
    }

    single {
        LocationTrackerImpl(
            context = androidContext(),
            client = get()
        )
    } bind LocationTracker::class

    single { LocationRepositoryImpl() } bind LocationRepository::class
    single { RoutesRepositoryImpl(httpClient = get()) } bind RoutesRepository::class

    single { GetUserLocationUseCase(locationRepository = get()) }
    single { GetRoutesUseCase(routesRepository = get()) }

    viewModel { MapsViewModel(getUserLocationUseCase = get(), getRoutesUseCase = get()) }

}