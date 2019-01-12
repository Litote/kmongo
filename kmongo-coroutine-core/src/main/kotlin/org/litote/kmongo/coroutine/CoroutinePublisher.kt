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

package org.litote.kmongo.coroutine

import kotlinx.coroutines.reactive.consumeEach
import org.reactivestreams.Publisher
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Gets coroutine version of [Publisher].
 */
val <T> Publisher<T>.coroutine: CoroutinePublisher<T> get() = CoroutinePublisher(this)

/**
 * Provides a list of not null elements from the publisher.
 */
suspend fun <T> Publisher<T>.toList(): List<T> {
    val r = ConcurrentLinkedQueue<T>()
    consumeEach { r.add(it) }
    return r.toList()
}


/**
 * Coroutine wrapper around [Publisher].
 */
open class CoroutinePublisher<T>(private val publisher: Publisher<T>) {

    /**
     * Provides a list of not null elements from the publisher.
     */
    suspend fun toList(): List<T> {
        val r = ConcurrentLinkedQueue<T>()
        publisher.consumeEach { r.add(it) }
        return r.toList()
    }
}