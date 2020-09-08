package se.fork.bgrreading.data.db

import android.content.Context
import androidx.annotation.MainThread
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.data.db.MyLocationDatabase
import se.fork.bgrreading.managers.LocationManager
import se.fork.bgrreading.managers.MotionManager
import java.util.concurrent.ExecutorService

class LocationRepository private constructor(
    private val myLocationDatabase: MyLocationDatabase,
    private val locationManager: LocationManager,
    private val motionManager: MotionManager,
    private val executor: ExecutorService
) {
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

    /**
     * Un-subscribes from location updates.
     */
    @MainThread
    fun stopMotionSensorUpdates() = motionManager.stopMotionSensorUpdates()

    companion object {
        @Volatile private var INSTANCE: LocationRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): LocationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationRepository(
                    MyLocationDatabase.getInstance(context),
                    LocationManager.getInstance(context),
                    MotionManager.getInstance(context),
                    executor)
                    .also { INSTANCE = it }
            }
        }
    }

}