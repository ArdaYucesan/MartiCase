package com.ardayucesan.marticase.map_screen.presentation

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ardayucesan.marticase.R
import com.ardayucesan.marticase.databinding.FragmentMapsBinding
import com.ardayucesan.marticase.map_screen.domain.AppLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.IOException
import java.util.Locale

class MapsFragment : Fragment() {

    // ViewModel'i Koin üzerinden inject edildi, fragment'ın yaşam döngüsüne bağlı.
    private val mapsViewModel: MapsViewModel by activityViewModel<MapsViewModel>()

    private lateinit var binding: FragmentMapsBinding

    // Haritadaki aktif polyline referansı
    private var drawedPolyline: Polyline? = null

    // Hedef ve kullanıcı marker referansları
    private var destinationMarker: Marker? = null
    private var userMarker: Marker? = null

    // Adres çözümlemek için geocoder
    private lateinit var geocoder: Geocoder

    private val zoom: Float = 15f

    // Harita hazır olduğunda çalışacak callback
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        // ViewModel'deki tetiklenen state değişimleri izleniyor
        mapsViewModel.mapsState.observeForever { mapsState ->

            // Eğer polyline bilgisi varsa haritada çizilir
            mapsState.currentPolyline?.encodedPolyline.let { encodedPolyline ->
                handlePolyline(googleMap, encodedPolyline)
            }

            // Kullanıcının mevcut konumunu alıp LatLng sınıfına çeviriyor
            val currentLatLng = mapsState.userLocation?.let { location ->
                LatLng(location.latitude, location.longitude)
            }

            currentLatLng?.let { latLng ->

                //eğer userMarker yoksa oluşturulur
                if (userMarker == null) {
                    createUserMarker(googleMap, latLng)
                }
                // Eğer step marker , step latlng listeleri boşsa başlangıç marker'ını oluşturulur / İşaretler temizlenirse tekrar oluşturulur
                if (mapsState.stepLatLng.isEmpty() && mapsState.stepMarker.isEmpty()) {
                    createStartingMarker(googleMap, latLng)
                }
                // Mevcut konuma göre kullanıcı marker güncellenir
                updateMapCamera(googleMap, latLng, userMarker)

                // 100 metrede bir step marker ekler
                calculateStep(googleMap, mapsState.stepLatLng.lastOrNull(), latLng)
            }
        }

        // Haritada herhangi bir yere tıklanınca hedef marker oluştur
        googleMap.setOnMapClickListener { latLng ->
            createDestinationMarker(googleMap, latLng)
        }
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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        geocoder = Geocoder(requireContext(), Locale.getDefault())

        mapFragment?.getMapAsync(callback)
    }

    // İki nokta arasındaki mesafeyi hesaplayıp, 100 metreyi geçerse yeni bir step marker'ı oluşturur
    private fun calculateStep(googleMap: GoogleMap, lastLatLng: LatLng?, currentLatLng: LatLng) {
        if (lastLatLng != null && SphericalUtil.computeDistanceBetween(
                lastLatLng,
                currentLatLng
            ) >= 100
        ) {
            createStepMarker(googleMap, currentLatLng)
        }
    }

    // Kullanıcı marker'ını günceller ve harita kamerasını hareket ettirir
    private fun updateMapCamera(googleMap: GoogleMap, latLng: LatLng, userMarker: Marker?) {
        userMarker?.position = latLng
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    // kullanıcı marker'ı oluşturur ve haritayı bu noktaya zoom yapar
    private fun createUserMarker(googleMap: GoogleMap, latLng: LatLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        userMarker = googleMap.addMarker(
            MarkerOptions().position(latLng)
                .title("User")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
    }

    // Yeni bir adım marker'ı bırakır, ViewModel'e bildirir
    private fun createStepMarker(googleMap: GoogleMap, latLng: LatLng) {
        addMarkerWithAddress(
            googleMap = googleMap,
            latLng = latLng,
            markerHue = BitmapDescriptorFactory.HUE_AZURE,
            notFoundTitle = "100m Step"
        ) { marker ->
            mapsViewModel.onAction(MapsAction.OnNewStepAdded(latLng, marker))
        }
    }

    // Hedef konumu haritada işaretler ve rota hesaplaması için ViewModel'e bildirir
    private fun createDestinationMarker(googleMap: GoogleMap, latLng: LatLng) {
//        println("Tıklanan konum: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
        destinationMarker?.remove()
        destinationMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Seçilen Konum")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        mapsViewModel.onAction(
            MapsAction.OnCreateRoute(
                destination = AppLocation(
                    latLng.latitude,
                    latLng.longitude,
                    System.currentTimeMillis()
                )
            )
        )
    }

    // Başlangıç noktasında marker oluşturur, step olarak kaydeder
    private fun createStartingMarker(googleMap: GoogleMap, latLng: LatLng) {
        addMarkerWithAddress(
            googleMap = googleMap,
            latLng = latLng,
            markerHue = BitmapDescriptorFactory.HUE_ORANGE
        ) { marker ->
            mapsViewModel.onAction(MapsAction.OnNewStepAdded(latLng, marker))
        }
    }

    // Adres bilgisini almak için marker ekleyen yardımcı metod , geocoder sınıfı api 33 ve üzerinde callback fonksiyonu ile çalışıyor
    private fun addMarkerWithAddress(
        googleMap: GoogleMap,
        latLng: LatLng,
        markerHue: Float,
        notFoundTitle: String = "Address not found",
        onMarkerAdded: (Marker?) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val addressTitle =
                            addresses.firstOrNull()?.getAddressLine(0) ?: notFoundTitle
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(latLng).title(addressTitle)
                                .icon(BitmapDescriptorFactory.defaultMarker(markerHue))
                        )
                        onMarkerAdded(marker)
                    }

                    override fun onError(errorMessage: String?) {
                        super.onError(errorMessage)
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(latLng).title(notFoundTitle)
                                .icon(BitmapDescriptorFactory.defaultMarker(markerHue))
                        )
                        onMarkerAdded(marker)
                    }
                }
            )
        } else {

            lifecycleScope.launch(Dispatchers.IO) {

                val addressTitle = try {
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    addresses?.firstOrNull()?.getAddressLine(0) ?: "Adres bulunamadı"
                } catch (e: IOException) {
                    e.printStackTrace()
                    "Adres alınamadı"
                }

                withContext(Dispatchers.Main) {
                    val marker = googleMap.addMarker(
                        MarkerOptions().position(latLng).title(addressTitle)
                            .icon(BitmapDescriptorFactory.defaultMarker(markerHue))
                    )
                    onMarkerAdded(marker)
                }
            }
        }
    }

    // Polyline verisini kontrol eder, mevcutsa haritada çizer yoksa temizler
    private fun handlePolyline(googleMap: GoogleMap, encodedPolyline: String?) {
        if (encodedPolyline != null) {
            drawPolylineOnMap(googleMap, encodedPolyline)
        } else {
            clearPolyline()
        }
    }

    // Encoded polyline verisini decode edip haritada çizer
    private fun drawPolylineOnMap(googleMap: GoogleMap, encodedPolyline: String) {
        drawedPolyline?.remove()
        val decodedPath: List<LatLng> = PolyUtil.decode(encodedPolyline)
        val polylineOptions =
            PolylineOptions().addAll(decodedPath).color(Color.parseColor("#33D101")).width(8f)
        drawedPolyline = googleMap.addPolyline(polylineOptions)
    }

    // Polyline ve hedef marker'ını temizler
    private fun clearPolyline() {
        drawedPolyline?.remove()
        destinationMarker?.remove()
    }
}