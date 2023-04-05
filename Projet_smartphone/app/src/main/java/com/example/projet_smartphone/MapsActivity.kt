package com.example.projet_smartphone

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
import android.widget.TextView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.projet_smartphone.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.Marker
import java.io.File
import java.io.IOException
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

        val point1 = Point(LatLng(48.8566, 2.3522), "red", 1)
        val point2 = Point(LatLng(48.8600, 2.3500), "blue", 2)
        val point3 = Point(LatLng(48.8640, 2.3480), "green", 3)
        val point4 = Point(LatLng(48.8680, 2.3460), "yellow", 4)
        val point5 = Point(LatLng(48.8720, 2.3440), "purple", 5)



        val trajectoire = Trajectoire("Trajectoire 1", listOf(point1, point2, point3, point4, point5))
        val fileName = "drone_trajectory.gpx"

        try {
            trajectoire.saveGPXFile(this, fileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }


        val file = File(this.filesDir, fileName)

        try {
            val gpxContent = file.readText()
            println("======================================\n\n\nContenu du fichier GPX : \n$gpxContent")
        } catch (e: IOException) {
            e.printStackTrace()
        }





        var trameNMEA = "\$GPRMC,104339.271,A,4609.055502,N,00110.158403,W,0.0,114.4,300323,,,*33\n" +
                "\$IIVHW,114.4,T,114.4,M,0.0,N,0.0,K*55\n" +
                "\$GPVTG,114.4,T,114.4,M,0.0,N,0.0,K*4E\n" +
                "\$IIHDT,114.4,T*22\n" +
                "\$GPGLL,4609.055502,N,00110.158403,W,104339.271,A*29\n" +
                "\$GPGGA,104339.271,4609.055502,N,00110.158403,W,1,4,0.6,2.0,M,,,,*22\n" +
                "\$GPGSA,A,3,8,11,15,22,,,,,,,,,3.3,0.6,0.6*0E\n" +
                "\$GPZDA,104339.271,30,03,2023,02,00*5F\n" +
                "\$IIVBW,-0.3,-0.3,A,-0.3,-0.2,A,-0.1,A,-0.7,A*44\n" +
                "!AIVDO,1,1,,A,17PaewhP00wraKPJJ6>lMkU>0000,0*6F\n" +
                "!AIVDM,1,1,,A,17PaewhP00wraKPJJ6>lMkU>0000,0*6D\n" +
                "\$WIMWD,9.2,T,9.2,M,2.0,N,1.0,M*59\n" +
                "\$WIMWV,254.9,R,2.0,N,A*2B\n" +
                "\$IIMTW,13.2,C*13\n" +
                "\$SDDPT,1.7,0.3*52\n" +
                "\$SDDBT,5.4,f,1.7,M,0.9,F*08\n" +
                "\$INWPL,4609.055502,N,00110.158403,W,wpt*3D\n" +
                "!AIVDO,2,1,9,A,57Paewh00001<To7;?@plD5<Tl0000000000000U1@:550w:c2TnA3QF,0*26\n" +
                "!AIVDO,2,2,9,A,@00000000000002,2*5D\n" +
                "!AIVDM,2,1,9,A,57Paewh00001<To7;?@plD5<Tl0000000000000U1@:550w:c2TnA3QF,0*24\n" +
                "!AIVDM,2,2,9,A,@00000000000002,2*5F\n" +
                "\$IIRPM,E,1,0,10.5,A*7C\n" +
                "\$IIRPM,E,2,0,10.5,A*7F"

        // Add a marker in Sydney and move the camera
        drone = Drone("Mon drone")
        drone.parseNMEA(trameNMEA)
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

    private lateinit var marker: Marker
    private val handler = Handler()

    fun startMarkerRefresh() {
        // Répéter la mise à jour du marqueur toutes les 2 secondes
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Mettre à jour la position du marqueur
                var vitesse = vitesseMAX*accelerometerValues[1]
                positionDrone = updateLatLng(positionDrone, accelerometerValues[0], vitesse)
                vitesse = vitesse * 0.53996
                System.out.println(drone.genereTrameNMEA(positionDrone.latitude,positionDrone.latitude,vitesse))

                val speedTextView = findViewById<TextView>(R.id.speed_text)
                speedTextView.text = "${round(vitesse*10)/10} knots"

                DroneMarkeur.position = positionDrone

                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    // Retour au menu principale
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
