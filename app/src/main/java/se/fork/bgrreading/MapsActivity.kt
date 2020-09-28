package se.fork.bgrreading

import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*
import se.fork.bgrreading.adapters.HorizontalGauge
import se.fork.bgrreading.data.remote.Session
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var session: Session
    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        setupGauges()

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
        testGauge()
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

    // -------------------------------------- Sensor reading data rendering ---------------------------------------------

    lateinit var xAccGauge: HorizontalGauge
    lateinit var yAccGauge: HorizontalGauge
    lateinit var zAccGauge: HorizontalGauge

    lateinit var xRotGauge: HorizontalGauge
    lateinit var yRotGauge: HorizontalGauge
    lateinit var zRotGauge: HorizontalGauge
    lateinit var rotGauge: HorizontalGauge

    private fun setupGauges() {
        xAccGauge = HorizontalGauge(acc_x)
        yAccGauge = HorizontalGauge(acc_y)
        zAccGauge = HorizontalGauge(acc_z)

        xRotGauge = HorizontalGauge(rot_x)
        yRotGauge = HorizontalGauge(rot_y)
        zRotGauge = HorizontalGauge(rot_z)
        rotGauge = HorizontalGauge(rot)
    }

    private fun testGauge() {
        xAccGauge.range = 100f

        Observable.just(45, -20, 80, 50, -34, -45, -30, 100)
            .zipWith(
                Observable.interval(1500, TimeUnit.MILLISECONDS),
                BiFunction<Int, Long, Int?> { item: Int?, interval: Long? -> item!! })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                    x: Int? -> x?.let {
                    xAccGauge.setValue(x.toFloat())
                }
            }
            .addTo(compositeDisposable)
    }
}