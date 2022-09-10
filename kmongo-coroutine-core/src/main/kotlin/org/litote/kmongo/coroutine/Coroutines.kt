/*
 * Copyright (C) 2016/2022 Litote
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
package org.litote.kmongo.coroutine

import com.mongodb.internal.async.SingleResultCallback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Coroutine wrapper for SingleResultCallback<T>
 *
 * @param callback lambda that will be supplied to wrapped SingleResultCallback<T>
 * @param <T>            the default target type of the collection to return
 */
suspend inline fun <T: Any> singleResult(crossinline callback: (SingleResultCallback<T>) -> Unit): T? {
    return suspendCoroutine { continuation ->
        callback(SingleResultCallback { result: T?, throwable: Throwable? ->
            if (throwable != null) {
                continuation.resumeWithException(throwable)
            } else {
                continuation.resume(result)
            }
        })
    }
}
