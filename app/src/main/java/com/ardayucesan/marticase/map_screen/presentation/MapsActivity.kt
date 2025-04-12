package com.ardayucesan.marticase.map_screen.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import kotlinx.coroutines.launch
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

    //mapsViewModel koin ile inject edildi ,Singleton olarak inject edildiği için MapsFragment ile aynı viewModel instance'ı kullanmış olduk
    private val mapsViewModel: MapsViewModel by viewModel<MapsViewModel>()

    private lateinit var binding: ActivityMapsBinding
    private val REQUEST_LOCATION_PERMISSION = 1

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

        checkLocationPermission()

        binding.startService.setOnClickListener {
            if (binding.startService.text == "Servis Başlat") {
                startLocationService()
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
                    is MapsEvent.ShowError -> {
                        Toast.makeText(
                            this@MapsActivity,
                            event.message,
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }
        }

        mapsViewModel.mapsState.observe(this) {
            binding.textView.text = it.userLocation?.latitude?.toString() ?: "No latitude available"
            binding.resetRoute.isEnabled = it.currentPolyline != null
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.map_fragment, MapsFragment())
                .commit()
        }
    }

    // Lokasyon takibinin önplan ve arkaplanda çalışmasını sağlayan lokasyon servisini açar
    private fun startLocationService() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }
        binding.startService.text = "Servis Durdur"
        binding.startService.backgroundTintList = ContextCompat.getColorStateList(
            this,
            R.color.marti_accent
        )
    }

    // Lokasyon takibinin önplan ve arkaplanda çalışmasını sağlayan lokasyon servisini kapatır
    private fun stopLocationService() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            stopService(this)
        }
        binding.startService.backgroundTintList = ContextCompat.getColorStateList(
            this,
            R.color.marti_primary
        )
        binding.startService.text = "Servis Başlat"
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