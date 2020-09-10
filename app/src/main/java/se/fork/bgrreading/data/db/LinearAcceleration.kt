package se.fork.bgrreading.data.db

import android.hardware.SensorEvent
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "linear_acceleration", indices = arrayOf(Index(value = ["timestamp"])))
data class LinearAcceleration(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val timestamp : Long,
    @ColumnInfo(name = "x_acc")
    val xAcc : Float,
    @ColumnInfo(name = "y_acc")
    val yAcc : Float,
    @ColumnInfo(name = "z_acc")
    val zAcc : Float
) {
    companion object {
        fun from(event: SensorEvent) : LinearAcceleration {
            return LinearAcceleration(timestamp = event.timestamp,
            xAcc = event.values.get(0),
            yAcc = event.values.get(1),
            zAcc = event.values.get(2))
        }
    }
}