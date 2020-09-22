package se.fork.bgrreading.data.db

import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.nio.channels.FileLock
import java.text.DateFormat
import java.util.Date
import java.util.UUID

/**
 * Data class for Location related data (only takes what's needed from
 * {@link android.location.Location} class).
 */
@Entity(tableName = "my_location_table")
data class MyLocationEntity(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var latitude: Double = -1.0,
    var longitude: Double = -1.0,
    var foreground: Boolean = true,
    var timestamp: Long = -1,
    var horizontalAccuracy: Float = -1f,
    var altitude: Double = -1.0,
    var bearing: Float = -1f,
    var bearingAccuracyDegrees: Float = -1f,
    var speed: Float = -1f,
    var speedAccuracyMetersPerSecond: Float = -1f,
    var verticalAccuracyMeters: Float = -1f,
    var hasAccuracy: Boolean = false,
    var hasAltitude: Boolean = false,
    var hasBearing: Boolean = false,
    var hasBearingAccuracy: Boolean = false,
    var hasSpeed: Boolean = false,
    var hasSpeedAccuracy: Boolean = false,
    var hasVerticalAccuracy: Boolean = false
) : Serializable {
    companion object {
        fun from(location: Location) : MyLocationEntity {
            val result = MyLocationEntity(
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = location.time,
                horizontalAccuracy = location.accuracy,
                altitude = location.altitude,
                bearing = location.bearing,
                speed = location.speed,
                hasAccuracy = location.hasAccuracy(),
                hasAltitude = location.hasAltitude(),
                hasBearing = location.hasBearing(),
                hasSpeed = location.hasSpeed()
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result.apply {
                    bearingAccuracyDegrees = location.bearingAccuracyDegrees
                    speedAccuracyMetersPerSecond = location.speedAccuracyMetersPerSecond
                    verticalAccuracyMeters = location.verticalAccuracyMeters
                    hasBearingAccuracy = location.hasBearingAccuracy()
                    hasSpeedAccuracy = location.hasSpeedAccuracy()
                    hasVerticalAccuracy = location.hasVerticalAccuracy()
                }
            }
            return result
        }
    }
}
