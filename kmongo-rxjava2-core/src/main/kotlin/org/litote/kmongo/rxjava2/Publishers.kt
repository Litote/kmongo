/*
 * Copyright (C) 2017/2018 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litote.kmongo.rxjava2

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.reactivestreams.Publisher


/**
 * Provides a [Maybe] from this [Publisher].
 */
fun <T> Publisher<T>.maybe(): Maybe<T> = toObservable().firstElement()

/**
 * Provides a [SingleResult] from this [Publisher].
 */
fun <T> Publisher<T>.single(): Single<T> = Single.fromPublisher(this)

/**
 * Provides a [Completable] from this [Publisher].
 */
fun Publisher<*>.completable(): Completable = Completable.fromPublisher(this)

/**
 * Subscribes to and awaits the termination of this Completable instance in a blocking manner and
 * rethrows any exception emitted.
 * <p>
 * <img width="640" height="432" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/Completable.blockingAwait.png" alt="">
 * <dl>
 *  <dt><b>Scheduler:</b></dt>
 *  <dd>{@code blockingAwait} does not operate by default on a particular {@link Scheduler}.</dd>
 *  <dt><b>Error handling:</b></dt>
 *  <dd>If the source signals an error, the operator wraps a checked {@link Exception}
 *  into {@link RuntimeException} and throws that. Otherwise, {@code RuntimeException}s and
 *  {@link Error}s are rethrown as they are.</dd>
 * </dl>
 * @throws RuntimeException wrapping an InterruptedException if the current thread is interrupted
 */
fun Publisher<*>.blockingAwait() = completable().blockingAwait()

/**
 * Waits in a blocking fashion until the current Single signals a success value (which is returned) or
 * an exception (which is propagated).
 * <dl>
 * <dt>**Scheduler:**</dt>
 * <dd>`blockingGet` does not operate by default on a particular [Scheduler].</dd>
 * <dt>**Error handling:**</dt>
 * <dd>If the source signals an error, the operator wraps a checked [Exception]
 * into [RuntimeException] and throws that. Otherwise, `RuntimeException`s and
 * [Error]s are rethrown as they are.</dd>
</dl> *
 * @return the success value
 */
fun <T> Publisher<T>.blockingGet(): T = single().blockingGet()

/**
 * Provides an Observable from a [Publisher].
 */
fun <T> Publisher<T>.toObservable(): Observable<T> = Observable.fromPublisher(this)