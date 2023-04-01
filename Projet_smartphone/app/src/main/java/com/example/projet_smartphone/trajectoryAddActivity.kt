package com.example.projet_smartphone

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import androidx.core.content.ContextCompat
import com.example.projet_smartphone.databinding.TrajectoryAddActivityBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class trajectoryAddActivity : AppCompatActivity(), OnMapsSdkInitializedCallback, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: TrajectoryAddActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

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

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("MyActivity", "The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> Log.d("MyActivity", "The legacy version of the renderer is used.")
        }
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
        mMap.moveCamera(CameraUpdateFactory.zoomTo(13F))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coord))
    }
}