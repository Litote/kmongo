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

package org.litote.kmongo.coroutine

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.lang.RuntimeException

class DoNothingPublisher<T> : Publisher<T> {

    override fun subscribe(s: Subscriber<in T>) {
        s.onComplete()
    }
}

class DoSinglePublisher<T>(val t: T) : Publisher<T> {

    override fun subscribe(s: Subscriber<in T>) {
        s.onSubscribe(object:Subscription {
            override fun cancel() {
            }

            override fun request(n: Long) {
            }
        })
        s.onNext(t)
        s.onComplete()
    }

}

class ErrorPublisher<T>(val t: Throwable = RuntimeException()) : Publisher<T> {

    override fun subscribe(s: Subscriber<in T>) {
        s.onError(t)
    }

}

