/*
 * Copyright (C) 2016/2020 Litote
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

package org.litote.kmongo.reactor

import com.mongodb.internal.async.SingleResultCallback
import reactor.core.publisher.Mono

/**
 * Reactor wrapper for SingleResultCallback<T>
 *
 * @param callback lambda that will be supplied to wrapped SingleResultCallback<T>
 * @param <T>            the default target type of the collection to return
 */
inline fun <T> monoResult(crossinline callback: (SingleResultCallback<T>) -> Unit): Mono<T> {
    return Mono.create { sink ->
        callback { result: T?, throwable: Throwable? ->
            when {
                throwable != null -> sink.error(throwable)
                result != null -> sink.success(result)
                else -> sink.success()
            }
        }
    }
}

/**
 * Reactor wrapper for SingleResultCallback<Void>
 *
 * @param callback lambda that will be supplied to wrapped SingleResultCallback<Void>
 */
inline fun emptyResult(crossinline callback: (SingleResultCallback<Void>) -> Unit): Mono<Void> {
    return Mono.create { sink ->
        callback { _: Void?, throwable: Throwable? ->
            when {
                throwable != null -> sink.error(throwable)
                else -> sink.success()
            }
        }
    }
}

