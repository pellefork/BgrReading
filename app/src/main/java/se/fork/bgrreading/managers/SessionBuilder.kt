package se.fork.bgrreading.managers

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import se.fork.bgrreading.data.db.*
import se.fork.bgrreading.data.remote.Session
import timber.log.Timber
import java.util.*
import java.util.concurrent.Executors

object SessionBuilder {
    lateinit var locationList : List<MyLocationEntity>
    lateinit var accelerationList : List<LinearAcceleration>
    lateinit var rotationList : List<RotationVector>

    fun build(context : Context, onAllDone : (Session) -> Unit, onError : (Throwable) -> Unit) {
        BgrReadingDatabase.getInstance(context).locationDao().getLocations()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .subscribe({
                locationList = it
                Timber.d("getLocations done: $it")
                locationsDone(context, onAllDone, onError)
            }, {
                Timber.e(it, "Could not getLocations")
                onError(it)
            })
            .addTo(CompositeDisposable())
    }

    private fun locationsDone(context : Context, onAllDone : (Session) -> Unit, onError : (Throwable) -> Unit) {
        BgrReadingDatabase.getInstance(context).linearAcceleretionDao().getAccelerations()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                accelerationList = it
                Timber.d("getAccelerations done: $it")
                accelerationsDone(context, onAllDone, onError)
            }, {
                Timber.e(it, "Could not getAccelerations")
                onError(it)
            })
            .addTo(CompositeDisposable())
    }

    private fun accelerationsDone(context : Context, onAllDone : (Session) -> Unit, onError : (Throwable) -> Unit) {
        BgrReadingDatabase.getInstance(context).rotationVectorDao().getRotationVectors()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                rotationList = it
                Timber.d("getRotationVectors done: $it")
                val session = Session("", locationList, accelerationList, rotationList)
                onAllDone(session)
            }, {
                Timber.e(it, "Could not getRotationVectors")
                onError(it)
            })
            .addTo(CompositeDisposable())
    }
}