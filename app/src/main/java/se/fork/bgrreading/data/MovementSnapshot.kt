package se.fork.bgrreading.data

import se.fork.bgrreading.data.db.LinearAcceleration
import se.fork.bgrreading.data.db.MyLocationEntity
import se.fork.bgrreading.data.db.RotationVector

data class MovementSnapshot (
    val frameNo : Int = -1,
    val frameStart : Long = -1,
    val frameEnd : Long = -1,
    val position: MyLocationEntity? = null,
    val acceleration : LinearAcceleration? = null,
    val rotation: RotationVector? = null
) {
    val isNonEmpty : Boolean = position != null || acceleration != null || rotation != null
}