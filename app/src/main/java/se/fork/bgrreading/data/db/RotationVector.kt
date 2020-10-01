package se.fork.bgrreading.data.db

import android.hardware.SensorEvent
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "rotation_vector", indices = arrayOf(Index(value = ["timestamp"])))
data class RotationVector(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var timestamp : Long = 0,
    @ColumnInfo(name = "x_rot")
    var xRot : Float = 0.0f,
    @ColumnInfo(name = "y_rot")
    var yRot : Float = 0.0f,
    @ColumnInfo(name = "z_rot")
    var zRot : Float = 0.0f,
    var rot : Float = 0.0f,
    var heading : Float = 0.0f
) : Serializable {
    companion object {
        fun from(event: SensorEvent) : RotationVector {
            return RotationVector(  timestamp = System.currentTimeMillis(),
                                    xRot = event.values.get(0),
                                    yRot = event.values.get(1),
                                    zRot = event.values.get(2),
                                    rot = event.values.get(3),
                                    heading = event.values.get(4))
        }

        fun from(rotationList : List<RotationVector>) : RotationVector? {
            when (rotationList.size) {
                0 -> {
                    return null
                }
                1 -> {
                    return rotationList.get(0)
                }
                else -> {
                    return RotationVector(
                        timestamp = rotationList.minBy { x -> x.timestamp }!!.timestamp,
                        xRot = rotationList.map { it.xRot }.average().toFloat(),
                        yRot = rotationList.map { it.yRot }.average().toFloat(),
                        zRot = rotationList.map { it.zRot }.average().toFloat(),
                        rot = rotationList.map { it.rot }.average().toFloat(),
                        heading = rotationList.map { it.heading }.average().toFloat()
                    )
                }
            }
        }
    }
}