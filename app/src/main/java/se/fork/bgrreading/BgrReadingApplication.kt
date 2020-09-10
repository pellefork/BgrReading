package se.fork.bgrreading

import android.app.Application
import com.facebook.stetho.Stetho
import se.fork.bgrreading.data.db.BgrReadingRepository
import timber.log.Timber
import java.util.concurrent.Executors

class BgrReadingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else ReleaseTree())
        Timber.d("onCreate: Timber working!!")
        Stetho.initializeWithDefaults(this)
        Timber.d("onCreate: Stetho initialized")

        bgrReadingRepository = BgrReadingRepository.getInstance(this, Executors.newSingleThreadExecutor())
    }

    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // No-op
        }
    }

    companion object{
        lateinit var bgrReadingRepository : BgrReadingRepository
    }
}

