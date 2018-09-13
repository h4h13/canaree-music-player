package dev.olog.msc.utils.k.extension

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import hu.akarnokd.rxjava2.operators.FlowableTransformers
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

fun Disposable?.unsubscribe(){
    this?.let {
        if (!isDisposed){
            dispose()
        }
    }
}

fun <T> Observable<T>.asFlowable(backpressureStrategy: BackpressureStrategy = BackpressureStrategy.LATEST)
        : Flowable<T> {
    return this.toFlowable(backpressureStrategy)
}

fun <T> Flowable<T>.asLiveData() : LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this)
}

fun <T> Observable<T>.asLiveData(backpressureStrategy: BackpressureStrategy = BackpressureStrategy.LATEST)
        : LiveData<T> {

    return LiveDataReactiveStreams.fromPublisher(this.toFlowable(backpressureStrategy))

}

fun <T, R> Flowable<List<T>>.mapToList(mapper: (T) -> R): Flowable<List<R>> {
    return this.map { it.map { mapper(it) } }
}

fun <T, R> Observable<List<T>>.mapToList(mapper: (T) -> R): Observable<List<R>> {
    return this.map { it.map { mapper(it) } }
}

fun <T, R> Single<List<T>>.mapToList(mapper: ((T) -> R)): Single<List<R>> {
    return flatMap { Flowable.fromIterable(it).map(mapper).toList() }
}

fun <T> Observable<T>.debounceFirst(timeout: Long = 1L, unit: TimeUnit = TimeUnit.SECONDS): Observable<T>{
    return this.asFlowable()
            .compose(FlowableTransformers.debounceFirst(timeout, unit))
            .toObservable()
}

fun <T> Flowable<T>.debounceFirst(timeout: Long = 1L, unit: TimeUnit = TimeUnit.SECONDS): Flowable<T>{
    return this.compose(FlowableTransformers.debounceFirst(timeout, unit))
}

fun <T> Observable<T>.defer(): Observable<T> {
    return Observable.defer { this }
}

fun <T> Single<T>.defer(): Single<T> {
    return Single.defer { this }
}