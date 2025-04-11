package com.ardayucesan.marticase.map_screen.data.gps

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ardayucesan.marticase.R
import com.ardayucesan.marticase.map_screen.domain.LocationTracker
import com.ardayucesan.marticase.map_screen.domain.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

class LocationService : Service() {

    //why service scope look into that again
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val locationTracker: LocationTracker by inject()

    //no need to bind
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // used onEach for side effects and then used launchIn as shorthand of collect
        locationTracker
            .getLocationUpdates(5000L)
            .catch { e ->
                e.printStackTrace()

                notificationManager.cancelAll()
            }
            .onEach { locationResult ->

                val updatedNotification = notification.setContentText(
                    when (locationResult) {
                        is Result.Success -> "Location: (${locationResult.data.latitude}, ${locationResult.data.longitude})"
                        is Result.Error -> "Service Error: ${locationResult.error.message}"
                    }
                )
                notificationManager.notify(1, updatedNotification.build())
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}