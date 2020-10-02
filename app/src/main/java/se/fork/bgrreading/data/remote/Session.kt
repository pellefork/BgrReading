package se.fork.bgrreading.data.remote

import se.fork.bgrreading.data.db.LinearAcceleration
import se.fork.bgrreading.data.db.MyLocationEntity
import se.fork.bgrreading.data.db.RotationVector
import java.io.Serializable
import java.util.*

data class Session(
    var id : String = "",
    var locations : List<MyLocationEntity> = listOf(),
    var accelerations: List<LinearAcceleration> = listOf(),
    var rotations: List<RotationVector> = listOf()
) : Serializable {
}