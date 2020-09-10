package se.fork.bgrreading.data.db

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
}