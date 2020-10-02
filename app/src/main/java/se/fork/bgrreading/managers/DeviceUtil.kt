package se.fork.bgrreading.managers

import android.os.Build

object DeviceUtil {
    /** Returns the consumer friendly device name  */
    fun getDeviceName(): String? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.capitalize()
        } else manufacturer.toString().capitalize() + " " + model
    }
}