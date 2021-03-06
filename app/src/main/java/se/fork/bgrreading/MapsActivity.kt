package se.fork.bgrreading

import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.content_maps.*
import se.fork.bgrreading.adapters.HorizontalGauge
import se.fork.bgrreading.data.MovementSnapshot
import se.fork.bgrreading.data.TimeLapse
import se.fork.bgrreading.data.remote.Session
import se.fork.bgrreading.data.remote.SessionHeader
import se.fork.bgrreading.extensions.addTo
import se.fork.bgrreading.extensions.delayEach
import se.fork.bgrreading.extensions.onClickWithDebounce
import se.fork.bgrreading.managers.TimeLapseBuilder
import se.fork.bgrreading.util.ReusableDisposable
import timber.log.Timber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var currentSession: Session
    private lateinit var currentSessionHeader: SessionHeader
    private var currentPath = mutableListOf<LatLng>()
    private var currentPolyline: Polyline? = null
    private var reusableDisposable = ReusableDisposable()
    private var currentFrame: Int = 0

    private var isMapReady : Boolean = false
    private var isSessionReady : Boolean = false
    private var isPlaying : Boolean = false

    private val timeFormat = SimpleDateFormat("mm:ss.SS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        setSupportActionBar(findViewById(R.id.toolbar))

        setupGauges()
        fab.onClickWithDebounce {
            if (isPlaying) {
                isPlaying = false
                setFabStatus(fab.visibility, R.drawable.play)
                pausePlayback()
            } else {
                isPlaying = true
                setFabStatus(fab.visibility, R.drawable.pause)
                playSession()
            }
        }


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        if (intent.hasExtra("session")) {
            currentSessionHeader = intent.getSerializableExtra("session") as SessionHeader
            Timber.d("onStart: Got Session header $currentSessionHeader")
            fetchSession(currentSessionHeader.id)
        } else {
            Toast.makeText(this, "No session data provided", Toast.LENGTH_SHORT).show()
        }
        setFabStatus(View.GONE, R.drawable.play)
    }

    override fun onPause() {
        pausePlayback()
        super.onPause()
    }

    private fun fetchSession(id: String) {
        val dbRef = FirebaseDatabase.getInstance().reference
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        Timber.d("fetchSession $id $uid")
        uid?.let {
            val path = dbRef.child("users").child(uid).child("sessions").child(id)

            path.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Timber.d("onDataChange: dataSnapshot.getValue() ${dataSnapshot.getValue()}")
                    val session = dataSnapshot.getValue(Session::class.java)
                    Timber.d("fetchSession: Got $session")
                    session?.let {
                        currentSession = session
                        currentFrame = 0
                        isSessionReady = true
                        if (isMapReady) {
                            zoomToSession()
                            setFabStatus(View.VISIBLE, R.drawable.play)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.e(databaseError.toException(),"onCancelled: ${databaseError.details}")
                    Toast.makeText(this@MapsActivity, "Error fetching session: " + databaseError.message, Toast.LENGTH_SHORT).show()
                }
            })
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
        isMapReady = true
        if (isSessionReady) {
            zoomToSession()
            setFabStatus(View.VISIBLE, R.drawable.play)
        }
    }

    private fun zoomToSession() {
        val boundsBuilder = LatLngBounds.builder()
        for (location in currentSession.locations) {
            boundsBuilder.include(LatLng(location.latitude, location.longitude))
        }
        val bounds = boundsBuilder.build()
        val size = distanceBetween(bounds.southwest, bounds.northeast)
        Timber.d("zoomToSession: bounds = $bounds")

        if (size > 50f) {
            Timber.d("zoomToSession: session greater than 50 m: $size")
            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val padding = (height * 0.12).toInt() // offset from edges of the map 12% of screen

            val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)

            map.animateCamera(cu)
            // map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10))
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

    private fun setFabStatus(visibility: Int, icon: Int) {
        fab.visibility = visibility
        fab.setImageResource(icon)
    }

    private fun pausePlayback() {
        reusableDisposable.dispose()
    }

    private fun playSession() {
        val timeLapse = TimeLapseBuilder().apply {
            frameRate = 30
            session = currentSession
        }.build()

        playTimeLapse(timeLapse)
    }

    private fun playTimeLapse(timeLapse: TimeLapse) {
        val frameLapse = 1000 / timeLapse.frameRate
        Observable.fromArray(timeLapse.movements.subList(currentFrame, timeLapse.movements.lastIndex))
            .flatMapIterable { it }
            .delayEach(frameLapse.toLong(), TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("playTimeLapse: Rendering $it")
                renderFrame(it)
                currentFrame++
                Timber.d("playTimeLapse rendered $currentFrame of ${timeLapse.movements.lastIndex}")
                if (currentFrame.equals(timeLapse.movements.lastIndex)) {
                    isPlaying = false
                    currentFrame = 0
                    setFabStatus(View.VISIBLE, R.drawable.play)
                }
            }, {
                Timber.e(it, "playTimeLapse: Went wrong")
            })
            .addTo(reusableDisposable)
    }

    private fun renderPosition(frame: MovementSnapshot) {
        val position = frame.position
        position?.let {
            val loc = LatLng(position.latitude, position.longitude)
            currentPath.add(loc)
            currentPolyline?.let {
                currentPolyline?.remove()
                currentPolyline = null
            }
            if (currentPath.size > 1) {
                val polyLineOptions = PolylineOptions()
                    .addAll(currentPath)
                map.addPolyline(polyLineOptions)
                renderAccuracyCircle(frame)
            }
            bearing_pos.findViewById<ImageView>(R.id.arrow).rotation = it.bearing
            renderSpeed(position.speed * 3.6f)
        }
    }

    private fun renderAccuracyCircle(frame: MovementSnapshot) {
        frame.position?.let {
            val circleOptions = CircleOptions()
                .strokeWidth(1f)
                .strokeColor(R.color.colorPrimary)
                .center(LatLng(it.latitude, it.longitude))
                .radius(it.horizontalAccuracy.toDouble()) // In meters

            val circle = map.addCircle(circleOptions)
        }
    }

    private fun renderFrame(frame: MovementSnapshot) {
        renderTime(Date(frame.frameStart - currentSessionHeader.startDate.time))
        renderPosition(frame)
        renderAcceleration(frame)
        renderRotation(frame)
    }

    private fun renderTime(time : Date) {
        time_text.text = timeFormat.format(time)
    }

    private fun renderSpeed(speed: Float) {
        speed_text.text = getString(R.string.format_speed).format(DecimalFormat("##.#").format(speed))
    }

    // -------------------------------------- Sensor reading data rendering ---------------------------------------------

    lateinit var xAccGauge: HorizontalGauge
    lateinit var yAccGauge: HorizontalGauge
    lateinit var zAccGauge: HorizontalGauge

    lateinit var xRotGauge: HorizontalGauge
    lateinit var yRotGauge: HorizontalGauge
    lateinit var zRotGauge: HorizontalGauge
    lateinit var rotGauge: HorizontalGauge

    private fun renderAcceleration(frame: MovementSnapshot) {
        frame.acceleration?.let {
            xAccGauge.setValue(it.xAcc)
            yAccGauge.setValue(it.yAcc)
            zAccGauge.setValue(it.zAcc)
        }
    }

    private fun renderRotation(frame: MovementSnapshot) {
        frame.rotation?.let {
            xRotGauge.setValue(it.xRot)
            yRotGauge.setValue(it.yRot)
            zRotGauge.setValue(it.zRot)
            rotGauge.setValue(it.rot)
            bearing_rot.findViewById<ImageView>(R.id.arrow).rotation = it.heading
        }
    }

    private fun setupGauges() {
        xAccGauge = HorizontalGauge(acc_x).apply {
            range = 10f
            legend = "X Acc"
        }
        xAccGauge.setValue(0f)

        yAccGauge = HorizontalGauge(acc_y).apply {
            range = 10f
            legend = "Y Acc"
        }
        yAccGauge.setValue(0f)

        zAccGauge = HorizontalGauge(acc_z).apply {
            range = 10f
            legend = "Z Acc"
        }
        zAccGauge.setValue(0f)

        xRotGauge = HorizontalGauge(rot_x).apply {
            range = 1f
            legend = "X Rot"
        }
        xRotGauge.setValue(0f)

        yRotGauge = HorizontalGauge(rot_y).apply {
            range = 1f
            legend = "Y Rot"
        }
        yRotGauge.setValue(0f)

        zRotGauge = HorizontalGauge(rot_z).apply {
            range = 1f
            legend = "Z Rot"
        }
        zRotGauge.setValue(0f)

        rotGauge = HorizontalGauge(rot).apply {
            range = 1f
            legend = "Rot"
        }
        rotGauge.setValue(0f)
    }

    private fun testGauge() {
        xAccGauge.range = 100f
        // xAccGauge.legend = "X Acc"

        Observable.just(45, -20, 80, 0, -34, -45, -30, 100, -100)
            .zipWith(
                Observable.interval(1500, TimeUnit.MILLISECONDS),
                BiFunction<Int, Long, Int?> { item: Int?, interval: Long? -> item!! })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { x: Int? -> x?.let {
                    xAccGauge.setValue(x.toFloat())
                }
            }
            .addTo(reusableDisposable)
    }
}