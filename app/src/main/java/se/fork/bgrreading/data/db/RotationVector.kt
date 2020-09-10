package se.fork.bgrreading.data.db

import android.hardware.SensorEvent
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "rotation_vector", indices = arrayOf(Index(value = ["timestamp"])))
data class RotationVector(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val timestamp : Long,
    @ColumnInfo(name = "x_rot")
    val xRot : Float,
    @ColumnInfo(name = "y_rot")
    val yRot : Float,
    @ColumnInfo(name = "z_rot")
    val zRot : Float,
    val rot : Float,
    val heading : Float
) {
    companion object {
        fun from(event: SensorEvent) : RotationVector {
            return RotationVector(  timestamp = event.timestamp,
                                    xRot = event.values.get(0),
                                    yRot = event.values.get(1),
                                    zRot = event.values.get(2),
                                    rot = event.values.get(3),
                                    heading = event.values.get(4))
        }
    }
}