package se.fork.bgrreading.data.db

import android.content.Context
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import se.fork.bgrreading.data.remote.Session
import se.fork.bgrreading.data.remote.SessionHeader
import se.fork.bgrreading.managers.DeviceUtil
import se.fork.bgrreading.managers.LocationManager
import se.fork.bgrreading.managers.MotionManager
import se.fork.bgrreading.managers.SessionBuilder
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

class BgrReadingRepository private constructor(
    private val bgrReadingDatabase: BgrReadingDatabase,
    private val locationManager: LocationManager,
    private val motionManager: MotionManager,
    private val executor: ExecutorService
) {
    private val firebaseDb = FirebaseDatabase.getInstance()

    fun testFirebase() {
        val dbRef = firebaseDb.reference
        dbRef.setValue("Test successful at ${Date().toString()}")
    }

    val receivingLocationUpdates: LiveData<Boolean> = locationManager.receivingLocationUpdates

    @MainThread
    fun startLocationUpdates() = locationManager.startLocationUpdates()

    @MainThread
    fun stopLocationUpdates() = locationManager.stopLocationUpdates()

    @MainThread
    fun startMotionSensorUpdates() = motionManager.startMotionSensorUpdates()

    fun clearDatabase() {
        executor.execute{
            bgrReadingDatabase.linearAcceleretionDao().clearTable()
            bgrReadingDatabase.locationDao().clearTable()
            bgrReadingDatabase.rotationVectorDao().clearTable()
        }
    }

    fun addAcceleration(acc : LinearAcceleration) {
        Timber.d("addAcceleration: $acc")
        executor.execute{
            bgrReadingDatabase.linearAcceleretionDao().addAcceleration(acc)
        }
    }

    fun addRotationVector(vector: RotationVector) {
        Timber.d("addRotationVector: $vector")
        executor.execute{
            bgrReadingDatabase.rotationVectorDao().addRotationVector(vector)
        }
    }

    fun addLocation(location: MyLocationEntity)  {
        executor.execute{
            bgrReadingDatabase.locationDao().addLocation(location)
        }
    }

    fun addLocations(locations: List<MyLocationEntity>) {
        executor.execute{
            bgrReadingDatabase.locationDao().addLocations(locations)
        }
    }

    @MainThread
    fun stopMotionSensorUpdates() = motionManager.stopMotionSensorUpdates()

    fun buildAndUploadSession(context: Context, onSuccess : () -> Unit, onError : (DatabaseError) -> Unit ) {
        SessionBuilder.build(context, {
            onSessionAggregated(context, it, onSuccess, onError)
        }, {
            onSessionAggregationError(it, context)
        })
    }

    fun onSessionAggregated(context: Context, session: Session, onSuccess : () -> Unit, onError : (DatabaseError) -> Unit ) {
        uploadSession(context, session, onSuccess, onError)
    }

    fun onSessionAggregationError(e:Throwable, context: Context) {
        Toast.makeText(context, "Could not aggregate session: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }

    private fun createSessionHeader(context: Context, session: Session) : SessionHeader {
        val user = FirebaseAuth.getInstance().currentUser
        val deviceName = DeviceUtil.getDeviceName()
        val deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)

        val startTimes = listOf<Long>(
            session.locations.get(0).timestamp,
            session.accelerations.get(0).timestamp,
            session.rotations.get(0).timestamp
        )
        val startDate = Date(startTimes.min() ?: 0)
        val endDate = Date(startTimes.max() ?: 0)
        val name = "Session " + SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(startDate)

        val header = SessionHeader(id = session.id,
            startDate = startDate,
            endDate = endDate,
            name = name,
            userName = user?.displayName,
            deviceId = deviceId,
            deviceName = deviceName,
            nLocations = session.locations.size,
            nAccelerations = session.accelerations.size,
            nRotations = session.rotations.size)

        return header
    }

    fun deleteSession(sessionId : String) {
        val dbRef = firebaseDb.reference
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            dbRef.child("users").child(uid).child("sessions").child(sessionId).setValue(null)
        }
    }

    private fun uploadSession(context: Context, session: Session, onSuccess : () -> Unit, onError : (DatabaseError) -> Unit ) {
        Timber.d("uploadSession: $session")
        val dbRef = firebaseDb.reference
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        uid?.let {
            val key = dbRef.child("users").child(uid).child("sessions").push().key
            if (key != null) {
                session.id = key
                val header = createSessionHeader(context, session)
                dbRef.child("users").child(uid).child("sessionHeaders").child(key).setValue(header)
                dbRef.child("users").child(uid).child("sessions").child(key).setValue(session, object : DatabaseReference.CompletionListener{
                    override fun onComplete(error: DatabaseError?, ref: DatabaseReference) {
                        Timber.d("onComplete: $error")
                        if (error != null) {
                            onError(error)
                        } else {
                            onSuccess()
                        }
                    }
                })
            }
        }
    }

    companion object {
        @Volatile private var INSTANCE: BgrReadingRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): BgrReadingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BgrReadingRepository(
                    BgrReadingDatabase.getInstance(context),
                    LocationManager.getInstance(context),
                    MotionManager.getInstance(context),
                    executor)
                    .also { INSTANCE = it }
            }
        }
    }

}