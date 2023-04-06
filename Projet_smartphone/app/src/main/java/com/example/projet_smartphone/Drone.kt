package com.example.projet_smartphone

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import android.os.Parcel
import android.os.Parcelable
import kotlin.math.abs

class Drone(val nom: String) : Parcelable {

    var positionActuel: Point = Point(LatLng(0.0,0.0),"bleu",0)
    var vitesse: Double? = null
    var angle: Double? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nom)
        parcel.writeParcelable(positionActuel, flags)
        parcel.writeValue(vitesse)
        parcel.writeValue(angle)
    }

    companion object CREATOR : Parcelable.Creator<Drone> {
        override fun createFromParcel(parcel: Parcel): Drone {
            val nom = parcel.readString() ?: ""
            val positionActuel = parcel.readParcelable(Point::class.java.classLoader) ?: Point(LatLng(0.0,0.0), "bleu", 0)
            val vitesse = parcel.readValue(Double::class.java.classLoader) as? Double
            val angle = parcel.readValue(Double::class.java.classLoader) as? Double
            return Drone(nom).apply {
                this.positionActuel = positionActuel
                this.vitesse = vitesse
                this.angle = angle
            }
        }

        override fun newArray(size: Int): Array<Drone?> {
            return arrayOfNulls(size)
        }
    }

    // Récupère la trame et analyse les données NMEA pour extraire la latitude, la longitude,
    // la vitesse et l'angle de trajectoire d'un drone et mettre à jour les propriétés du drone en conséquence.
    fun parseNMEA(trameNMEA: String) {
        val lines = trameNMEA.split("\n")

        for (line in lines) {
            val tokens = line.split(",")

            when (tokens[0]) {
                "\$GPRMC" -> {
                    val latitude = parseLatitude(tokens[3], tokens[4])
                    val longitude = parseLongitude(tokens[5], tokens[6])
                    val vitesseKnots = tokens[7].toDoubleOrNull() ?: 0.0
                    val angleTraj = tokens[8].toDoubleOrNull() ?: 0.0

                    positionActuel = Point(LatLng(latitude,longitude), "bleu",0)
                    vitesse = vitesseKnots
                    angle = angleTraj

                    // Debugging statements
                    println("Latitude: $latitude, Longitude: $longitude, Vitesse: $vitesseKnots, Angle: $angleTraj")
                }
            }
        }
    }



    private fun parseLatitude(latitudeStr: String, direction: String): Double {
        val decimalDegrees = (latitudeStr.substring(0, 2).toDouble() + latitudeStr.substring(2).toDouble() / 60)
        return if (direction == "S") -decimalDegrees else decimalDegrees
    }

    private fun parseLongitude(longitudeStr: String, direction: String): Double {
        val decimalDegrees = (longitudeStr.substring(0, 3).toDouble() + longitudeStr.substring(3).toDouble() / 60)
        return if (direction == "W") -decimalDegrees else decimalDegrees
    }

    override fun toString(): String {
        return "Drone(nom='$nom', positionActuel=$positionActuel, vitesse=$vitesse, angle=$angle)"
    }


    // Prend en entrée la latitude, la longitude et la vitesse du drone,
    // met à jour les propriétés du drone en conséquence et renvoie une trame NMEA pour transmettre les données.
    fun genereTrameNMEA(latitude: Double,longitude: Double,vitesse: Double ): String {
        this.positionActuel.latLng = LatLng(latitude,longitude)
        this.vitesse = vitesse
        val latitude = formatLatitude(this.positionActuel.latLng.latitude)
        val longitude = formatLongitude(this.positionActuel.latLng.longitude)
        val vitesseKnots = String.format(Locale.US, "%.2f", this.vitesse!!)
        val angleTraj = String.format(Locale.US, "%.2f", this.angle!!)
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyy"))

        println("Trame NMEA :")
        return "\$GPRMC,${date}000000,A,$latitude,N,$longitude,E,$vitesseKnots,$angleTraj,$date,0.0,E,A"
    }

    private fun formatLatitude(latitude: Double): String {
        val degrees = latitude.toInt().toString().padStart(2, '0')
        val minutes = String.format(Locale.US, "%.4f", (latitude - degrees.toInt()).times(60))
        return "$degrees$minutes"
    }

    private fun formatLongitude(longitude: Double): String {
        val degrees = abs(longitude.toInt()).toString().padStart(3, '0')
        val minutes = String.format(Locale.US, "%.4f", (abs(longitude) - abs(longitude.toInt())).times(60))
        val direction = if (longitude < 0) "W" else "E"
        return "$degrees$minutes,$direction"
    }

    // Fonction permettant de mettre à jour la direction du drone en degré
    fun setDirection(angleRadian : Double){
        this.angle = angleRadian * (180 / Math.PI)
    }

}
