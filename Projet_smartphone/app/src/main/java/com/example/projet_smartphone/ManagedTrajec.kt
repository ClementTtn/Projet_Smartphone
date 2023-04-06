package com.example.projet_smartphone

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ManagedTrajec : AppCompatActivity() {
    private lateinit var adapter:MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_managed_trajec)

        //modification de ActionBar : modification de la couleur, ajout du bouton de retour
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setBackgroundDrawable(ContextCompat.getDrawable(this@ManagedTrajec, R.drawable.background_degrade))
        }
        window.statusBarColor = Color.TRANSPARENT

        //creation de la liste des noms des trajectoires
        val list = arrayListOf<String>()

        //recuperation des fichiers du repertoire courant avec extension .traj
        val files = applicationContext.filesDir.listFiles { file -> file.name.endsWith(".traj") }
        for (file in files!!) {
            //ajout des noms des fichiers a la liste
            list.add(file.nameWithoutExtension)
        }

        //recuperation du composant de la vue
        val listView: ListView = findViewById(R.id._listView)

        //ajout de adapter a la vue
        adapter = MyAdapter(list,  this)
        listView.adapter = adapter

        //itemclicklistener de la listview
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _, position, _ ->
            //recuperation de item courant
            val intent = Intent(this, TrajectoryAddActivity::class.java)

            //chargement de la fenetre 3 avec le nom du fichier en parametre
            intent.putExtra("nom_fichier",adapterView.getItemAtPosition(position).toString() + ".traj")
            startActivity(intent)
        }

        //recuperation du composant de la vue
        val ajout: Button = findViewById(R.id.button2)

        //onclicklistener du bouton
        ajout.setOnClickListener {
            //chargement de la vue de création de trajectoire
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