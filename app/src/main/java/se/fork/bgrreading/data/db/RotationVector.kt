package se.fork.bgrreading.data.db

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
    val yAcc : Float,
    @ColumnInfo(name = "z_rot")
    val zAcc : Float,
    val rot : Float,
    val heading : Float
) {
}