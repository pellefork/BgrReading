package se.fork.bgrreading.data.remote

import se.fork.bgrreading.data.db.LinearAcceleration
import se.fork.bgrreading.data.db.MyLocationEntity
import se.fork.bgrreading.data.db.RotationVector
import java.util.*

data class Session(
    val name: String,
    val uploadDate: Date,
    val locations : List<MyLocationEntity>,
    val accelerations: List<LinearAcceleration>,
    val rotations: List<RotationVector>
) {
}