package se.fork.bgrreading.extensions

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import se.fork.bgrreading.util.ReusableDisposable
import java.util.concurrent.TimeUnit

fun <T> Observable<T>.delayEach(interval: Long, timeUnit: TimeUnit): Observable<T> =
    Observable.zip(
        this,
        Observable.interval(interval, timeUnit),
        BiFunction { item, _ -> item }
    )

operator fun ReusableDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}

fun Disposable.addTo(androidDisposable: ReusableDisposable): Disposable
        = apply { androidDisposable.add(this) }