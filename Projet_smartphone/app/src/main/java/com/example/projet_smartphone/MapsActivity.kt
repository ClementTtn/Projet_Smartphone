package com.example.projet_smartphone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.projet_smartphone.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        var trameNMEA = "\$GPRMC,074624.248,A,3459.991297,S,13830.004029,E,5.2,22.2,300323,,,*0B\n" +
                "\$IIVHW,22.2,T,22.2,M,5.2,N,9.6,K*5D\n" +
                "\$GPVTG,22.2,T,22.2,M,5.2,N,9.6,K*46\n" +
                "\$IIHDT,22.2,T*10\n" +
                "\$GPGLL,3459.991297,S,13830.004029,E,074624.248,A*24\n" +
                "\$GPGGA,074624.248,3459.991297,S,13830.004029,E,1,4,1.3,2.0,M,,,,*2B\n" +
                "\$GPGSA,A,3,8,11,15,22,,,,,,,,,1.0,1.3,3.0*0E\n" +
                "\$GPZDA,074624.248,30,03,2023,02,00*5A\n" +
                "\$IIVBW,4.5,4.5,A,4.4,4.8,A,4.9,A,4.4,A*42\n" +
                "!AIVDO,1,1,,A,17PaewhP0lar0<AcvA60o@dh0000,0*4A\n" +
                "!AIVDM,1,1,,A,17PaewhP0lar0<AcvA60o@dh0000,0*48\n" +
                "\$WIMWD,175.0,T,175.0,M,27.8,N,14.3,M*51\n" +
                "\$WIMWV,146.9,R,23.3,N,A*1B\n" +
                "\$IIMTW,8.3,C*28\n" +
                "\$SDDPT,13.0,0.3*66\n" +
                "\$SDDBT,42.8,f,13.0,M,7.1,F*0C\n" +
                "!AIVDO,2,1,9,A,57Paewh00001<To7;?@plD5<Tl0000000000000U1@:550w7f2TnA3QF,0*2E\n" +
                "!AIVDO,2,2,9,A,@00000000000002,2*5D\n" +
                "!AIVDM,2,1,9,A,57Paewh00001<To7;?@plD5<Tl0000000000000U1@:550w7f2TnA3QF,0*2C\n" +
                "!AIVDM,2,2,9,A,@00000000000002,2*5F\n" +
                "\$IIRPM,E,1,0,10.5,A*7C\n" +
                "\$IIRPM,E,2,0,10.5,A*7F"

        val drone = Drone("Mon Drone")

        drone.parseNMEA(trameNMEA)
        System.out.println(drone.toString())
        System.out.println("====================================")
        System.out.println(drone.genereTrameNMEA())
        System.out.println("====================================")
        System.out.println("====================================")
        System.out.println(drone.toString())



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}