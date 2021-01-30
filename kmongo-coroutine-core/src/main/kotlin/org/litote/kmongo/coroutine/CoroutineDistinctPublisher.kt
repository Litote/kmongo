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

import com.mongodb.client.model.Collation
import com.mongodb.reactivestreams.client.DistinctPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.conversions.Bson
import java.util.concurrent.TimeUnit

/**
 * Gets coroutine version of [CoroutineDistinctPublisher].
 */
val <T: Any> DistinctPublisher<T>.coroutine: CoroutineDistinctPublisher<T>
    get() = CoroutineDistinctPublisher(
        this
    )

/**
 * Coroutine wrapper around [CoroutineDistinctPublisher].
 */
class CoroutineDistinctPublisher<T: Any>(override val publisher: DistinctPublisher<T>) :
    CoroutinePublisher<T>(publisher) {

    /**
     * Sets the query filter to apply to the query.
     *
     * @param filter the filter
     * @return this
     * @mongodb.driver.manual reference/method/db.collection.find/ Filter
     */
    fun filter(filter: Bson): CoroutineDistinctPublisher<T> = publisher.filter(filter).coroutine

    /**
     * Sets the maximum execution time on the server for this operation.
     *
     * @param maxTime  the max time
     * @param timeUnit the time unit, which may not be null
     * @return this
     */
    fun maxTime(maxTime: Long, timeUnit: TimeUnit): CoroutineDistinctPublisher<T> =
        publisher.maxTime(maxTime, timeUnit).coroutine

    /**
     * Sets the collation options
     *
     *
     * A null value represents the server default.
     * @param collation the collation options to use
     * @return this
     * @since 1.3
     * @mongodb.server.release 3.4
     */
    fun collation(collation: Collation): CoroutineDistinctPublisher<T> =
        publisher.collation(collation).coroutine

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
    fun batchSize(batchSize: Int): CoroutineDistinctPublisher<T> =
        publisher.batchSize(batchSize).coroutine

    /**
     * Helper to return a publisher limited to the first result.
     *
     * @return a single item.
     * @since 1.8
     */
    suspend fun first(): T? = publisher.first().awaitFirstOrNull()

}
