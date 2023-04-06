package com.example.projet_smartphone

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.projet_smartphone.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import kotlin.math.*


// Pilotage du drone avec le gyroscope
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var droneMarkeur: Marker
    private lateinit var drone : Drone
    private lateinit var positionDrone : LatLng
    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var accelerometerValues = Array(2) { 0.0 }
    var vitesseMAX = 10.0

    fun updateLatLng(currentLatLng: LatLng, direction: Double, speed: Double): LatLng {
        val distance = speed / 3600.0 // Convert speed from km/h to km/s

        val lat1 = Math.toRadians(currentLatLng.latitude)
        val lon1 = Math.toRadians(currentLatLng.longitude)

        val r = 6371.0 // Earth radius in km

        val lat2 = asin(sin(lat1) * cos(distance/r) + cos(lat1) * sin(distance/r) * cos(direction))
        val lon2 = lon1 + atan2(sin(direction) * sin(distance/r) * cos(lat1), cos(distance/r) - sin(lat1) * sin(lat2))

        val updatedLat = Math.toDegrees(lat2)
        val updatedLng = Math.toDegrees(lon2)

        return LatLng(updatedLat, updatedLng)
    }


    // Initialise la carte Google Maps, récupère les permissions de localisation de l'utilisateur,
    // crée et affiche le marqueur du drone sur la carte. Elle crée également des listeners pour les boutons de zoom et le bouton de suivi de caméra,
    // ainsi qu'un listener pour le bouton de retour au menu principal.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Keeps phone in light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Activation du bouton de retour au menu principale et modification de l'apparence
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setBackgroundDrawable(ContextCompat.getDrawable(this@MapsActivity, R.drawable.background_degrade))
        }
        window.statusBarColor = Color.TRANSPARENT
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    }


    //écupère l'instance de GoogleMap, met à jour l'interface utilisateur en fonction de l'état des permissions,
    // crée le marqueur du drone sur la carte, et crée des écouteurs pour les boutons de zoom et le bouton de suivi de caméra.
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()

        val extras = intent.extras
        if (extras != null) {
            drone = extras.getParcelable("drone_object")!!

        }
        positionDrone = drone.positionActuel.latLng

        // Changement de l'icon du "marker" drone et de sa taille
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.waverider)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 125, 125, false)
        val markerIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        droneMarkeur = mMap.addMarker(MarkerOptions()
            .position(positionDrone)
            .title(drone.nom)
            .icon(markerIcon)
            .anchor(0.5f,0.5f)
            .rotation(drone.angle!!.toFloat())
        )!!
        mMap.moveCamera(CameraUpdateFactory.newLatLng(positionDrone))
        val zoom = CameraUpdateFactory.newLatLngZoom(positionDrone, DEFAULT_ZOOM.toFloat())
        mMap.animateCamera(zoom)


        // Ajout d'un écouteur pour le bouton de zoom plus
        val zoomInButton = findViewById<ImageButton>(R.id.zoom_in_button)
        zoomInButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }

        // Ajout d'un écouteur pour le bouton de zoom moins
        val zoomOutButton = findViewById<ImageButton>(R.id.zoom_out_button)
        zoomOutButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }


        val toggleCameraFollowButton = findViewById<Button>(R.id.toggle_camera_follow)
        toggleCameraFollowButton.setOnClickListener {
            cameraFollowsDrone = !cameraFollowsDrone
            if (cameraFollowsDrone) {
                toggleCameraFollowButton.text = getString(R.string.button_camera_suivi)
            } else {
                toggleCameraFollowButton.text = getString(R.string.button_camera_fixe)
            }
        }

        setUpSensorStuff()
        startMarkerRefresh()
    }

    private fun setUpSensorStuff() {
        // Create the sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Specify the sensor you want to listen to
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI ,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }


    override fun onSensorChanged(event: SensorEvent?) {
        // Checks for the sensor we have registered
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val xValue = event.values[0]*-1
            val yValue = event.values[1]
            val radianAngle = atan2(yValue.toDouble(), xValue.toDouble())
            val vitesse = sqrt((xValue*xValue + yValue*yValue).toDouble()/10)

            accelerometerValues = arrayOf(radianAngle,vitesse)

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }


    // Supprime le fragment de la carte et renvoie l'objet drone à l'activité MainActivity.
    private fun onDestroyMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.let {
            supportFragmentManager
                .beginTransaction()
                .remove(it)
                .commit()
        }
    }

    private fun resendData(){
        val intent = Intent(this@MapsActivity, MainActivity::class.java)
        intent.putExtra("drone_object", drone)
        setResult(Activity.RESULT_OK, intent)
    }

    private val handler = Handler()
    private var cameraFollowsDrone: Boolean = true


    // Appel toutes les secondes pour mettre à jour la position du marqueur du drone sur la carte et calculer la vitesse du drone en utilisant les données de l'accéléromètre.
    private fun startMarkerRefresh() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Mettre à jour la position du marqueur
                var vitesse = vitesseMAX * accelerometerValues[1]
                positionDrone = updateLatLng(positionDrone, accelerometerValues[0], vitesse)
                vitesse *= 0.53996
                println(drone.genereTrameNMEA(positionDrone.latitude, positionDrone.longitude, vitesse))

                val speedTextView = findViewById<TextView>(R.id.speed_text)
                speedTextView.text = buildString {
                    append((round(vitesse * 10) / 10))
                    append(" knots")
                }

                drone.setDirection(accelerometerValues[0])

                droneMarkeur.position = positionDrone
                droneMarkeur.rotation = drone.angle!!.toFloat()

                if (cameraFollowsDrone) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(positionDrone))
                }

                handler.postDelayed(this, 1000)
            }
        }, 2500)
    }


    // Retour au menu principale
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onDestroyMap()
                resendData()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
