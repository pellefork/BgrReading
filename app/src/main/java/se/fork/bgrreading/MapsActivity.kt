package se.fork.bgrreading

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TimePicker
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import se.fork.bgrreading.data.remote.Session
import timber.log.Timber

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        if (intent.hasExtra("session")) {
            session = intent.getSerializableExtra("session") as Session
            Timber.d("onStart: Got Session $session")
        } else {
            Toast.makeText(this, "No session data provided", Toast.LENGTH_SHORT).show()
        }
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
        map = googleMap
        zoomToSession()
        drawPolyLine()
    }

    private fun zoomToSession() {
        val boundsBuilder = LatLngBounds.builder()
        for (location in session.locations) {
            boundsBuilder.include(LatLng(location.latitude, location.longitude))
        }
        val bounds = boundsBuilder.build()
        val size = distanceBetween(bounds.southwest, bounds.northeast)

        if (size > 50f) {
            Timber.d("zoomToSession: session greater than 50 m: $size")
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
        } else {
            Timber.d("zoomToSession: session less than 50 m: $size")
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 15.0f))
        }
    }

    private fun distanceBetween(a: LatLng, b: LatLng) : Float {
        val result = FloatArray(1)
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result)
        return result.get(0)
    }

    private fun drawPolyLine() {
        val polyLineOptions = PolylineOptions()
            .addAll(session.locations.map {
                LatLng(it.latitude, it.longitude)
            })
        map.addPolyline(polyLineOptions)
    }
}