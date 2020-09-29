package se.fork.bgrreading.data

import android.util.SparseArray
import se.fork.bgrreading.data.remote.Session

class TimeLapse {
    var frameRate : Int = 30
    var maxIndex : Int = 0
    val movements: SparseArray<MovementSnapshot> = SparseArray()

    companion object {
        fun from(session: Session, frameRate: Int) : TimeLapse {
            val obj = TimeLapse()

            return obj
        }
    }
}