package com.example.projet_smartphone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.projet_smartphone.databinding.TrajectoryAddActivityBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class trajectoryAddActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: TrajectoryAddActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = TrajectoryAddActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Créer une liste pour stocker les positions des clics
        val positions : ArrayList<LatLng> = ArrayList()

        // Ajouter un écouteur pour le clic sur la carte
        mMap.setOnMapClickListener { latLng ->
            // Obtenir la latitude et la longitude du clic
            val latitude = latLng.latitude
            val longitude = latLng.longitude

            // Ajouter un marqueur à la position du clic et l'ajouter à la liste
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title("Position du clic")
                .snippet("Lat: $latitude, Lng: $longitude")

            mMap.addMarker(markerOptions)
            positions.add(latLng)

            // Mettre à jour la ligne avec les positions de tous les clics
            if(positions.size > 1){
                val lastPosition = positions[positions.size - 2]
                val line = mMap.addPolyline(
                    PolylineOptions()
                        .add(lastPosition,latLng)
                )
            }
        }

        // Définir une position de départ pour la caméra
        val coord = LatLng(46.160329,-1.151139)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(13F))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coord))
    }
}