package com.ardayucesan.marticase.map_screen.presentation

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ardayucesan.marticase.R
import com.ardayucesan.marticase.databinding.ActivityMapsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

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
class MapsActivity : AppCompatActivity() {

    // mapsViewModel koin ile inject edildi ,Singleton olarak inject edildiği için MapsFragment ile aynı viewModel instance'ı kullanmış olduk
    private val mapsViewModel: MapsViewModel by viewModel<MapsViewModel>()

    private lateinit var binding: ActivityMapsBinding
    private val REQUEST_LOCATION_PERMISSION = 1

    // LocationService üzerinden gönderilen başlangıç , bitiş verilerini dinleyen receiver
    private val serviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val data = intent?.getStringExtra("status")

            when (data) {
                LocationService.ACTION_STOP -> {
                    binding.startService.text = "Servis Başlat"
                    binding.startService.backgroundTintList = ContextCompat.getColorStateList(
                        this@MapsActivity,
                        R.color.marti_primary
                    )
                }

                LocationService.ACTION_START -> {
                    binding.startService.text = "Servis Durdur"
                    binding.startService.backgroundTintList = ContextCompat.getColorStateList(
                        this@MapsActivity,
                        R.color.marti_accent
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //statusbar rengi değiştirmek için eklendi
        val window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController

            if (controller != null) {
                controller.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
                window.statusBarColor = ContextCompat.getColor(this, R.color.marti_primary)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkRequirementsAndStartService()

        binding.startService.setOnClickListener {
            if (binding.startService.text == "Servis Başlat") {
                checkRequirementsAndStartService()
            } else {
                stopLocationService()
            }
        }

        binding.resetRoute.setOnClickListener {
            mapsViewModel.onAction(MapsAction.OnResetPolyline)
        }

        binding.clearMarkers.setOnClickListener {
            mapsViewModel.onAction(MapsAction.OnClearMarkersAndLatLngs)
        }

        lifecycleScope.launch {
            mapsViewModel.events.collect { event ->
                when (event) {
                    is MapsEvent.ShowErrorToast -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MapsActivity,
                                event.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    MapsEvent.ShowGpsDisabledDialog -> {
                        showGpsDisabledDialog()
                    }

                    MapsEvent.ShowNetworkDisabledDialog -> showNetworkDisabledDialog()
                    MapsEvent.RequestLocationPermission -> checkLocationPermission()
                }
            }
        }

        mapsViewModel.mapsState.observe(this) {
            binding.resetRoute.isEnabled = it.currentPolyline != null
        }

        val filter = IntentFilter("com.ardayucesan.marticase")
        @Suppress("UnspecifiedRegisterReceiverFlag")
        registerReceiver(
            serviceReceiver, filter,
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.map_fragment, MapsFragment())
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(serviceReceiver)
    }

    private fun checkRequirementsAndStartService() {
        when {
            !isGpsEnabled() -> {
                showGpsDisabledDialog()
            }

            !isNetworkEnabled() -> {
                showNetworkDisabledDialog()
            }

            else -> {
                checkLocationPermission()
            }
        }
    }

    private fun showGpsDisabledDialog() {
        AlertDialog.Builder(this)
            .setTitle("GPS Kapalı")
            .setMessage("Konum servisini açmak ister misiniz?")
            .setPositiveButton("Ayarlar") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("İptal", { dialog, _ ->
//                finish()
            })
            .setCancelable(false)
            .create()
            .show()
    }

    private fun showNetworkDisabledDialog() {
        AlertDialog.Builder(this)
            .setTitle("İnternet Bağlantısı")
            .setMessage("İnternet bağlantınızı kontrol etmek ister misiniz?")
            .setPositiveButton("Ayarlar") { _, _ ->
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("İptal", { dialog, _ ->
//                finish()
            })
            .setCancelable(false)
            .create()
            .show()
    }

    // Lokasyon takibinin önplan ve arkaplanda çalışmasını sağlayan lokasyon servisini açar
    private fun startLocationService() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }
    }

    // Lokasyon takibinin önplan ve arkaplanda çalışmasını sağlayan lokasyon servisini kapatır
    private fun stopLocationService() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            stopService(this)
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isGpsEnabled
    }

    private fun isNetworkEnabled(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    //Eğer izin halihazırda verilmişse tekrar sormadan viewModela
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            startLocationService()
            mapsViewModel.onAction(MapsAction.OnStartLocationTracking)
        }
    }

    //İzin diyaloğunun verilen cevaba göre LocationService başlatılır
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService()
                mapsViewModel.onAction(MapsAction.OnStartLocationTracking)
            } else {
                Toast.makeText(this, "Konum izni reddedildi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}