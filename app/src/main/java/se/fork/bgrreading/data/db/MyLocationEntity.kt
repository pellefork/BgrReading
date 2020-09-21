package se.fork.bgrreading.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.text.DateFormat
import java.util.Date
import java.util.UUID

/**
 * Data class for Location related data (only takes what's needed from
 * {@link android.location.Location} class).
 */
@Entity(tableName = "my_location_table")
data class MyLocationEntity(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var foreground: Boolean = true,
    var date: Date = Date()
) : Serializable {

    override fun toString(): String {
        val appState = if (foreground) {
            "in app"
        } else {
            "in BG"
        }

        return "$latitude, $longitude $appState on " +
                "${DateFormat.getDateTimeInstance().format(date)}.\n"
    }
}
