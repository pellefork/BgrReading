package se.fork.bgrreading.data.db

import android.content.Context
import androidx.annotation.MainThread
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Completable
import se.fork.bgrreading.managers.LocationManager
import se.fork.bgrreading.managers.MotionManager
import timber.log.Timber
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

    /**
     * Subscribes to location updates.
     */
    @MainThread
    fun startLocationUpdates() = locationManager.startLocationUpdates()

    /**
     * Un-subscribes from location updates.
     */
    @MainThread
    fun stopLocationUpdates() = locationManager.stopLocationUpdates()
    /**
     * Subscribes to location updates.
     */
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

    /**
     * Un-subscribes from location updates.
     */
    @MainThread
    fun stopMotionSensorUpdates() = motionManager.stopMotionSensorUpdates()

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
                    .also {

                    }
            }
        }
    }

}