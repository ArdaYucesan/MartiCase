package com.ardayucesan.marticase.map_screen.presentation

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.ardayucesan.marticase.R
import com.ardayucesan.marticase.map_screen.core.Constants.GPS_SERVICE_NOTIFICATION_CHANNEL
import com.ardayucesan.marticase.map_screen.domain.LocationTracker
import com.ardayucesan.marticase.map_screen.domain.repository.LocationRepository
import com.ardayucesan.marticase.map_screen.domain.utils.GpsError
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import com.ardayucesan.marticase.map_screen.presentation.util.hasLocationPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class LocationService : Service() {

    // LocationTracker üzerinden gelen flow'un collect edileceği coroutine scope , servis kapatıldığında cancel edilip LocationTracker üzerinden gelen veri akışı durdurulur
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val locationTracker: LocationTracker by inject()
    private val locationRepository: LocationRepository by inject()

    // Konum bilgisini dinler ve kapatıldığında kullanıcıya Toast bildirimi gönderir ve servisi kapatır
    private val locationListener = object : android.location.LocationListener {
        override fun onProviderEnabled(provider: String) {
        }

        override fun onProviderDisabled(provider: String) {
            serviceScope.launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LocationService,
                        "$provider kapalı! Servis durdurulacak.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                stop()
            }
        }

        override fun onLocationChanged(location: android.location.Location) {
            // FusedLocationProvider client kullandığım için boş kalacak
        }
    }

    // bu servis bağlı çalışmayacağı için null dönüyor
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("when is this calling")
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()

        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Servisi başlatır , servisin çalıştığını gösteren bildirimi oluşturur ve gönderir
     * LocationTracker sınıfı ile lokasyon verisini alır
     * LocationRepository ile alınan lokasyon verisini günceller, verinin uygulamanın başka alanlarında önplanda ve arkaplanda erişilebilir olmasını sağlar
     */
    @SuppressLint("MissingPermission")
    private fun start() {

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // sadece konum bilgisi açık mı kapalı mı kontrolü için kullanıldı
        if (this.hasLocationPermission()) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                1f,
                locationListener
            )
        }

        sendServiceStartedBroadcast()

        val notification = NotificationCompat.Builder(this, GPS_SERVICE_NOTIFICATION_CHANNEL)
            .setContentTitle("Tracking location...")
            .setContentText("Location: not found")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //side effectler için onEach yazıldı ve ardından launchIn kullanarak verilen scope içinde collect yapıldı
        locationTracker
            .getLocationUpdates(500)
            .catch { e ->
                e.printStackTrace()
                notificationManager.cancelAll()
            }
            .onEach { result ->
                locationRepository.updateUserLocations(result)

                when (result) {
                    is Result.Success -> {
                        val location = result.data
                        notification.setContentText("Koordinat: (${location.latitude}, ${location.longitude})")
                    }

                    is Result.Error -> {

                        when (result.error) {

                            is GpsError.GpsDisabled -> {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@LocationService,
                                        "Konum bilgisi bulunamadı. Servis 30 saniye içinde durduralacak",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                notification.setContentText("GPS kapalı - Servis duracak")
                                stop()
                            }

                            is GpsError.NetworkDisabled -> {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@LocationService,
                                        "İnternet bağlantısı kesildi. Servis 30 saniye içinde durduralacak",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                notification.setContentText("İnternet yok - Servis duracak")
                                stop()
                            }

                            else -> {
                                notification.setContentText("Error: ${result.error.message}")
                            }
                        }
                    }
                }
                notificationManager.notify(1, notification.build())

            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    //Servisi durdurur , servisin çalıştığı co routine kapatılınca LocationTracker üzerinden gelen veri akışı da kesilir.
    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun sendServiceStartedBroadcast() {
        val broadcastIntent = Intent("com.ardayucesan.marticase")
        broadcastIntent.putExtra("status", ACTION_START)
        sendBroadcast(broadcastIntent)
    }

    private fun sendServiceStoppedBroadcast() {
        val broadcastIntent = Intent("com.ardayucesan.marticase")
        broadcastIntent.putExtra("status", ACTION_STOP)

        // Öncelikli olarak broadcast gönder
        sendBroadcast(broadcastIntent)
    }

    override fun onDestroy() {
        super.onDestroy()

        sendServiceStoppedBroadcast()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}