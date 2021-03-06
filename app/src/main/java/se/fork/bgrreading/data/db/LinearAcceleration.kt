package se.fork.bgrreading.data.db

import android.hardware.SensorEvent
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "linear_acceleration", indices = arrayOf(Index(value = ["timestamp"])))
data class LinearAcceleration(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var timestamp : Long = 0,
    @ColumnInfo(name = "x_acc")
    var xAcc : Float = 0f,
    @ColumnInfo(name = "y_acc")
    var yAcc : Float = 0f,
    @ColumnInfo(name = "z_acc")
    var zAcc : Float = 0f
) : Serializable {
    companion object {
        fun from(event: SensorEvent) : LinearAcceleration {
            return LinearAcceleration(timestamp = System.currentTimeMillis(),
            xAcc = event.values.get(0),
            yAcc = event.values.get(1),
            zAcc = event.values.get(2))
        }
        fun from(accelerationList : List<LinearAcceleration>) : LinearAcceleration? {
            when (accelerationList.size) {
                0 -> {
                    return null
                }
                1 -> {
                    return accelerationList.get(0)
                }
                else -> {
                    return LinearAcceleration(
                        timestamp = accelerationList.minBy { x -> x.timestamp }!!.timestamp,
                        xAcc = accelerationList.map { it.xAcc }.average().toFloat(),
                        yAcc = accelerationList.map { it.yAcc }.average().toFloat(),
                        zAcc = accelerationList.map { it.zAcc }.average().toFloat()
                    )
                }
            }
        }
    }
}