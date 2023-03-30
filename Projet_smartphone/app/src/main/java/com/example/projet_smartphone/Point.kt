package com.example.projet_smartphone

class Point(val latitude: Double, val longitude: Double, val couleur: String) {
    companion object {
        private var counter = 1
    }

    val id = counter++
    override fun toString(): String {
        return "Point(latitude=$latitude, longitude=$longitude, couleur='$couleur', id=$id)"
    }


}