package com.example.projet_smartphone

import com.google.android.gms.maps.model.LatLng

data class Point(val latLng: LatLng, val couleur: String, val numero : Int) {

    override fun toString(): String {
        return "Point(latitude=${latLng.latitude}, longitude=${latLng.longitude}, couleur='$couleur', id=$numero)"
    }

}