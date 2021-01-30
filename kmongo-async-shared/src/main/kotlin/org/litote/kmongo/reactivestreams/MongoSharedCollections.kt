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

import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.reactivestreams.client.ChangeStreamPublisher
import com.mongodb.reactivestreams.client.MongoCollection
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

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
 * @param subscribeListener Triggered when watch is started (or reopened)
 * @param reopenListener Triggered when watch is reopened
 * @param reopenDelayInMS wait the specified time before trying to recovering
 * @param errorListener listen exceptions
 */
inline fun <reified T : Any> MongoCollection<T>.watchIndefinitely(
    fullDocument: FullDocument = FullDocument.DEFAULT,
    noinline subscribeListener: () -> Unit = {},
    noinline errorListener: (Throwable) -> Unit = {},
    noinline reopenListener: () -> Unit = {},
    reopenDelayInMS: Long = 5000,
    noinline listener: (ChangeStreamDocument<T>) -> Unit
) {
    watchIndefinitely(
        watchProvider = {
            watch(T::class.java).fullDocument(fullDocument)
        },
        subscribeListener = subscribeListener,
        errorListener = errorListener,
        reopenListener = reopenListener,
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
 * @param subscribeListener Triggered when watch is started (or reopened)
 * @param reopenListener Triggered when watch is reopened
 * @param reopenDelayInMS wait the specified time before trying to recovering
 * @param errorListener listen exceptions
 */
fun <T> MongoCollection<T>.watchIndefinitely(
    watchProvider: (MongoCollection<T>) -> ChangeStreamPublisher<T>,
    subscribeListener: () -> Unit = {},
    errorListener: (Throwable) -> Unit = {},
    reopenListener: () -> Unit = {},
    reopenDelayInMS: Long = 5000,
    listener: (ChangeStreamDocument<T>) -> Unit
) {
    watchProvider(this).subscribe(
        WatchSubscriber(
            this,
            watchProvider,
            subscribeListener,
            errorListener,
            reopenListener,
            reopenDelayInMS,
            listener
        )
    )
}

private class WatchSubscriber<T>(
    val col: MongoCollection<T>,
    val watchProvider: (MongoCollection<T>) -> ChangeStreamPublisher<T>,
    val subscribeListener: () -> Unit = {},
    val errorListener: (Throwable) -> Unit = {},
    val reopenListener: () -> Unit = {},
    val reopenDelayInMS: Long = 5000,
    val listener: (ChangeStreamDocument<T>) -> Unit
) : Subscriber<ChangeStreamDocument<T>> {

    private val complete = AtomicBoolean()

    override fun onComplete() {
        //run only one
        if (!complete.getAndSet(true)) {
            //wait to allow system to recover
            Executors.newSingleThreadScheduledExecutor().apply {
                schedule(
                    {
                        reopenListener()
                        col.watchIndefinitely(
                            watchProvider,
                            subscribeListener,
                            errorListener,
                            reopenListener,
                            reopenDelayInMS,
                            listener
                        )
                        awaitTermination(1, TimeUnit.MINUTES)
                    },
                    reopenDelayInMS,
                    TimeUnit.MILLISECONDS
                )
            }
        }
    }

    override fun onSubscribe(s: Subscription) {
        s.request(Long.MAX_VALUE)
        subscribeListener()
    }

    override fun onNext(t: ChangeStreamDocument<T>) {
        listener(t)
    }

    override fun onError(t: Throwable) {
        errorListener(t)
        onComplete()
    }

}