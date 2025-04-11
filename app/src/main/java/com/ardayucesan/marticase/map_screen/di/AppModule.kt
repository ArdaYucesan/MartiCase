package com.ardayucesan.marticase.map_screen.di

import com.ardayucesan.marticase.map_screen.data.gps.LocationTrackerImpl
import com.ardayucesan.marticase.map_screen.data.gps.repository_impl.LocationRepositoryImpl
import com.ardayucesan.marticase.map_screen.domain.LocationTracker
import com.ardayucesan.marticase.map_screen.domain.repository.LocationRepository
import com.ardayucesan.marticase.map_screen.domain.use_case.GetUserLocationUseCase
import com.ardayucesan.marticase.map_screen.presentation.MapsViewModel
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    //TODO : doğru bir uygulama mı androidContext injection incele


    single {
        LocationServices.getFusedLocationProviderClient(androidContext())
    }

    single {
        LocationTrackerImpl(
            context = androidContext(),
            client = get()
        )
    } bind LocationTracker::class

    single { LocationRepositoryImpl(locationTracker = get()) } bind LocationRepository::class

    single { GetUserLocationUseCase(locationRepository = get()) }

    viewModel { MapsViewModel(getUserLocationUseCase = get()) }

//    scope(named("activityScope")) {
//        viewModel { MapsViewModel(getUserLocationUseCase = get()) }
//    }
}