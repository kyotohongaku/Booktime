package de.audiobook.player.misc

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.BiFunction

object Observables {

  inline fun <T1 : Any, T2 : Any, R : Any> combineLatest(
      source1: ObservableSource<T1>,
      source2: ObservableSource<T2>,
      crossinline combiner: (T1, T2) -> R
  ): Observable<R> {
    return Observable.combineLatest(source1, source2, BiFunction { t1, t2 -> combiner(t1, t2) })
  }
}
