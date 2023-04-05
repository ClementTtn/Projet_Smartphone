package com.example.projet_smartphone

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Activation du bouton de retour au menu principale et modification de l'apparence
        supportActionBar?.apply {
            setBackgroundDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.background_degrade))
        }
        window.statusBarColor = Color.TRANSPARENT

        // Blocage de la rotation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val boutonDrone = findViewById<Button>(R.id.button_drone)
        boutonDrone.setOnClickListener{
            val intent = Intent(this@MainActivity, MapsActivity::class.java)

            startActivity(intent)
        }

        val boutonTrajectory = findViewById<Button>(R.id.button_trajectory)
        boutonTrajectory.setOnClickListener{
            val intent = Intent(this@MainActivity, TrajectoryAddActivity::class.java)

            startActivity(intent)
        }

        val boutonMyTrajectory = findViewById<Button>(R.id.button_mytrajectory)
        boutonMyTrajectory.setOnClickListener{
            val intent = Intent(this@MainActivity, ManagedTrajec::class.java)

            startActivity(intent)
        }

    }

}