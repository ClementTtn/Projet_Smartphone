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


        var trameNMEA = "\$GPRMC,104339.271,A,4609.055502,N,00110.158403,W,0.0,114.4,300323,,,*33\n" +
                "\$IIVHW,114.4,T,114.4,M,0.0,N,0.0,K*55\n" +
                "\$GPVTG,114.4,T,114.4,M,0.0,N,0.0,K*4E\n" +
                "\$IIHDT,114.4,T*22\n" +
                "\$GPGLL,4609.055502,N,00110.158403,W,104339.271,A*29\n" +
                "\$GPGGA,104339.271,4609.055502,N,00110.158403,W,1,4,0.6,2.0,M,,,,*22\n" +
                "\$GPGSA,A,3,8,11,15,22,,,,,,,,,3.3,0.6,0.6*0E\n" +
                "\$GPZDA,104339.271,30,03,2023,02,00*5F\n" +
                "\$IIVBW,-0.3,-0.3,A,-0.3,-0.2,A,-0.1,A,-0.7,A*44\n" +
                "!AIVDO,1,1,,A,17PaewhP00wraKPJJ6>lMkU>0000,0*6F\n" +
                "!AIVDM,1,1,,A,17PaewhP00wraKPJJ6>lMkU>0000,0*6D\n" +
                "\$WIMWD,9.2,T,9.2,M,2.0,N,1.0,M*59\n" +
                "\$WIMWV,254.9,R,2.0,N,A*2B\n" +
                "\$IIMTW,13.2,C*13\n" +
                "\$SDDPT,1.7,0.3*52\n" +
                "\$SDDBT,5.4,f,1.7,M,0.9,F*08\n" +
                "\$INWPL,4609.055502,N,00110.158403,W,wpt*3D\n" +
                "!AIVDO,2,1,9,A,57Paewh00001<To7;?@plD5<Tl0000000000000U1@:550w:c2TnA3QF,0*26\n" +
                "!AIVDO,2,2,9,A,@00000000000002,2*5D\n" +
                "!AIVDM,2,1,9,A,57Paewh00001<To7;?@plD5<Tl0000000000000U1@:550w:c2TnA3QF,0*24\n" +
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