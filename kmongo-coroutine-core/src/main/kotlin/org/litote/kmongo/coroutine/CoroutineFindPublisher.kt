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

import com.mongodb.CursorType
import com.mongodb.client.model.Collation
import com.mongodb.reactivestreams.client.FindPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.conversions.Bson
import java.util.concurrent.TimeUnit

/**
 * Gets coroutine version of [CoroutineFindPublisher].
 */
val <T: Any> FindPublisher<T>.coroutine: CoroutineFindPublisher<T>
    get() = CoroutineFindPublisher(
        this
    )

/**
 * Coroutine wrapper around [CoroutineFindPublisher].
 */
class CoroutineFindPublisher<T: Any>(override val publisher: FindPublisher<T>) :
    CoroutinePublisher<T>(publisher) {

    /**
     * Helper to return a publisher limited to the first result.
     *
     * @return a single item.
     */
    suspend fun first(): T? = publisher.first().awaitFirstOrNull()

    /**
     * Sets the query filter to apply to the query.
     *
     * @param filter the filter.
     * @return this
     * @mongodb.driver.manual reference/method/db.collection.find/ Filter
     */
    fun filter(filter: Bson): CoroutineFindPublisher<T> = publisher.filter(filter).coroutine

    /**
     * Sets the limit to apply.
     *
     * @param limit the limit
     * @return this
     * @mongodb.driver.manual reference/method/cursor.limit/#cursor.limit Limit
     */
    fun limit(limit: Int): CoroutineFindPublisher<T> = publisher.limit(limit).coroutine

    /**
     * Sets the number of documents to skip.
     *
     * @param skip the number of documents to skip
     * @return this
     * @mongodb.driver.manual reference/method/cursor.skip/#cursor.skip Skip
     */
    fun skip(skip: Int): CoroutineFindPublisher<T> = publisher.skip(skip).coroutine

    /**
     * Sets the maximum execution time on the server for this operation.
     *
     * @param maxTime  the max time
     * @param timeUnit the time unit
     * @return this
     * @mongodb.driver.manual reference/method/cursor.maxTimeMS/#cursor.maxTimeMS Max Time
     */
    fun maxTime(maxTime: Long, timeUnit: TimeUnit): CoroutineFindPublisher<T> =
        publisher.maxTime(maxTime, timeUnit).coroutine

    /**
     * The maximum amount of time for the server to wait on new documents to satisfy a tailable cursor
     * query. This only applies to a TAILABLE_AWAIT cursor. When the cursor is not a TAILABLE_AWAIT cursor,
     * this option is ignored.
     *
     * On servers &gt;= 3.2, this option will be specified on the getMore command as "maxTimeMS". The default
     * is no value: no "maxTimeMS" is sent to the server with the getMore command.
     *
     * On servers &lt; 3.2, this option is ignored, and indicates that the driver should respect the server's default value
     *
     * A zero value will be ignored.
     *
     * @param maxAwaitTime  the max await time
     * @param timeUnit the time unit to return the result in
     * @return the maximum await execution time in the given time unit
     * @mongodb.driver.manual reference/method/cursor.maxTimeMS/#cursor.maxTimeMS Max Time
     * @since 1.2
     */
    fun maxAwaitTime(maxAwaitTime: Long, timeUnit: TimeUnit): CoroutineFindPublisher<T> =
        publisher.maxAwaitTime(maxAwaitTime, timeUnit).coroutine

    /**
     * Sets a document describing the fields to return for all matching documents.
     *
     * @param projection the project document.
     * @return this
     * @mongodb.driver.manual reference/method/db.collection.find/ Projection
     */
    fun projection(projection: Bson): CoroutineFindPublisher<T> = publisher.projection(projection).coroutine

    /**
     * Sets the sort criteria to apply to the query.
     *
     * @param sort the sort criteria.
     * @return this
     * @mongodb.driver.manual reference/method/cursor.sort/ Sort
     */
    fun sort(sort: Bson): CoroutineFindPublisher<T> = publisher.sort(sort).coroutine

    /**
     * The server normally times out idle cursors after an inactivity period (10 minutes)
     * to prevent excess memory use. Set this option to prevent that.
     *
     * @param noCursorTimeout true if cursor timeout is disabled
     * @return this
     */
    fun noCursorTimeout(noCursorTimeout: Boolean): CoroutineFindPublisher<T> =
        publisher.noCursorTimeout(noCursorTimeout).coroutine

    /**
     * Users should not set this under normal circumstances.
     *
     * @param oplogReplay if oplog replay is enabled
     * @return this
     */
    fun oplogReplay(oplogReplay: Boolean): CoroutineFindPublisher<T> =
        publisher.oplogReplay(oplogReplay).coroutine

    /**
     * Get partial results from a sharded cluster if one or more shards are unreachable (instead of throwing an error).
     *
     * @param partial if partial results for sharded clusters is enabled
     * @return this
     */
    fun partial(partial: Boolean): CoroutineFindPublisher<T> =
        publisher.partial(partial).coroutine

    /**
     * Sets the cursor type.
     *
     * @param cursorType the cursor type
     * @return this
     */
    fun cursorType(cursorType: CursorType): CoroutineFindPublisher<T> =
        publisher.cursorType(cursorType).coroutine

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
    fun collation(collation: Collation): CoroutineFindPublisher<T> =
        publisher.collation(collation).coroutine

    /**
     * Sets the comment to the query. A null value means no comment is set.
     *
     * @param comment the comment
     * @return this
     * @since 1.6
     */
    fun comment(comment: String): CoroutineFindPublisher<T> =
        publisher.comment(comment).coroutine

    /**
     * Sets the hint for which index to use. A null value means no hint is set.
     *
     * @param hint the hint
     * @return this
     * @since 1.6
     */
    fun hint(hint: Bson): CoroutineFindPublisher<T> = publisher.hint(hint).coroutine

    /**
     * Sets the exclusive upper bound for a specific index. A null value means no max is set.
     *
     * @param max the max
     * @return this
     * @since 1.6
     */
    fun max(max: Bson): CoroutineFindPublisher<T> = publisher.max(max).coroutine

    /**
     * Sets the minimum inclusive lower bound for a specific index. A null value means no max is set.
     *
     * @param min the min
     * @return this
     * @since 1.6
     */
    fun min(min: Bson): CoroutineFindPublisher<T> = publisher.min(min).coroutine

    /**
     * Sets the returnKey. If true the find operation will return only the index keys in the resulting documents.
     *
     * @param returnKey the returnKey
     * @return this
     * @since 1.6
     */
    fun returnKey(returnKey: Boolean): CoroutineFindPublisher<T> = publisher.returnKey(returnKey).coroutine

    /**
     * Sets the showRecordId. Set to true to add a field `$recordId` to the returned documents.
     *
     * @param showRecordId the showRecordId
     * @return this
     * @since 1.6
     */
    fun showRecordId(showRecordId: Boolean): CoroutineFindPublisher<T> = publisher.showRecordId(showRecordId).coroutine

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
    fun batchSize(batchSize: Int): CoroutineFindPublisher<T> = publisher.batchSize(batchSize).coroutine

}
