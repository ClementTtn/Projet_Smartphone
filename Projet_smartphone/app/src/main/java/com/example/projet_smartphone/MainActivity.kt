package com.example.projet_smartphone

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.apply {
            setBackgroundDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.background_degrade))
        }
        window.statusBarColor = Color.TRANSPARENT

        val boutonDrone = findViewById<Button>(R.id.button_drone)

        val boutonTrajectory = findViewById<Button>(R.id.button_trajectory)
        boutonTrajectory.setOnClickListener{
            val intent = Intent(this@MainActivity, trajectoryAddActivity::class.java)

            startActivity(intent)
        }

        val boutonMyTrajectory = findViewById<Button>(R.id.button_mytrajectory)



    }

}