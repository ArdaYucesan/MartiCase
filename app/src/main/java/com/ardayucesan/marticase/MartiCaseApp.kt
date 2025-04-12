package com.ardayucesan.marticase

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.ardayucesan.marticase.map_screen.core.Constants.GPS_SERVICE_NOTIFICATION_CHANNEL
import com.ardayucesan.marticase.map_screen.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Bu uygulama, Martı tarafından gönderilen case çalışması için geliştirilmiştir.
 *
 * Tek aktiviteli basit bir uygulama olmasına rağmen, daha kapsamlı projelerde kullanılabilecek
 * mimari yapıları ve tasarım desenlerini göstermek amacıyla geliştirilmiştir.
 *
 * Temel İşlevler:
 * - Kullanıcı konumunu gerçek zamanlı takip etme
 * - Google Maps entegrasyonu
 * - Google Routes API Rota hesaplama ve çizimi
 * - MVVM mimarisi kullanımı
 *
 * Not: Bu yapıların bazıları tek sayfalık bir uygulama için fazla karmaşık olabilir,
 * ancak büyük ölçekli projelerdeki kullanımlarını göstermek için eklenmiştir.
 *
 * @author Arda Yücesan
 */
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