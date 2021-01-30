/*
 * Copyright (C) 2016/2021 Litote
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

import com.mongodb.internal.async.SingleResultCallback
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * RX wrapper for SingleResultCallback<T>
 *
 * @param callback lambda that will be supplied to wrapped SingleResultCallback<T>
 * @param <T>            the default target type of the collection to return
 */
inline fun <T> maybeResult(crossinline callback: (SingleResultCallback<T>) -> Unit): Maybe<T> {
    return Maybe.create<T> { emitter ->
        callback(SingleResultCallback { result: T?, throwable: Throwable? ->
            when {
                throwable != null -> emitter.onError(throwable)
                result != null -> emitter.onSuccess(result)
                else -> emitter.onComplete()
            }
        })
    }
}

/**
 * RX wrapper for SingleResultCallback<T>
 *
 * @param callback lambda that will be supplied to wrapped SingleResultCallback<T>
 * @param <T>            the default target type of the collection to return
 */
inline fun <T> singleResult(crossinline callback: (SingleResultCallback<T>) -> Unit): Single<T> {
    return Single.create<T> { emitter ->
        callback(SingleResultCallback { result: T?, throwable: Throwable? ->
            when {
                throwable != null -> emitter.onError(throwable)
                result != null -> emitter.onSuccess(result)
                else -> emitter.onError(NullPointerException("Null value passed"))
            }
        })
    }
}

/**
 * RX wrapper for SingleResultCallback<T>
 *
 * @param callback lambda that will be supplied to wrapped SingleResultCallback<T>
 */
inline fun completableResult(crossinline callback: (SingleResultCallback<Void>) -> Unit): Completable {
    return Completable.create { emitter ->
        callback(SingleResultCallback { result: Void?, throwable: Throwable? ->
            when {
                throwable != null -> emitter.onError(throwable)
                else -> emitter.onComplete()
            }
        })
    }
}

