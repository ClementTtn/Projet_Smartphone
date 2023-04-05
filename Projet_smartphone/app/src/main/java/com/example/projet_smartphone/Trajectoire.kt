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


    override fun toString(): String {
        return "Trajectoire(id=$id, nom='$nom', listePoints=$listePoints)"
    }

}
