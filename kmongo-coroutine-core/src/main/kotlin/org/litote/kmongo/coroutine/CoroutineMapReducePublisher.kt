/*
 * Copyright (C) 2016/2020 Litote
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
import com.mongodb.client.model.MapReduceAction
import com.mongodb.reactivestreams.client.MapReducePublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.conversions.Bson
import java.util.concurrent.TimeUnit

/**
 * Gets coroutine version of [MapReducePublisher].
 */
val <T> MapReducePublisher<T>.coroutine: CoroutineMapReducePublisher<T>
    get() = CoroutineMapReducePublisher(
        this
    )

/**
 * Coroutine wrapper around [MapReducePublisher].
 */
class CoroutineMapReducePublisher<T>(override val publisher: MapReducePublisher<T>) :
    CoroutinePublisher<T>(publisher) {

    /**
     * Sets the collectionName for the output of the MapReduce
     *
     *
     * The default action is replace the collection if it exists, to change this use [.action].
     *
     * @param collectionName the name of the collection that you want the map-reduce operation to write its output.
     * @return this
     */
    fun collectionName(collectionName: String): CoroutineMapReducePublisher<T> =
        publisher.collectionName(collectionName).coroutine

    /**
     * Sets the JavaScript function that follows the reduce method and modifies the output.
     *
     * @param finalizeFunction the JavaScript function that follows the reduce method and modifies the output.
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce/#mapreduce-finalize-cmd Requirements for the finalize Function
     */
    fun finalizeFunction(finalizeFunction: String): CoroutineMapReducePublisher<T> =
        publisher.finalizeFunction(finalizeFunction).coroutine

    /**
     * Sets the global variables that are accessible in the map, reduce and finalize functions.
     *
     * @param scope the global variables that are accessible in the map, reduce and finalize functions.
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce mapReduce
     */
    fun scope(scope: Bson): CoroutineMapReducePublisher<T> = publisher.scope(scope).coroutine

    /**
     * Sets the sort criteria to apply to the query.
     *
     * @param sort the sort criteria, which may be null.
     * @return this
     * @mongodb.driver.manual reference/method/cursor.sort/ Sort
     */
    fun sort(sort: Bson): CoroutineMapReducePublisher<T> = publisher.sort(sort).coroutine

    /**
     * Sets the query filter to apply to the query.
     *
     * @param filter the filter to apply to the query.
     * @return this
     * @mongodb.driver.manual reference/method/db.collection.find/ Filter
     */
    fun filter(filter: Bson): CoroutineMapReducePublisher<T> = publisher.filter(filter).coroutine

    /**
     * Sets the limit to apply.
     *
     * @param limit the limit, which may be null
     * @return this
     * @mongodb.driver.manual reference/method/cursor.limit/#cursor.limit Limit
     */
    fun limit(limit: Int): CoroutineMapReducePublisher<T> = publisher.limit(limit).coroutine

    /**
     * Sets the flag that specifies whether to convert intermediate data into BSON format between the execution of the map and reduce
     * functions. Defaults to false.
     *
     * @param jsMode the flag that specifies whether to convert intermediate data into BSON format between the execution of the map and
     * reduce functions
     * @return jsMode
     * @mongodb.driver.manual reference/command/mapReduce mapReduce
     */
    fun jsMode(jsMode: Boolean): CoroutineMapReducePublisher<T> = publisher.jsMode(jsMode).coroutine

    /**
     * Sets whether to include the timing information in the result information.
     *
     * @param verbose whether to include the timing information in the result information.
     * @return this
     */
    fun verbose(verbose: Boolean): CoroutineMapReducePublisher<T> = publisher.verbose(verbose).coroutine

    /**
     * Sets the maximum execution time on the server for this operation.
     *
     * @param maxTime  the max time
     * @param timeUnit the time unit, which may not be null
     * @return this
     * @mongodb.driver.manual reference/method/cursor.maxTimeMS/#cursor.maxTimeMS Max Time
     */
    fun maxTime(maxTime: Long, timeUnit: TimeUnit): CoroutineMapReducePublisher<T> =
        publisher.maxTime(maxTime, timeUnit).coroutine

    /**
     * Specify the `MapReduceAction` to be used when writing to a collection.
     *
     * @param action an [com.mongodb.client.model.MapReduceAction] to perform on the collection
     * @return this
     */
    fun action(action: MapReduceAction): CoroutineMapReducePublisher<T> =
        publisher.action(action).coroutine

    /**
     * Sets the name of the database to output into.
     *
     * @param databaseName the name of the database to output into.
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce/#output-to-a-collection-with-an-action output with an action
     */
    fun databaseName(databaseName: String): CoroutineMapReducePublisher<T> =
        publisher.databaseName(databaseName).coroutine

    /**
     * Sets if the output database is sharded
     *
     * @param sharded if the output database is sharded
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce/#output-to-a-collection-with-an-action output with an action
     */
    fun sharded(sharded: Boolean): CoroutineMapReducePublisher<T> =
        publisher.sharded(sharded).coroutine

    /**
     * Sets if the post-processing step will prevent MongoDB from locking the database.
     *
     * Valid only with the `MapReduceAction.MERGE` or `MapReduceAction.REDUCE` actions.
     *
     * @param nonAtomic if the post-processing step will prevent MongoDB from locking the database.
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce/#output-to-a-collection-with-an-action output with an action
     */
    fun nonAtomic(nonAtomic: Boolean): CoroutineMapReducePublisher<T> =
        publisher.nonAtomic(nonAtomic).coroutine

    /**
     * Sets the bypass document level validation flag.
     *
     *
     * Note: This only applies when an $out stage is specified.
     *
     * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
     * @return this
     * @since 1.2
     * @mongodb.driver.manual reference/command/aggregate/ Aggregation
     * @mongodb.server.release 3.2
     */
    fun bypassDocumentValidation(bypassDocumentValidation: Boolean?): CoroutineMapReducePublisher<T> =
        publisher.bypassDocumentValidation(bypassDocumentValidation).coroutine

    /**
     * Aggregates documents to a collection according to the specified map-reduce function with the given options, which must specify a
     * non-inline result.
     *
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual aggregation/ Aggregation
     */
    suspend fun toCollection() = publisher.toCollection().awaitFirstOrNull()

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
    fun collation(collation: Collation): CoroutineMapReducePublisher<T> =
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
    fun batchSize(batchSize: Int): CoroutineMapReducePublisher<T> = publisher.batchSize(batchSize).coroutine

    /**
     * Helper to return a publisher limited to the first result.
     *
     * @return a single item.
     * @since 1.8
     */
    suspend fun first(): T? = publisher.first().awaitFirstOrNull()
}