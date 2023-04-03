package com.example.projet_smartphone

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Drone(val nom: String){

    var positionActuel: Point = Point(0.0,0.0,"bleu")
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

                    positionActuel = Point(latitude, longitude, "bleu")
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


    fun genereTrameNMEA(latitude: Double,longitude: Double,vitesse: Double ): String {
        this.positionActuel.latitude = latitude
        this.positionActuel.longitude = longitude
        this.vitesse = vitesse
        val latitude = formatLatitude(this.positionActuel!!.latitude)
        val longitude = formatLongitude(this.positionActuel!!.longitude)
        val vitesseKnots = String.format(Locale.US, "%.2f", this.vitesse!!)
        val angleTraj = String.format(Locale.US, "%.2f", this.angle!!)
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyy"))

        System.out.println("Trame NMEA :")
        return "\$GPRMC,${date}000000,A,$latitude,N,$longitude,E,$vitesseKnots,$angleTraj,$date,0.0,E,A"
    }

    private fun formatLatitude(latitude: Double): String {
        val degrees = latitude.toInt().toString().padStart(2, '0')
        val minutes = String.format(Locale.US, "%.4f", (latitude - degrees.toInt()).times(60))
        return "$degrees$minutes"
    }

    private fun formatLongitude(longitude: Double): String {
        val degrees = Math.abs(longitude.toInt()).toString().padStart(3, '0')
        val minutes = String.format(Locale.US, "%.4f", (Math.abs(longitude) - Math.abs(longitude.toInt())).times(60))
        val direction = if (longitude < 0) "W" else "E"
        return "$degrees$minutes,$direction"
    }

}