package com.example.projet_smartphone

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class TrajectoireManager(var listPoints: List<Point>, var nom: String) {


    fun exportToGPX(): String {
        val gpxContent = StringBuilder()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")

        gpxContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
        gpxContent.append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"WaveRider\">\n")


            gpxContent.append("\t<trk>\n")
            gpxContent.append("\t\t<name>${this.nom}</name>\n")
            gpxContent.append("\t\t<trkseg>\n")

            for (point in listPoints) {
                val time = dateFormatter.format(Date()) // Utilisez la date et l'heure actuelles pour cet exemple
                gpxContent.append("\t\t\t<trkpt lat=\"${point.latLng.latitude}\" lon=\"${point.latLng.longitude}\">\n")
                gpxContent.append("\t\t\t\t<time>$time</time>\n")
                gpxContent.append("\t\t\t\t<name>${point.numero}</name>\n")
                gpxContent.append("\t\t\t</trkpt>\n")
            }

            gpxContent.append("\t\t</trkseg>\n")
            gpxContent.append("\t</trk>\n")


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