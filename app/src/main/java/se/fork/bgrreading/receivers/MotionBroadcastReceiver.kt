
package se.fork.bgrreading.receivers

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult
import se.fork.bgrreading.data.db.BgrReadingRepository
import se.fork.bgrreading.data.db.MyLocationEntity
import timber.log.Timber
import java.util.Date
import java.util.concurrent.Executors

/**
 * Receiver for handling location updates.
 *
 * For apps targeting API level O and above
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates in the background. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should NOT be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
class MotionBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("onReceive() context:$context, intent:$intent")

        if (intent.action == ACTION_PROCESS_UPDATES) {
            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations.map { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val foreground = isAppInForeground(context)
                    val date = Date(location.time)
                    Timber.d("Received location $date $latitude $longitude")

                    MyLocationEntity(
                        latitude = latitude,
                        longitude = longitude,
                        foreground = foreground,
                        timestamp = location.time
                    )

                }
                if (locations.isNotEmpty()) {
                    BgrReadingRepository.getInstance(context, Executors.newSingleThreadExecutor())
                        .addLocations(locations)
                }
            }
        }
    }

    // Note: This function's implementation is only for debugging purposes. If you are going to do
    // this in a production app, you should instead track the state of all your activities in a
    // process via android.app.Application.ActivityLifecycleCallbacks's
    // unregisterActivityLifecycleCallbacks(). For more information, check out the link:
    // https://developer.android.com/reference/android/app/Application.html#unregisterActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        appProcesses.forEach { appProcess ->
            if (appProcess.importance ==
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == context.packageName) {
                return true
            }
        }
        return false
    }

    companion object {
        const val ACTION_PROCESS_UPDATES = "se.fork.bgrreading.receivers.MotionBroadcastReceiver.ACTION_PROCESS_UPDATES"
    }
}
