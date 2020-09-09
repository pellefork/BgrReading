package se.fork.bgrreading.managers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import androidx.annotation.MainThread
import timber.log.Timber

class MotionManager private constructor(private val context: Context)  : SensorEventListener {
    val  sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val motionSensorTypes = listOf(Sensor.TYPE_ACCELEROMETER,
                                    Sensor.TYPE_GYROSCOPE,
                                    Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
                                    Sensor.TYPE_MOTION_DETECT,
                                    Sensor.TYPE_SIGNIFICANT_MOTION,
                                    Sensor.TYPE_LINEAR_ACCELERATION,
                                    Sensor.TYPE_POSE_6DOF,
                                    Sensor.TYPE_ROTATION_VECTOR)

    fun listSensors() : List<Sensor> {
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        Timber.d("Total sensors: ${deviceSensors.size}")
        deviceSensors.forEach{
            Timber.d("Sensor name: $it")
        }
        return deviceSensors
    }

    @MainThread
    fun startMotionSensorUpdates() {
        Timber.d("startMotionSensorUpdates")
        for (sensorType in motionSensorTypes) {
            val sensor = sensorManager.getDefaultSensor(sensorType)
            Timber.d("startMotionSensorUpdates: Registering listener for $sensor")
            val result = sensorManager.registerListener(this, sensor, SENSOR_DELAY_FASTEST)
            Timber.d("startMotionSensorUpdates: Registering successful? $result")
        }
    }

    @MainThread
    fun stopMotionSensorUpdates() {
        Timber.d("startMotionSensorUpdates")
        sensorManager.unregisterListener(this)
    }

    companion object {
        @Volatile private var INSTANCE: MotionManager? = null

        fun getInstance(context: Context): MotionManager {
            Timber.d("getInstance")
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MotionManager(context).also { INSTANCE = it }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Timber.d("onAccuracyChanged $sensor $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorName = event?.sensor?.name
        val accuracy = event?.accuracy
        val timestamp = event?.timestamp
        val values = event?.values?.toList()
        Timber.d("onSensorChanged $timestamp, $sensorName, $accuracy, $values")
    }
}