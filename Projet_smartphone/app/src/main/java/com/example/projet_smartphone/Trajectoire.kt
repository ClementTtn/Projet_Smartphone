package com.example.projet_smartphone

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline

class Trajectoire(val positionDebut : Marker, val positionFin : Marker, var polyline: Polyline) {

    private val nom: String = ""
    private val listePoints: List<Point> = ArrayList()
    private var vitesse: Double = 15.0

    public fun getVitesse() : Double{
        return this.vitesse
    }
    public fun setVitesse(vitesse: Double){
        this.vitesse = vitesse
    }

}