package com.example.projet_smartphone

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projet_smartphone.databinding.TrajectoryAddActivityBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.*
import java.io.*

class trajectoryAddActivity : AppCompatActivity(), OnMapsSdkInitializedCallback, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: TrajectoryAddActivityBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val positions : ArrayList<Marker> = ArrayList()
    private val polylines : ArrayList<Polyline> = ArrayList()
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private val DEFAULT_ZOOM = 14
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force l'initialisation avec le dernier moteur de rendu de la map
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

        val saveButton = findViewById<Button>(R.id.buttonSauvegarder)
        saveButton.setOnClickListener{
            if(positions.size > 1){
                // Création de la popup de sauvegarder de la trajectoire
                val dialogBuilder = AlertDialog.Builder(this)
                val inflater = this.layoutInflater
                val dialogView = inflater.inflate(R.layout.popup_save_trajectory, null)
                dialogBuilder.setView(dialogView)

                val dialog = dialogBuilder.create()

                val editText = dialogView.findViewById<EditText>(R.id.edit_text)
                val boutonSauvegarder = dialogView.findViewById<Button>(R.id.button_sauvegarder)

                // Fermeture de la popup
                val bouttonAnnuler = dialogView.findViewById<Button>(R.id.button_annuler)
                bouttonAnnuler.setOnClickListener{
                    dialog.dismiss()
                }

                dialog.show()
            }
            else{
                // Création de la popup trajectoire invalide
                val context: Context = this
                val builder = AlertDialog.Builder(context)
                builder.setTitle("La trajectoire est invalide")
                builder.setMessage("Votre trajectoire doit avoir plus d'un point.")
                val dialog = builder.create()
                dialog.show()
            }
        }

        val lancerButton = findViewById<Button>(R.id.buttonLancer)
        lancerButton.setOnClickListener{
            // Lancement de la trajectoire
        }

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

        // Ajouter d'un draglistener sur le marqueur
        mMap.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {

                // Mise à jour des polylines
                for (i in 0 until positions.size) {
                    val markerList = positions[i]

                    // Recherche du marker modifié
                    if(markerList == marker){

                        // Mise à jour des polylines, suppresion et création
                        if( i > 0){
                            polylines[i-1].remove()
                            polylines[i-1] = addPolyline(positions[i-1].position,marker.position)
                            if(polylines.size > i){
                                polylines[i].remove()
                                polylines[i] = addPolyline(marker.position,positions[i+1].position)
                            }
                            break
                        }
                        else if(i == 0){
                            if(polylines.size > i){
                                polylines[i].remove()
                                polylines[i] = addPolyline(marker.position,positions[i+1].position)
                            }
                            break
                        }
                    }
                }

            }
        })

        // Ajouter un écouteur pour le clic sur la carte
        mMap.setOnMapClickListener { latLng ->

            // Ajout d'un marqueur à la position du clic et l'ajouter à la liste
            positions.add(addMarker(latLng,positions.size))

            // Ajout du polyline reliant les marqueurs, si il y a plus d'un marqueur
            if(positions.size > 1){
                val lastPosition = positions[positions.size - 2].position
                polylines.add(addPolyline(lastPosition,latLng))
            }
        }

        // Définir une position de départ pour la caméra
        val coord = LatLng(46.160329,-1.151139)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM.toFloat()))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coord))
    }

    private fun addPolyline(startPosition : LatLng, endPosition: LatLng) : Polyline {
        val stampStyle = TextureStyle.newBuilder(BitmapDescriptorFactory.fromResource(android.R.drawable.arrow_down_float)).build()
        val span = StyleSpan(StrokeStyle.colorBuilder(Color.BLACK).stamp(stampStyle).build())
        return mMap.addPolyline(
            PolylineOptions()
                .add(startPosition, endPosition)
                .addSpan(span)
                .width(30f)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    private fun addMarker(position : LatLng, numero : Int) : Marker{
        return mMap.addMarker(MarkerOptions()
            .position(position)
            .title("Numéro ${numero+1}")
            .draggable(true))!!
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