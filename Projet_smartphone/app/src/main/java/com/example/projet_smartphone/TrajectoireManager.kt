package com.example.projet_smartphone

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class TrajectoireManager {
    private val trajectoires: MutableList<Trajectoire> = mutableListOf()

    fun ajouterTrajectoire(trajectoire: Trajectoire) {
        trajectoires.add(trajectoire)
    }

    fun supprimerTrajectoire(id: Int) {
        trajectoires.removeIf { it.id == id }
    }

    fun getTrajectoire(id: Int): Trajectoire? {
        return trajectoires.find { it.id == id }
    }

    fun getAllTrajectoires(): List<Trajectoire> {
        return trajectoires.toList()
    }

    fun exportToGPX(): String {
        val gpxContent = StringBuilder()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")

        gpxContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
        gpxContent.append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"TrajectoireManager\">\n")

        for (trajectoire in trajectoires) {
            gpxContent.append("\t<trk>\n")
            gpxContent.append("\t\t<name>${trajectoire.nom}</name>\n")
            gpxContent.append("\t\t<trkseg>\n")

            for (point in trajectoire.listePoints) {
                val time = dateFormatter.format(Date()) // Utilisez la date et l'heure actuelles pour cet exemple
                gpxContent.append("\t\t\t<trkpt lat=\"${point.latLng.latitude}\" lon=\"${point.latLng.longitude}\">\n")
                gpxContent.append("\t\t\t\t<time>$time</time>\n")
                gpxContent.append("\t\t\t\t<name>${point.numero}</name>\n")
                gpxContent.append("\t\t\t</trkpt>\n")
            }

            gpxContent.append("\t\t</trkseg>\n")
            gpxContent.append("\t</trk>\n")
        }

        gpxContent.append("</gpx>")

        return gpxContent.toString()
    }

    fun saveGPXToFile(context: Context, gpxContent: String, fileName: String) {
        val file = File(context.filesDir, fileName)
        file.writeText(gpxContent)
    }

    fun readGPXFile(context: Context, fileName: String): String {
        val file = File(context.filesDir, fileName)
        return file.readText()
    }

}

