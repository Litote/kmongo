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

package org.litote.kmongo.reactivestreams

import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 *
 */
internal class SimpleSubscriber<T>(private val listener: (Throwable?) -> Unit = {}) : Subscriber<T> {

    override fun onComplete() {
        listener(null)
    }

    override fun onSubscribe(s: Subscription) {
        s.request(Long.MAX_VALUE)
    }

    override fun onNext(t: T) {
    }

    override fun onError(t: Throwable) {
        listener(t)
    }
}