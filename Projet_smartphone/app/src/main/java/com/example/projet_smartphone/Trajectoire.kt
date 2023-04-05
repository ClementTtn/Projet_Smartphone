package com.example.projet_smartphone

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.FileInputStream


class Trajectoire(val nom: String, val listePoints: List<Point>) {
    companion object {
        private var counter = 1
    }

    val id = counter++

    fun generateGPX(): String {
        val dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

        val gpxHeader = """
        |<?xml version="1.0" encoding="UTF-8"?>
        |<gpx version="1.1" creator="DroneTrajectoryApp"
        |    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |    xmlns="http://www.topografix.com/GPX/1/1"
        |    xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
        |<metadata>
        |    <time>$dateTime</time>
        |</metadata>
        |<trk>
        |    <name>Drone Trajectory</name>
        |    <trkseg>
    """.trimMargin()

        val gpxFooter = """
        |    </trkseg>
        |</trk>
        |</gpx>
    """.trimMargin()

        val gpxPoints = listePoints.joinToString(separator = "\n") { point ->
            "|<trkpt lat=\"${point.latLng.latitude}\" lon=\"${point.latLng.longitude}\"></trkpt>"
        }.trimMargin()

        return gpxHeader + gpxPoints + gpxFooter
    }


    @Throws(IOException::class)
    fun saveGPXFile(context: Context, fileName: String) {
        val gpxData = generateGPX()

        // Écrire le fichier GPX dans le répertoire des fichiers de l'application
        try {
            val file = File(context.filesDir, fileName)
            file.writeText(gpxData)
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }




    @Throws(IOException::class)
    fun readGPXFile(context: Context, fileName: String): String {
        val gpxFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        return FileInputStream(gpxFile).use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        }
    }
}
