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

import com.mongodb.client.model.Collation
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.reactivestreams.client.ChangeStreamPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.BsonDocument
import org.bson.BsonTimestamp
import java.util.concurrent.TimeUnit

/**
 * Gets coroutine version of [ChangeStreamPublisher].
 */
val <T> ChangeStreamPublisher<T>.coroutine: CoroutineChangeStreamPublisher<T>
    get() = CoroutineChangeStreamPublisher(
        this
    )

/**
 * Coroutine wrapper around [ChangeStreamPublisher].
 */
class CoroutineChangeStreamPublisher<TResult>(val publisher: ChangeStreamPublisher<TResult>) :
    CoroutinePublisher<ChangeStreamDocument<TResult>>(publisher) {

    /**
     * Sets the fullDocument value.
     *
     * @param fullDocument the fullDocument
     * @return this
     */
    fun fullDocument(fullDocument: FullDocument): CoroutineChangeStreamPublisher<TResult> =
        publisher.fullDocument(fullDocument).coroutine

    /**
     * Sets the logical starting point for the new change stream.
     *
     * @param resumeToken the resume token
     * @return this
     */
    fun resumeAfter(resumeToken: BsonDocument): CoroutineChangeStreamPublisher<TResult> =
        publisher.resumeAfter(resumeToken).coroutine

    /**
     * The change stream will only provide changes that occurred after the specified timestamp.
     *
     *
     * Any command run against the server will return an operation time that can be used here.
     *
     * The default value is an operation time obtained from the server before the change stream was created.
     *
     * @param startAtOperationTime the start at operation time
     * @since 1.9
     * @return this
     * @mongodb.server.release 4.0
     * @mongodb.driver.manual reference/method/db.runCommand/
     */
    fun startAtOperationTime(startAtOperationTime: BsonTimestamp): CoroutineChangeStreamPublisher<TResult> =
        publisher.startAtOperationTime(startAtOperationTime).coroutine

    /**
     * Sets the maximum await execution time on the server for this operation.
     *
     * @param maxAwaitTime  the max await time.  A zero value will be ignored, and indicates that the driver should respect the server's
     * default value
     * @param timeUnit the time unit, which may not be null
     * @return this
     */
    fun maxAwaitTime(maxAwaitTime: Long, timeUnit: TimeUnit): CoroutineChangeStreamPublisher<TResult> =
        publisher.maxAwaitTime(maxAwaitTime, timeUnit).coroutine

    /**
     * Sets the collation options
     *
     *
     * A null value represents the server default.
     * @param collation the collation options to use
     * @return this
     */
    fun collation(collation: Collation): CoroutineChangeStreamPublisher<TResult> =
        publisher.collation(collation).coroutine

    /**
     * Returns a list containing the results of the change stream based on the document class provided.
     *
     * @param <TDocument> the result type
     * @return the new Mongo Iterable
     */
    suspend inline fun <reified TDocument> withDocumentClass(): List<TDocument> =
        publisher.withDocumentClass(TDocument::class.java).toList()

    /**
     * Sets the number of documents to return per batch.
     *
     *
     * Overrides the [org.reactivestreams.Subscription.request] value for setting the batch size, allowing for fine grained
     * control over the underlying cursor.
     *
     * @param batchSize the batch size
     * @return this
     * @since 1.8
     * @mongodb.driver.manual reference/method/cursor.batchSize/#cursor.batchSize Batch Size
     */
    fun batchSize(batchSize: Int): CoroutineChangeStreamPublisher<TResult> =
        publisher.batchSize(batchSize).coroutine

    /**
     * Helper to return a publisher limited to the first result.
     *
     * @return a single item.
     * @since 1.8
     */
    suspend fun first(): ChangeStreamDocument<TResult>? = publisher.first().awaitFirstOrNull()
}