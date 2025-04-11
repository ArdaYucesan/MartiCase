package com.ardayucesan.marticase.map_screen.presentation

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ardayucesan.marticase.R
import android.Manifest
import android.content.Intent
import android.widget.Toast
import com.ardayucesan.marticase.databinding.ActivityMapsBinding
import com.ardayucesan.marticase.map_screen.data.gps.LocationService
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapsActivity : AppCompatActivity() {

//    private val mapsViewModel: MapsViewModel by viewModel()

    private val mapsViewModel: MapsViewModel by viewModel<MapsViewModel>()

    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        mapsViewModel.updateTest()

        mapsViewModel.mapsState.observe(this) {
            binding.textView.text = it.userLocation?.latitude?.toString() ?: "No latitude available"
        }

        binding.startService.setOnClickListener {
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                startService(this)
            }
        }

        checkLocationPermission()

//        Intent(applicationContext, LocationService::class.java).apply {
//            action = LocationService.ACTION_START
//            startService(this)
//        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.map_fragment, MapsFragment())
                .commit()
        }
    }

    private val REQUEST_LOCATION_PERMISSION = 1

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
            // i didnt call viewModel.getUserLocation instead created an action
            mapsViewModel.onAction(MapsAction.OnStartLocationTrackerClicked)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapsViewModel.onAction(MapsAction.OnStartLocationTrackerClicked)
            } else {
                Toast.makeText(this, "Konum izni reddedildi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}