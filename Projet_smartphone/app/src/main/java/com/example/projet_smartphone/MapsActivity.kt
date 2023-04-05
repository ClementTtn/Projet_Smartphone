package com.example.projet_smartphone

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
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.projet_smartphone.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.Marker
import kotlin.math.round


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var DroneMarkeur: Marker
    private lateinit var drone : Drone
    private lateinit var positionDrone : LatLng
    var accelerometerValues = Array(2) { 0.0 }
    var vitesseMAX = 10.0

    fun updateLatLng(currentLatLng: LatLng, direction: Double, speed: Double): LatLng {
        val distance = speed / 3600.0 // Convert speed from km/h to km/s

        val lat1 = Math.toRadians(currentLatLng.latitude)
        val lon1 = Math.toRadians(currentLatLng.longitude)

        val R = 6371.0 // Earth radius in km

        val lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance/R) + Math.cos(lat1) * Math.sin(distance/R) * Math.cos(
            direction
        ))
        val lon2 = lon1 + Math.atan2(Math.sin(direction) * Math.sin(distance/R) * Math.cos(lat1), Math.cos(distance/R) - Math.sin(lat1) * Math.sin(lat2))

        val updatedLat = Math.toDegrees(lat2)
        val updatedLng = Math.toDegrees(lon2)

        return LatLng(updatedLat, updatedLng)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        val extras = intent.extras
        if (extras != null) {
            drone = extras.getParcelable<Drone>("drone_object")!!

        }
        positionDrone = drone.positionActuel.latLng

        DroneMarkeur = mMap.addMarker(MarkerOptions().position(positionDrone).title(drone.nom))!!
        mMap.moveCamera(CameraUpdateFactory.newLatLng(positionDrone))
        val zoomLevel = 15f
        val Zoom = CameraUpdateFactory.newLatLngZoom(positionDrone, zoomLevel)
        mMap.animateCamera(Zoom)



        setUpSensorStuff()
        startMarkerRefresh()
    }

    private fun setUpSensorStuff() {
        // Create the sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Specify the sensor you want to listen to
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
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
            val radianAngle = Math.atan2(yValue.toDouble(), xValue.toDouble())
            val vitesse = Math.sqrt((xValue*xValue + yValue*yValue).toDouble()/10)

            accelerometerValues = arrayOf(radianAngle,vitesse)

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
    fun onDestroyMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.let {
            supportFragmentManager
                .beginTransaction()
                .remove(it)
                .commit()
        }
    }

    fun resendData(){
        val intent = Intent(this@MapsActivity, MainActivity::class.java)
        intent.putExtra("drone_object", drone)
        setResult(Activity.RESULT_OK, intent)
    }

    private lateinit var marker: Marker
    private val handler = Handler()

    fun startMarkerRefresh() {
        // Répéter la mise à jour du marqueur toutes les 2 secondes
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Mettre à jour la position du marqueur
                var vitesse = vitesseMAX*accelerometerValues[1]
                positionDrone = updateLatLng(positionDrone, accelerometerValues[0], vitesse)
                //System.out.println(drone.genereTrameNMEA(positionDrone.latitude,positionDrone.latitude,vitesse))

                val speedTextView = findViewById<TextView>(R.id.speed_text)
                speedTextView.text = "${round(vitesse*10)/10} knots"

                DroneMarkeur.position = positionDrone
                drone.positionActuel.latLng = positionDrone

                val speedTextView = findViewById<TextView>(R.id.speed_text)
                speedTextView.text = "${round(vitesse)} km/h"
                // Répéter l'exécution de cette fonction toutes les 2 secondes

                handler.postDelayed(this, 1000)
            }
        }, 1000)
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



}
