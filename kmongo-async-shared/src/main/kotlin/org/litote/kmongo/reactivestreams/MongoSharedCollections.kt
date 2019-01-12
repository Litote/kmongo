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

package org.litote.kmongo.reactivestreams

import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.reactivestreams.client.ChangeStreamPublisher
import com.mongodb.reactivestreams.client.MongoCollection
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

//*******
//MongoCollection extension methods
//*******

/**
 * Returns a [MongoCollection] with a KMongo codec.
 */
fun <T> MongoCollection<T>.withKMongo(): MongoCollection<T> =
    withCodecRegistry(KMongo.configureRegistry(codecRegistry))

/**
 * Create a new MongoCollection instance with a different default class to cast any documents returned from the database into..
 *
 * @param <NewTDocument> the default class to cast any documents returned from the database into.
 * @return a new MongoCollection instance with the different default class
 */
inline fun <reified NewTDocument : Any> MongoCollection<*>.withDocumentClass(): MongoCollection<NewTDocument> =
    withDocumentClass(NewTDocument::class.java)


/**
 * Watches collection changes.
 * Open a new stream if an invalidate event or drop database event occurs.
 *
 * @param listener the main listener
 * @param fullDocument [FullDocument] option
 * @param subscribeListener Triggered when watch is started (or restarted)
 * @param reopenDelayInMS wait the specified time before trying to recovering
 * @param errorListener listen exceptions
 */
inline fun <reified T : Any> MongoCollection<T>.watchIndefinitely(
    fullDocument: FullDocument = FullDocument.DEFAULT,
    noinline subscribeListener: () -> Unit = {},
    noinline errorListener: (Throwable) -> Unit = {},
    reopenDelayInMS: Long = 5,
    noinline listener: (ChangeStreamDocument<T>) -> Unit
) {
    watchIndefinitely(
        watchProvider = {
            watch(T::class.java).fullDocument(fullDocument)
        },
        subscribeListener = subscribeListener,
        errorListener = errorListener,
        reopenDelayInMS = reopenDelayInMS,
        listener = listener
    )
}

/**
 * Watches collection changes.
 * Open a new stream if an invalidate event occurs.
 * The basic idea is to survive automatically to replicaset changes
 *
 * @param listener the main listener
 * @param watchProvider provides [ChangeStreamPublisher]
 * @param subscribeListener Triggered when watch is started (or restarted)
 * @param reopenDelayInMS wait the specified time before trying to recovering
 * @param errorListener listen exceptions
 */
fun <T> MongoCollection<T>.watchIndefinitely(
    watchProvider: (MongoCollection<T>) -> ChangeStreamPublisher<T>,
    subscribeListener: () -> Unit = {},
    errorListener: (Throwable) -> Unit = {},
    reopenDelayInMS: Long = 5,
    listener: (ChangeStreamDocument<T>) -> Unit
) {
    watchProvider(this).subscribe(
        object : Subscriber<ChangeStreamDocument<T>> {
            override fun onComplete() {
                //wait to allow system to recover
                Executors.newSingleThreadScheduledExecutor().apply {
                    schedule(
                        {
                            watchIndefinitely(
                                watchProvider,
                                subscribeListener,
                                errorListener,
                                reopenDelayInMS,
                                listener
                            )
                            awaitTermination(1, TimeUnit.MINUTES)
                        },
                        reopenDelayInMS,
                        TimeUnit.SECONDS
                    )
                }
            }

            override fun onSubscribe(s: Subscription) {
                s.request(Long.MAX_VALUE)
                subscribeListener.invoke()
            }

            override fun onNext(t: ChangeStreamDocument<T>) {
                listener(t)
            }

            override fun onError(t: Throwable) {
                errorListener(t)
                onComplete()
            }
        })
}