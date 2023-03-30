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


        var trameNMEA = "\$GPRMC,101952.670,A,4607.555721,N,00113.106932,W,5.0,15.0,300323,,,*0A\n" +
                "\$IIVHW,15.0,T,15.0,M,5.0,N,9.3,K*5A\n" +
                "\$GPVTG,15.0,T,15.0,M,5.0,N,9.3,K*41\n" +
                "\$IIHDT,15.0,T*16\n" +
                "\$GPGLL,4607.555721,N,00113.106932,W,101952.670,A*21\n" +
                "\$GPGGA,101952.670,4607.555721,N,00113.106932,W,1,4,0.8,2.0,M,,,,*24\n" +
                "\$GPGSA,A,3,8,11,15,22,,,,,,,,,1.4,0.8,1.4*06\n" +
                "\$GPZDA,101952.670,30,03,2023,02,00*58\n" +
                "\$IIVBW,4.1,4.7,A,4.4,4.8,A,4.5,A,4.9,A*45\n" +
                "!AIVDO,1,1,,A,17PaewhP0jwrK26JI;a@UPO`0000,0*1A\n" +
                "!AIVDM,1,1,,A,17PaewhP0jwrK26JI;a@UPO`0000,0*18\n" +
                "\$WIMWD,295.4,T,295.4,M,4.4,N,2.3,M*5B\n" +
                "\$WIMWV,323.1,R,7.2,N,A*25\n" +
                "\$IIMTW,7.1,C*25\n" +
                "\$SDDPT,12.4,0.3*63\n" +
                "\$SDDBT,40.7,f,12.4,M,6.8,F*0C\n" +
                "!AIVDO,2,1,9,A,57Paewh00001<To7;?@plD5<Tl0000000000000U1@:550w:C2TnA3QF,0*06\n" +
                "!AIVDO,2,2,9,A,@00000000000002,2*5D\n" +
                "!AIVDM,2,1,9,A,57Paewh00001<To7;?@plD5<Tl0000000000000U1@:550w:C2TnA3QF,0*04\n" +
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


        // Add a marker in Sydney and move the camera
        val sydney = LatLng(drone.positionActuel.latitude, drone.positionActuel.longitude)
        mMap.addMarker(MarkerOptions().position(sydney).title(drone.nom))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}