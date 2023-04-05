package com.example.projet_smartphone

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import kotlin.math.*

class Trajectoire(val positionDebut : Marker, val positionFin : Marker, var polyline: Polyline) {

    private val nom: String = ""
    private val listePoints: List<Point> = ArrayList()
    private var vitesse: Double = 15.0

    companion object {
        private var counter = 1
    }

    val id = counter++

    fun getNom() : String{
        return this.nom
    }
    fun getVitesse() : Double{
        return this.vitesse
    }
    fun setVitesse(vitesse: Double){
        this.vitesse = vitesse
    }

    fun getListePoints() : List<Point>{
        return this.getListePoints()
    }

    fun getCoordinates(): List<LatLng> {
        val r = 6371
        val latA = Math.toRadians(this.positionDebut.position.latitude)
        val longA = Math.toRadians(this.positionDebut.position.longitude)
        val latB = Math.toRadians(this.positionFin.position.latitude)
        val longB = Math.toRadians(this.positionFin.position.longitude)

        val dLat = latB - latA
        val dLong = longB - longA

        val a = sin(dLat / 2).pow(2) + cos(latA) * cos(latB) * sin(dLong / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val d = r * c

        val vitesseKmh = this.vitesse * 1.852
        val intervalleTemps = 33 // intervalle de temps en ms pour afficher 30 points par seconde
        val tempsGlobal = d / vitesseKmh * 3600 * 1000 // temps en ms
        val nombrePoints = (tempsGlobal / intervalleTemps).toInt() // distance parcourue en nm pendant l'intervalle de temps

        val latStep = dLat / nombrePoints
        val longStep = dLong / nombrePoints

        return (0..nombrePoints).map { i ->
            val lat = Math.toDegrees(latA + i * latStep)
            val long = Math.toDegrees(longA + i * longStep)
            LatLng(lat, long)
        }
    }

    fun getDirection() : Float{

        val latA = Math.toRadians(this.positionDebut.position.latitude)
        val longA = Math.toRadians(this.positionDebut.position.longitude)
        val latB = Math.toRadians(this.positionFin.position.latitude)
        val longB = Math.toRadians(this.positionFin.position.longitude)

        val y = sin(longB - longA) * cos(latB)
        val x = cos(latA) * sin(latB) - sin(latA) * cos(latB) * cos(longB - longA)
        var direction = Math.toDegrees(atan2(y, x)).toFloat()

        if (direction < 0) {
            direction += 360f
        }

        return direction
    }

}
