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

import com.mongodb.ClientSessionOptions
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoClient
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.conversions.Bson
import java.io.Closeable

/**
 * Gets coroutine version of [MongoClient].
 */
val MongoClient.coroutine: CoroutineClient get() = CoroutineClient(this)

/**
 * A wrapper around [MongoClient].
 * Provides coroutine methods for [Reactive Streams driver](https://mongodb.github.io/mongo-java-driver-reactivestreams/).
 */
class CoroutineClient(val client: MongoClient) : Closeable by client {

    /**
     * Gets the database with the given name.
     *
     * @param name the name of the database
     * @return the database
     */
    fun getDatabase(name: String): CoroutineDatabase = client.getDatabase(name).coroutine

    /**
     * Get a list of the database names
     *
     * @mongodb.driver.manual reference/commands/listDatabases List Databases
     * @return an iterable containing all the names of all the databases
     */
    suspend fun listDatabaseNames(): List<String> = client.listDatabaseNames().toList()

    /**
     * Get a list of the database names
     *
     * @param clientSession the client session with which to associate this operation
     * @mongodb.driver.manual reference/commands/listDatabases List Databases
     * @return an iterable containing all the names of all the databases
     *
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun listDatabaseNames(clientSession: ClientSession): List<String> =
        client.listDatabaseNames(clientSession).toList()

    /**
     * Gets the list of databases
     *
     * @param <TResult>   the type of the class to use - use `Document` if you don't know what to use.
     * @return the fluent list databases interface
     */
    suspend inline fun <reified TResult> listDatabases(): List<TResult> =
        client.listDatabases(TResult::class.java).toList()

    /**
     * Gets the list of databases
     *
     * @param clientSession the client session with which to associate this operation
     * @param <TResult>   the type of the class to use instead of `Document`.
     * @return the fluent list databases interface
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend inline fun <reified TResult> listDatabases(clientSession: ClientSession): List<TResult> =
        client.listDatabases(clientSession, TResult::class.java).toList()

    /**
     * Creates a change stream for this client.
     *
     * @param pipeline    the aggregation pipeline to apply to the change stream
     * @param <TResult>   the target document type of the iterable.
     * @return the change stream iterable
     * @mongodb.driver.dochub core/changestreams Change Streams
     * @since 1.9
     * @mongodb.server.release 4.0
     */
    inline fun <reified TResult> watch(pipeline: List<Bson> = emptyList()): CoroutineChangeStreamPublisher<TResult> =
        client.watch(pipeline, TResult::class.java).coroutine

    /**
     * Creates a change stream for this client.
     *
     * @param clientSession the client session with which to associate this operation
     * @param pipeline    the aggregation pipeline to apply to the change stream
     * @param <TResult>   the target document type of the iterable.
     * @return the change stream iterable
     * @since 1.9
     * @mongodb.server.release 4.0
     * @mongodb.driver.dochub core/changestreams Change Streams
     */
    inline fun <reified TResult> watch(
        clientSession: ClientSession,
        pipeline: List<Bson> = emptyList()
    ): CoroutineChangeStreamPublisher<TResult> =
        client.watch(clientSession, pipeline, TResult::class.java).coroutine

    /**
     * Creates a client session.
     *
     * @return the client session.
     * @mongodb.server.release 3.6
     * @since 1.9
     */
    suspend fun startSession(): ClientSession = client.startSession().awaitSingle()

    /**
     * Creates a client session.
     *
     * @param options the options for the client session
     * @return the client session.
     * @mongodb.server.release 3.6
     * @since 1.9
     */
    suspend fun startSession(options: ClientSessionOptions): ClientSession = client.startSession(options).awaitSingle()

}