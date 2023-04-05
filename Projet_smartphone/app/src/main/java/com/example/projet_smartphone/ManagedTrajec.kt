package com.example.projet_smartphone

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ManagedTrajec : AppCompatActivity() {
    private lateinit var adapter:MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_managed_trajec)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setBackgroundDrawable(ContextCompat.getDrawable(this@ManagedTrajec, R.drawable.background_degrade))
        }
        window.statusBarColor = Color.TRANSPARENT

        val list = arrayListOf<String>()
        val files = applicationContext.filesDir.listFiles { file -> file.name.endsWith(".traj") }
        for (file in files!!) {
            list.add(file.nameWithoutExtension)
        }
        val listView: ListView = findViewById(R.id._listView)
        adapter = MyAdapter(list,  this)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _, position, _ ->
            val intent = Intent(this, TrajectoryAddActivity::class.java)

            intent.putExtra("nom_fichier",adapterView.getItemAtPosition(position).toString() + ".traj")

            startActivity(intent)
        }

        val ajout: Button = findViewById(R.id.button2)
        ajout.setOnClickListener { //chargement de la vue de création de trajectoire
            val intent = Intent(this, TrajectoryAddActivity::class.java)

            startActivity(intent)
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
}