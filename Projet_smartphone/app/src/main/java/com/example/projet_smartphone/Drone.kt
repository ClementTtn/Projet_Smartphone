package com.example.projet_smartphone

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Drone(val nom: String){

    var positionActuelle: Point = Point(0.0,0.0,"bleu")
    var vitesse: Double? = null
    var angle: Double? = null


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

                    positionActuelle = Point(latitude,longitude, "bleu")
                    vitesse = vitesseKnots
                    angle = angleTraj
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
        return "Drone(nom='$nom', positionActuel=$positionActuelle, vitesse=$vitesse, angle=$angle)"
    }


    fun genereTrameNMEA(): String {
        if (this.positionActuelle == null || this.vitesse == null || this.angle == null) {
            throw IllegalArgumentException("Drone information is incomplete.")
        }
        val latitude = formatLatitude(this.positionActuelle!!.latitude)
        val longitude = formatLongitude(this.positionActuelle!!.longitude)
        val vitesseKnots = String.format(Locale.US, "%.2f", this.vitesse!!)
        val angleTraj = String.format(Locale.US, "%.2f", this.angle!!)
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyy"))

        return "\$GPRMC,${date}000000,A,$latitude,N,$longitude,E,$vitesseKnots,$angleTraj,$date,0.0,E,A"
    }

    private fun formatLatitude(latitude: Double): String {
        val degrees = latitude.toInt().toString().padStart(2, '0')
        val minutes = String.format(Locale.US, "%.4f", (latitude - degrees.toInt()).times(60))
        return "$degrees$minutes"
    }

    private fun formatLongitude(longitude: Double): String {
        val degrees = longitude.toInt().toString().padStart(3, '0')
        val minutes = String.format(Locale.US, "%.4f", (longitude - degrees.toInt()).times(60))
        return "$degrees$minutes"
    }

}