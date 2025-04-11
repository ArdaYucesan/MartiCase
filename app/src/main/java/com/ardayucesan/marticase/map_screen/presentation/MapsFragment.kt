package com.ardayucesan.marticase.map_screen.presentation

import android.content.Context
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.ardayucesan.marticase.R
import com.ardayucesan.marticase.databinding.FragmentMapsBinding
import com.ardayucesan.marticase.map_screen.domain.UserLocation

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class MapsFragment : Fragment() {

    private val mapsViewModel: MapsViewModel by activityViewModel<MapsViewModel>()
    private lateinit var binding: FragmentMapsBinding
    private var drawedPolyline: Polyline? = null
    private var destinationMarker: Marker? = null
//    private val stepMarkers: MutableList<Marker> = mutableListOf()
//    private val stepLatLngs = mutableListOf<LatLng>()

    private var userMarker: Marker? = null

    private val callback = OnMapReadyCallback { googleMap ->

        mapsViewModel.mapsState.observeForever { mapsState ->

            mapsState.currentPolyline?.encodedPolyline.let { encodedPolyline ->
                if (encodedPolyline != null) {
                    drawPolylineOnMap(googleMap, encodedPolyline)
                } else {
                    clearPolyline()
                }
            }

            val latLngState = mapsState.userLocation?.let { location ->
                LatLng(
                    location.latitude,
                    location.longitude
                )
            }

            val stepLatLngList = mapsState.stepLatLng
            val stepMarkerList = mapsState.stepMarker

            latLngState?.let { latLng ->
                if (userMarker == null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    userMarker =
                        googleMap.addMarker(
                            MarkerOptions().position(latLng).title("Marker in User")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )
                }

                if (stepLatLngList.isEmpty() && stepMarkerList.isEmpty()) {
                    val startingMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Starting Marker")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    )

                    mapsViewModel.onAction(MapsAction.OnNewStepAdded(latLng, startingMarker))
                }

                userMarker?.position = latLng
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))

                val lastStep = stepLatLngList.lastOrNull()

                if (lastStep != null && SphericalUtil.computeDistanceBetween(
                        lastStep,
                        latLng
                    ) >= 100
                ) {
                    // Yeni step ekle
                    val newMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("100m Step")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )

                    newMarker?.let { newMarker ->
//                        stepMarkers.add(it)
                        mapsViewModel.onAction(MapsAction.OnNewStepAdded(latLng, newMarker))
                    }
//                    stepLatLngs.add(latLng) // Marker'ın pozisyonunu kopya olarak al
                }
            }
        }

        googleMap.setOnMapClickListener { latLng ->
            // latLng.latitude ve latLng.longitude ile konuma erişebilirsin
            println("Tıklanan konum: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
            destinationMarker?.remove()
            // Örnek: Marker eklemek istersen
            destinationMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Seçilen Konum")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )

            mapsViewModel.onAction(
                MapsAction.OnCreateRoute(
                    destination = UserLocation(
                        latLng.latitude,
                        latLng.longitude,
                        System.currentTimeMillis()
                    ),
                )
            )
        }
    }

//    private fun simulateUserWalking(googleMap: GoogleMap, encodedPolyline: String) {
//        stepMarkers.forEach { it.remove() }
//        userMarkerSimulation?.remove()
//
//        val decodedPath = PolyUtil.decode(encodedPolyline)
//
//        println("decoded path $decodedPath")
//
//        userMarkerSimulation = googleMap.addMarker(
//            MarkerOptions()
//                .position(decodedPath.first())
//                .title("User is walking")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//        )
//
//        lifecycleScope.launch {
//            var distanceAccumulator = 0.0 // metrede biriktireceğiz
//
//            for (i in 1 until decodedPath.size) {
//                val start = decodedPath[i - 1]
//                val end = decodedPath[i]
//
//                // Kullanıcıyı ilerlet
//                userMarkerSimulation?.position = end
//                googleMap.animateCamera(CameraUpdateFactory.newLatLng(end))
//
//                // Bu adımda alınan mesafeyi hesapla
//                val stepDistance =
//                    SphericalUtil.computeDistanceBetween(start, end) // metre cinsinden
//
//                distanceAccumulator += stepDistance
//
//                // Eğer biriken mesafe 100 metreyi geçtiyse marker bırak
//                if (distanceAccumulator >= 100.0) {
//                    val marker: Marker? = googleMap.addMarker(
//                        MarkerOptions()
//                            .position(end)
//                            .title("100m point")
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
//                    )
//                    if (marker != null) {
//                        stepMarkers.add(marker)
//                    }
//                    distanceAccumulator = 0.0 // sıfırla
//                }
//
//                delay(500L) // yürüyüş hızı gibi
//            }
//        }
//    }

    private fun drawPolylineOnMap(googleMap: GoogleMap, encodedPolyline: String) {
        drawedPolyline?.remove()
        val decodedPath: List<LatLng> = PolyUtil.decode(encodedPolyline)

        val polylineOptions = PolylineOptions()
            .addAll(decodedPath)
            .color(Color.parseColor("#33D101")) // Renk isteğe bağlı
            .width(8f)

        drawedPolyline = googleMap.addPolyline(polylineOptions)
    }

    private fun clearPolyline() {
        // Clear the polyline from the map
        drawedPolyline?.remove()
        destinationMarker?.remove()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        mapsViewModel.onAction(MapsAction.OnStartLocationTrackerClicked)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


}