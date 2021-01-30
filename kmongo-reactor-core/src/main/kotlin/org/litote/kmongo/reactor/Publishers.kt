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

package org.litote.kmongo.reactor

import org.reactivestreams.Publisher
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

/**
 * Subscribes to and awaits the termination of this [Publisher] instance in a blocking manner and rethrows any exception emitted.
 *
 * @throws RuntimeException wrapping an InterruptedException if the current thread is interrupted
 */
fun Publisher<*>.blockLast(): Any? = toFlux().blockLast()

/**
 * Waits in a blocking fashion until the current Single signals a success value, null (which is returned) or an exception (which is propagated).
 * @return the success value or null
 */
fun <T> Publisher<T>.block(): T? = toMono().block()
