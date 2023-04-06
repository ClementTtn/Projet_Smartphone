package com.example.projet_smartphone

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var drone : Drone
    private var isFirstStart = true
    private var trameNMEA = "\$GPRMC,104339.271,A,4609.055502,N,00110.158403,W,0.0,114.4,300323,,,*33\n" +
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

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation du drone au premier lancement de l'activité
        if(isFirstStart) {
            drone = Drone("Mon drone")
            drone.parseNMEA(trameNMEA)
            isFirstStart = false
        }

        // Activation du bouton de retour au menu principale et modification de l'apparence
        supportActionBar?.apply {
            setBackgroundDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.background_degrade))
        }
        window.statusBarColor = Color.TRANSPARENT

        // Blocage de la rotation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Ajout du click listener sur le bouton de lancement de la fenetre 2 : Piloter mon drone : MapsActivity
        val boutonDrone = findViewById<Button>(R.id.button_drone)
        val myActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            drone = result.data?.extras?.getParcelable("drone_object")!!
            isFirstStart = false
        }

        // Ajout du click listener sur le bouton de lancement de la fenetre 3 : Créer une trajectoire : TrajectoryAddActivity
        boutonDrone.setOnClickListener{
            // Lancement de l'activité
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(intent)
        }

        // Ajout du click listener sur le bouton de lancement de la fenetre 3 : Mes trajectoires : TrajectoryAddActivity
        val boutonTrajectory = findViewById<Button>(R.id.button_trajectory)
        boutonTrajectory.setOnClickListener{
            // Lancement de l'activité
            val intent = Intent(this@MainActivity, TrajectoryAddActivity::class.java)
            startActivity(intent)
        }

        // Ajout du click listener sur le bouton de lancement de la fenetre 4 : Mes trajectoires : ManagedTrajec
        val boutonMyTrajectory = findViewById<Button>(R.id.button_mytrajectory)
        boutonMyTrajectory.setOnClickListener{
            // Lancement de l'activité
            val intent = Intent(this@MainActivity, ManagedTrajec::class.java)
            startActivity(intent)
        }

    }
}