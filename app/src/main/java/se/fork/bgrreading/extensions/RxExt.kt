package se.fork.bgrreading.extensions

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

fun <T> Observable<T>.delayEach(interval: Long, timeUnit: TimeUnit): Observable<T> =
    Observable.zip(
        this,
        Observable.interval(interval, timeUnit),
        BiFunction { item, _ -> item }
    )