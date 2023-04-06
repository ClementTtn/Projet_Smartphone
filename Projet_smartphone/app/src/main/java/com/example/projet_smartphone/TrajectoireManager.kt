package com.example.projet_smartphone

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/*
Cette class permet d'exporter les données d'une trajectoire que l'on enregistre dans un fichier au format GPX,
une utilise cette classe dans la vue TrajectoryAddActivity
 */
class TrajectoireManager(var listPoints: List<Point>, var nom: String) {


    // Génère une chaîne de caractères représentant les données de la trajectoire au format GPX. On utilise des balises pour structurées les données.
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
                gpxContent.append("\t\t\t\t<id>${point.numero}</id>\n")
                gpxContent.append("\t\t\t</trkpt>\n")
            }

            gpxContent.append("\t\t</trkseg>\n")
            gpxContent.append("\t</trk>\n")


        gpxContent.append("</gpx>")

        return gpxContent.toString()
    }

    // Sauvegarde le contenu de la chaîne de caractères générée par la méthode `exportToGPX()` dans un fichier avec un nom donné.
    fun saveGPXToFile(context: Context, gpxContent: String, fileName: String) {
        val file = File(context.filesDir, fileName)
        file.writeText(gpxContent)
    }

    // Lit le contenu d'un fichier GPX avec un nom donné et renvoie le contenu sous forme de chaîne de caractères.
    fun readGPXFile(context: Context, fileName: String): String {
        val file = File(context.filesDir, fileName)
        return file.readText()
    }

}