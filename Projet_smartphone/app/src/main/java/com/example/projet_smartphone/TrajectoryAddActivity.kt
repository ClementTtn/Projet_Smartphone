package com.example.projet_smartphone

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*
import kotlin.math.*

class TrajectoryAddActivity : AppCompatActivity(), OnMapsSdkInitializedCallback, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: TrajectoryAddActivityBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var points : ArrayList<Point> = ArrayList()
    private val trajectoires : ArrayList<Trajectoire> = ArrayList()
    private var nomFichier : String? = null
    private var trajectoireLance : Boolean = false
    private lateinit var lastMarker : Marker
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private val DEFAULT_ZOOM = 14
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

     // Fonction permettant la création de l'activité et de son layout. Ajout des clicks listeners sur les boutons permettant de lancer le popup de sauvegarde et de lancer la trajectoire
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force l'initialisation avec le dernier moteur de rendu de la map
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

        binding = TrajectoryAddActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupère la service de localisation
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Activation du bouton de retour au menu principale et modification de l'apparence
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setBackgroundDrawable(ContextCompat.getDrawable(this@TrajectoryAddActivity, R.drawable.background_degrade))
        }
        window.statusBarColor = Color.TRANSPARENT

        // Blocage de la rotation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // On récupère l'intent avec le nom du fichier
        nomFichier = intent.getStringExtra("nom_fichier")

        // Ajoute le clickLister sur le bouton de sauvegarde
        val saveButton = findViewById<Button>(R.id.buttonSauvegarder)
        saveButton.setOnClickListener{
            if(points.size > 1){

                // Création de la popup de sauvegarder de la trajectoire
                val dialogBuilder = AlertDialog.Builder(this)
                val inflater = this.layoutInflater
                val dialogView = inflater.inflate(R.layout.popup_save_trajectory, null)
                dialogBuilder.setView(dialogView)

                val dialog = dialogBuilder.create()

                // Récupération des éléments de la vue
                val editText = dialogView.findViewById<EditText>(R.id.edit_text)
                val boutonSauvegarder = dialogView.findViewById<Button>(R.id.button_sauvegarder)

                // Ajoute le click listener sur le bouton de sauvegarde
                boutonSauvegarder.setOnClickListener{

                    // On sauvegarde les points de la trajectoire dans un fichier .traj et dans .gpx

                    // Sauvegarde du .traj
                    val gson = Gson()
                    val jsonString = gson.toJson(points)
                    val fileName : String = editText.text.toString() + ".traj"
                    val fileOutputStream : FileOutputStream

                    try {
                        fileOutputStream = openFileOutput(fileName, MODE_PRIVATE)
                        fileOutputStream.write(jsonString.toByteArray())
                    } catch (e: FileNotFoundException){
                        e.printStackTrace()
                    }catch (e: NumberFormatException){
                        e.printStackTrace()
                    }catch (e: IOException){
                        e.printStackTrace()
                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                    // Sauvegarde du .gpx
                    val trajectoireManager  = TrajectoireManager(points,editText.text.toString())
                    val gpxContent = trajectoireManager.exportToGPX()
                    val fileNameGPX = editText.text.toString() + ".gpx"
                    trajectoireManager.saveGPXToFile(this,gpxContent,fileNameGPX)
                    trajectoireManager.readGPXFile(this, fileNameGPX)
                    val gpxContent2 = trajectoireManager.readGPXFile(this, fileNameGPX)
                    println("=========================")
                    println(gpxContent2)

                    dialog.dismiss()
                }

                // Fermeture de la popup
                val bouttonAnnuler = dialogView.findViewById<Button>(R.id.button_annuler)
                bouttonAnnuler.setOnClickListener {
                    dialog.dismiss()
                }

                // Affichage de la popup
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

        // Lancement de la trajectoire
        val lancerButton = findViewById<Button>(R.id.buttonLancer)
        lancerButton.setOnClickListener {
            // Lancement de la trajectoire
            if(!this.trajectoireLance){
                trajectoireLance = true
                lancerButton.text = getString(R.string.button_lancer_encours)
                GlobalScope.launch(Dispatchers.Main) {

                    // Affichage du trajet du drone sur chaque trajectoire
                    for (i in 0 until trajectoires.size) {

                        // Calcul des points de la trajectoire
                        val positionsTrajectoire = trajectoires[i].getCoordinates()

                        // Remise de l'orientation de la map au nord
                        val currentLatLng = mMap.cameraPosition.target
                        val currentZoomLevel = mMap.cameraPosition.zoom
                        val newCameraPosition = CameraPosition.Builder()
                            .target(currentLatLng)
                            .zoom(currentZoomLevel)
                            .bearing(0f) // réinitialise l'orientation à 0
                            .build()
                        val cameraUpdate = CameraUpdateFactory.newCameraPosition(newCameraPosition)
                        mMap.moveCamera(cameraUpdate)

                        // Changement de l'icon du "marker" drone et de sa taille
                        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.waverider)
                        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 125, 125, false)
                        val markerIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

                        // Création du marker du drone pour cette trajectoire
                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(positionsTrajectoire[0])
                                .icon(markerIcon)
                                .rotation(trajectoires[i].getDirection())
                                .anchor(0.5f, 0.5f)
                        )
                        // Boucle pour bouger le marker du drone sur une trajectoire
                        val drone = Drone("Mon drone")
                        drone.setAngle(trajectoires[i].getDirection().toDouble())
                        for (j in 1 until positionsTrajectoire.size) {
                            delay(33) // Attendre 33 ms avant la prochaine itération
                            println(drone.genereTrameNMEA(positionsTrajectoire[j].latitude, positionsTrajectoire[j].longitude, trajectoires[i].getVitesse()))
                            marker!!.position = positionsTrajectoire[j]
                        }
                        marker!!.remove()
                    }
                    trajectoireLance = false
                    lancerButton.text = getString(R.string.button_lancer)
                }
            }
        }

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

        // On vérifie si un intent a été passer
        if(nomFichier != null){
            // On trace la map en fonction du fichier de .traj
            redrawMap(nomFichier!!)
        }

        // Ajout le click listener pour le changement de vitesse d'une trajectoire
        mMap.setOnPolylineClickListener { polyline ->

            val pointDebut = polyline.points.first()
            val pointFin = polyline.points.last()
            var trajectoire : Trajectoire? = null

            // On trouve la trajectoire correspondant à la polyline
            for (i in 0 until trajectoires.size) {
                val trajectoireRecherche = trajectoires[i]
                if(trajectoireRecherche.positionDebut.position.latitude == pointDebut.latitude && trajectoireRecherche.positionDebut.position.longitude == pointDebut.longitude
                    && trajectoireRecherche.positionFin.position.latitude == pointFin.latitude && trajectoireRecherche.positionFin.position.longitude == pointFin.longitude)
                {
                    trajectoire = trajectoires[i]
                    break
                }
            }

            // Création de la popup speed
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.popup_speed_trajectory, null)
            val speedEdit = dialogView.findViewById<EditText>(R.id.edit_speed)
            dialogBuilder.setView(dialogView)
            val dialog = dialogBuilder.create()

            // On mets la vitesse actuelle de la trajectoire
            speedEdit.setText(trajectoire!!.getVitesse().toString())

            // On rajoute le click listener sur le bouton sauvegarde
            val boutonSauvegarder = dialogView.findViewById<Button>(R.id.button_sauvegarder)
            boutonSauvegarder.setOnClickListener{
                // On change la vitesse de la trajectoire
                trajectoire.setVitesse(speedEdit.text.toString().toDouble())
                dialog.dismiss()
            }

            // On rajoute le clickListener du bouton annuler
            val boutonAnnuler = dialogView.findViewById<Button>(R.id.button_annuler)
            boutonAnnuler.setOnClickListener{
                // On ferme la popup
                dialog.dismiss()
            }

            // On affiche le popup
            dialog.show()
        }

        // Ajouter d'un draglistener sur le marqueur
        mMap.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {

                // On trouve la bonne trajectoire
                for (i in 0 until trajectoires.size) {

                    if(trajectoires[i].positionDebut == marker){

                        // On efface et rajoute la trajectoire modifier, en choissant le bon type de modification en fonction de la position de la trajectoire dans la liste
                        if(i > 0){
                            // On modifie la trajectoire avant et après le points
                            trajectoires[i].polyline.remove()
                            trajectoires[i].polyline = addPolyline(trajectoires[i].positionDebut.position,trajectoires[i].positionFin.position)
                            trajectoires[i-1].polyline.remove()
                            trajectoires[i-1].polyline = addPolyline(trajectoires[i-1].positionDebut.position,trajectoires[i-1].positionFin.position)
                            break
                        }
                        else{
                            // On modifie la première trajectoire
                            trajectoires[i].polyline.remove()
                            trajectoires[i].polyline = addPolyline(trajectoires[i].positionDebut.position,trajectoires[i].positionFin.position)
                            break
                        }

                    }
                    else if( i == trajectoires.size-1 && trajectoires[i].positionFin == marker){
                        // On modifie la dernière trajectoire
                        trajectoires[i].polyline.remove()
                        trajectoires[i].polyline = addPolyline(trajectoires[i].positionDebut.position,trajectoires[i].positionFin.position)
                        break
                    }
                }

            }
        })

        // Ajouter un écouteur pour le clic sur la carte
        mMap.setOnMapClickListener { latLng ->

            // Ajout d'un marqueur à la position du clic et l'ajouter à la liste
            val point = Point(latLng,"",points.size)
            val marker = addMarker(latLng,point.numero)

            // Ajout du polyline reliant les marqueurs, si il y a plus d'un marqueur
            if(points.size > 0){
                // On crée et ajoute la trajectoire partant de l'avant dernier marker, jusqu'au nouveau marker
                val polyline = addPolyline(lastMarker.position,latLng)
                trajectoires.add(Trajectoire(lastMarker,marker,polyline))
            }
            // On rajoute le point et "sauvegarde" le dernier marker
            lastMarker = marker
            points.add(point)
        }

        // On définit une position de départ pour la caméra
        val coord = LatLng(46.160329,-1.151139)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM.toFloat()))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coord))
    }

    private fun redrawMap(nomFichier : String){

        // On récupère les points du fichier
        try {
            val fileInputStream = openFileInput(nomFichier)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val stringBuilder = StringBuilder()
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line).append("\n")
                line = bufferedReader.readLine()
            }
            bufferedReader.close()

            val json = stringBuilder.toString()
            val gson = Gson()
            val type = object : TypeToken<ArrayList<Point>>() {}.type
            points = gson.fromJson(json, type)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // On retrace les polylines et les markers correspondant au fichier .traj
        for(i in 0 until points.size){
            // On rajoute les marker
            val marker = addMarker(points[i].latLng,i)
            if(i > 0){
                // On rajoute les polylines
                val polyline = addPolyline(points[i-1].latLng,points[i].latLng)
                trajectoires.add(Trajectoire(lastMarker,marker,polyline))
            }
            lastMarker = marker
        }

    }

    // Fonction permettant de rajouter une polyline aussi appelée une trajectoire à la map
    private fun addPolyline(startPosition : LatLng, endPosition: LatLng) : Polyline {

        // On ajoute les flèches comme texture de la polyline
        val stampStyle = TextureStyle.newBuilder(BitmapDescriptorFactory.fromResource(android.R.drawable.arrow_down_float)).build()

        // On crée le style de la polyline
        val span = StyleSpan(StrokeStyle.colorBuilder(Color.BLACK).stamp(stampStyle).build())

        // On retourne la polyline
        return mMap.addPolyline(
            PolylineOptions()
                .add(startPosition, endPosition)
                .addSpan(span)
                .width(30f)
                .startCap(RoundCap())
                .endCap(RoundCap())
                .clickable(true)
        )
    }

    // Fonction permettant de rajouter un marker à la map avec son numéro
    private fun addMarker(position : LatLng, numero : Int) : Marker{
        return mMap.addMarker(MarkerOptions()
            .position(position)
            .title("Numéro ${numero+1}")
            .draggable(true))!!
    }

    // Fonction tiré de la documentation google/kotlin permettant d'actualiser la localisation du téléphone et de modifier l'affichage en conséquence
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

    // Fonction tiré de la documentation google/kotlin permettant d'obtenir les droits de localisation
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

    // Fonction tiré de la documentation google/kotlin permettant d'obtenir la localisation du téléphone
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