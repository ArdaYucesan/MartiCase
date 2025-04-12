package com.ardayucesan.marticase

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ardayucesan.marticase.map_screen.core.Constants.GPS_SERVICE_NOTIFICATION_CHANNEL
import com.ardayucesan.marticase.map_screen.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MartiCaseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            GPS_SERVICE_NOTIFICATION_CHANNEL,
            "Location",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        startKoin {
            androidContext(this@MartiCaseApp)
            modules(appModule)
        }
    }
}