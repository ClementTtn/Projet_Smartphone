package com.example.projet_smartphone

import com.google.android.gms.maps.model.LatLng
import android.os.Parcel
import android.os.Parcelable

data class Point(var latLng: LatLng, val couleur: String, val numero: Int) : Parcelable {

    override fun toString(): String {
        return "Point(latitude=${latLng.latitude}, longitude=${latLng.longitude}, couleur='$couleur', id=$numero)"
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(latLng, flags)
        parcel.writeString(couleur)
        parcel.writeInt(numero)
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            val latLng = parcel.readParcelable(LatLng::class.java.classLoader) ?: LatLng(0.0, 0.0)
            val couleur = parcel.readString() ?: ""
            val numero = parcel.readInt()
            return Point(latLng, couleur, numero)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }
}
