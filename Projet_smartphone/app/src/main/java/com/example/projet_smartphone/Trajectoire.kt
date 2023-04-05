package com.example.projet_smartphone

class Trajectoire(val nom: String, val listePoints: List<Point>) {
    companion object {
        private var counter = 1
    }

    val id = counter++
}