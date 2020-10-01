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
    private var timeZero: Long? = null
    private var timeEnd: Long? = null

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
            timeZero = startTimes.min()
            timeEnd = startTimes.max()
            Timber.d("buildMaps, timeZero = $timeZero timeEnd = $timeEnd")

            if (timeZero == null || timeEnd == null) return 0

            val lapseMillis = timeEnd!! - timeZero!!
            for (location in it.locations) {
                locationMap.put((location.timestamp - timeZero!!).toInt(), location)
            }
            for (acceleration in it.accelerations) {
                accelerationMap.put((acceleration.timestamp - timeZero!!).toInt(), acceleration)
            }
            for (rotation in it.rotations) {
                rotationMap.put((rotation.timestamp - timeZero!!).toInt(), rotation)
            }
            return lapseMillis.toInt()
        }
        return 0
    }

    private fun buildTimeLapse(lapseMillis: Int?) : TimeLapse {
        val timeLapse = TimeLapse()
        if (lapseMillis == null) return timeLapse
        timeLapse.frameRate = frameRate
        val millisPerFrame = 1000 / frameRate
        var frameStart = timeZero!!
        var frameEnd = frameStart + millisPerFrame
        var frameNo = 0
        while (frameStart < timeEnd!!) {
            val frameStartRelative = frameStart-timeZero!!
            val frameEndRelative = frameEnd-timeZero!!
            Timber.d("buildTimeLapse: frameStartRelative = $frameStartRelative, frameEndRelative = $frameEndRelative")
            val frameLocationList = locationMap.filter { it.key in frameStartRelative..frameEndRelative }.values.toList()
            val frameLocation = if (frameLocationList.size.equals(0)) null else frameLocationList.get(0)
            val frameAcceleration = LinearAcceleration.from( accelerationMap.filter { it.key in frameStartRelative..frameEndRelative }.values.toList())
            val frameRotation = RotationVector.from(rotationMap.filter { it.key in frameStartRelative..frameEndRelative }.values.toList())
            Timber.d("buildTimeLapse: from $frameStartRelative to $frameEndRelative loc $frameLocation acc $frameAcceleration rot $frameRotation")
            timeLapse.movements.add(MovementSnapshot(frameNo++, frameStart, frameEnd, frameLocation, frameAcceleration, frameRotation))
            frameStart = frameEnd + 1
            frameEnd += millisPerFrame
        }
        Timber.d("buildTimeLapse: Built $timeLapse")
        return timeLapse
    }
}