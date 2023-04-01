package com.example.projet_smartphone

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.content.ContextCompat
import com.example.projet_smartphone.databinding.TrajectoryAddActivityBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient

class trajectoryAddActivity : AppCompatActivity(), OnMapsSdkInitializedCallback, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: TrajectoryAddActivityBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private val DEFAULT_ZOOM = 14
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

        binding = TrajectoryAddActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(ContextCompat.getDrawable(this@trajectoryAddActivity, R.drawable.background_degrade))
        }
        window.statusBarColor = Color.TRANSPARENT

    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("MyActivity", "The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> Log.d("MyActivity", "The legacy version of the renderer is used.")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        // Créer une liste pour stocker les positions des clics
        val positions : ArrayList<LatLng> = ArrayList()

        // Ajouter un écouteur pour le clic sur la carte
        mMap.setOnMapClickListener { latLng ->

            // Ajouter un marqueur à la position du clic et l'ajouter à la liste
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title("Numéro ${positions.size+1}")

            mMap.addMarker(markerOptions)
            positions.add(latLng)

            // Mettre à jour la ligne avec les positions de tous les clics
            if(positions.size > 1){
                val lastPosition = positions[positions.size - 2]

                val stampStyle = TextureStyle.newBuilder(BitmapDescriptorFactory.fromResource(android.R.drawable.arrow_down_float)).build()
                val span = StyleSpan(StrokeStyle.colorBuilder(Color.BLACK).stamp(stampStyle).build())
                mMap.addPolyline(
                    PolylineOptions()
                        .add(lastPosition,latLng)
                        .addSpan(span)
                        .width(30f)
                )
            }
        }

        // Définir une position de départ pour la caméra
        val coord = LatLng(46.160329,-1.151139)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM.toFloat()))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coord))
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}