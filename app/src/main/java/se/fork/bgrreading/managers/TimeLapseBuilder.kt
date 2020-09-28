package se.fork.bgrreading.managers

import se.fork.bgrreading.data.MovementSnapshot
import se.fork.bgrreading.data.TimeLapse
import se.fork.bgrreading.data.db.LinearAcceleration
import se.fork.bgrreading.data.db.MyLocationEntity
import se.fork.bgrreading.data.db.RotationVector
import se.fork.bgrreading.data.remote.Session
import timber.log.Timber

class TimeLapseBuilder {
    var frameRate: Int = 30
    var session: Session? = null

    fun build() : TimeLapse {
        val lapseMillis = buildMaps()
        val timeLapse = buildTimeLapse(lapseMillis)
        Timber.d("build $timeLapse")
        return timeLapse
    }

    private val locationMap = hashMapOf<Int, MyLocationEntity>()
    private val accelerationMap = hashMapOf<Int, LinearAcceleration>()
    private val rotationMap = hashMapOf<Int, RotationVector>()

    private fun buildMaps() : Int? {
        session?.let {
            val startTimes = listOf<Long>(
                it.locations.get(0).timestamp,
                it.accelerations.get(0).timestamp,
                it.rotations.get(0).timestamp
            )
            val timeZero = startTimes.min()
            val timeEnd = startTimes.max()
            Timber.d("buildMaps, timeZero = $timeZero")

            if (timeZero == null || timeEnd == null) return 0

            val lapseMillis = timeEnd - timeZero
            for (location in it.locations) {
                locationMap.put((location.timestamp - timeZero).toInt(), location)
            }
            for (acceleration in it.accelerations) {
                accelerationMap.put((acceleration.timestamp - timeZero).toInt(), acceleration)
            }
            for (rotation in it.rotations) {
                rotationMap.put((rotation.timestamp - timeZero).toInt(), rotation)
            }
            return lapseMillis.toInt()
        }
        return 0
    }

    private fun buildTimeLapse(frames: Int?) : TimeLapse {
        val timeLapse = TimeLapse().apply {
            frameRate = frameRate
        }
        if (frames == null) return timeLapse

        for (frameNo in 0..frames) {
            val frame = MovementSnapshot(frameNo = frameNo,
                                        position = locationMap.get(frameNo),
                                        acceleration = accelerationMap.get(frameNo),
                                        rotation = rotationMap.get(frameNo))
            if (frame.isNonEmpty) {
                timeLapse.movements.append(frameNo, frame)
            }
        }
        return timeLapse
    }
}