package com.example.projet_smartphone

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
        list.addAll(listOf("Trajectoire 1","Traj", "Trajectoire 3", "Trajectoire 3", "Trajectoire 3", "Trajectoire 3", "Trajectoire 3", "Trajectoire 3"))

        val listView: ListView = findViewById(R.id._listView)
        adapter = MyAdapter(list,  this)
        listView.adapter = adapter


        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            println(adapterView.getItemAtPosition(position))
        }

        val ajout: Button = findViewById(R.id.button2)
        ajout.setOnClickListener { //chargement de la vue de création de trajectoire
            println("ok button")
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