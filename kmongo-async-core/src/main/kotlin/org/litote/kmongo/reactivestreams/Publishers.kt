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

package org.litote.kmongo.reactivestreams

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.ConcurrentLinkedQueue


/**
 * Listen one element from the publisher.
 */
fun <T> Publisher<T>.listenSingle(listener: (T?, Throwable?) -> Unit): Unit =
    subscribe(object : Subscriber<T> {
        override fun onComplete() {
            //do nothing
        }

        override fun onSubscribe(s: Subscription) {
            s.request(1)
        }

        override fun onNext(t: T?) {
            listener(t, null)
        }

        override fun onError(t: Throwable) {
            listener(null, t)
        }
    })

/**
 * Listen each element from the publisher.
 */
fun <T> Publisher<T>.forEach(listener: (T?, Throwable?) -> Unit): Unit =
    subscribe(object : Subscriber<T> {
        override fun onComplete() {
            //do nothing
        }

        override fun onSubscribe(s: Subscription) {
            s.request(Long.MAX_VALUE)
        }

        override fun onNext(t: T?) {
            listener(t, null)
        }

        override fun onError(t: Throwable) {
            listener(null, t)
        }
    })

/**
 * Listen list of not null elements from the publisher.
 */
fun <T> Publisher<T>.listenList(listener: (List<T>?, Throwable?) -> Unit) {
    val r = ConcurrentLinkedQueue<T>()
    subscribe(object : Subscriber<T> {
        override fun onComplete() {
            listener(r.toList(), null)
        }

        override fun onSubscribe(s: Subscription) {
            s.request(Long.MAX_VALUE)
        }

        override fun onNext(t: T?) {
            if (t != null) {
                r.add(t)
            }
        }

        override fun onError(t: Throwable) {
            listener(null, t)
        }
    })
}

