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

import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.CreateViewOptions
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import com.mongodb.reactivestreams.client.Success
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil

/**
 * Gets coroutine version of [MongoCollection].
 */
val MongoDatabase.coroutine: CoroutineDatabase get() = CoroutineDatabase(this)

/**
 * A wrapper around [MongoDatabase].
 * Provides coroutine methods for [Reactive Streams driver](https://mongodb.github.io/mongo-java-driver-reactivestreams/).
 */
class CoroutineDatabase(val database: MongoDatabase) {

    /**
     * Gets the name of the database.
     *
     * @return the database name
     */
    val name: String get() = database.name

    /**
     * Get the codec registry for the MongoDatabase.
     *
     * @return the [org.bson.codecs.configuration.CodecRegistry]
     */
    val codecRegistry: CodecRegistry get() = database.codecRegistry

    /**
     * Get the read preference for the MongoDatabase.
     *
     * @return the [com.mongodb.ReadPreference]
     */
    val readPreference: ReadPreference get() = database.readPreference

    /**
     * Get the write concern for the MongoDatabase.
     *
     * @return the [com.mongodb.WriteConcern]
     */
    val writeConcern: WriteConcern get() = database.writeConcern

    /**
     * Get the read concern for the MongoCollection.
     *
     * @return the [com.mongodb.ReadConcern]
     * @since 1.2
     * @mongodb.server.release 3.2
     */
    val readConcern: ReadConcern get() = database.readConcern

    /**
     * Create a new MongoDatabase instance with a different codec registry.
     *
     * @param codecRegistry the new [org.bson.codecs.configuration.CodecRegistry] for the collection
     * @return a new MongoDatabase instance with the different codec registry
     */
    fun withCodecRegistry(codecRegistry: CodecRegistry): CoroutineDatabase =
        database.withCodecRegistry(codecRegistry).coroutine

    /**
     * Create a new MongoDatabase instance with a different read preference.
     *
     * @param readPreference the new [com.mongodb.ReadPreference] for the collection
     * @return a new MongoDatabase instance with the different readPreference
     */
    fun withReadPreference(readPreference: ReadPreference): CoroutineDatabase =
        database.withReadPreference(readPreference).coroutine

    /**
     * Create a new MongoDatabase instance with a different write concern.
     *
     * @param writeConcern the new [com.mongodb.WriteConcern] for the collection
     * @return a new MongoDatabase instance with the different writeConcern
     */
    fun withWriteConcern(writeConcern: WriteConcern): CoroutineDatabase =
        database.withWriteConcern(writeConcern).coroutine

    /**
     * Create a new MongoDatabase instance with a different read concern.
     *
     * @param readConcern the new [ReadConcern] for the collection
     * @return a new MongoDatabase instance with the different ReadConcern
     * @since 1.2
     * @mongodb.server.release 3.2
     */
    fun withReadConcern(readConcern: ReadConcern): CoroutineDatabase =
        database.withReadConcern(readConcern).coroutine

    /**
     * Gets a collection, with a specific default document class.
     *
     * @param collectionName the name of the collection to return
     * @param clazz          the default class to cast any documents returned from the database into.
     * @param <TDocument>    the type of the class to use instead of `Document`.
     * @return the collection
     **/
    inline fun <reified TDocument : Any> getCollection(
        collectionName: String = KMongoUtil.defaultCollectionName(TDocument::class)
    ): CoroutineCollection<TDocument> =
        database.getCollection(collectionName, TDocument::class.java).coroutine

    /**
     * Executes command in the context of the current database.
     *
     * @param command        the command to be run
     * @param readPreference the [com.mongodb.ReadPreference] to be used when executing the command
     * @param <TResult>      the type of the class to use instead of `Document`.
     * @return a publisher containing the command result
     */
    suspend inline fun <reified TResult> runCommand(
        command: Bson,
        readPreference: ReadPreference = this.readPreference
    ): TResult? = database.runCommand(command, readPreference, TResult::class.java).awaitFirstOrNull()

    /**
     * Executes command in the context of the current database.
     *
     * @param clientSession the client session with which to associate this operation
     * @param command        the command to be run
     * @param readPreference the [com.mongodb.ReadPreference] to be used when executing the command
     * @param <TResult>      the type of the class to use instead of `Document`.
     * @return a publisher containing the command result
     */
    suspend inline fun <reified TResult> runCommand(
        clientSession: ClientSession,
        command: Bson,
        readPreference: ReadPreference = this.readPreference
    ): TResult? = database.runCommand(clientSession, command, readPreference, TResult::class.java).awaitFirstOrNull()

    /**
     * Drops this database.
     *
     * @mongodb.driver.manual reference/commands/dropDatabase/#dbcmd.dropDatabase Drop database
     */
    suspend fun drop(): Success = database.drop().awaitSingle()

    /**
     * Drops this database.
     *
     * @param clientSession the client session with which to associate this operation
     * @mongodb.driver.manual reference/commands/dropDatabase/#dbcmd.dropDatabase Drop database
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun drop(clientSession: ClientSession): Success = database.drop(clientSession).awaitSingle()

    /**
     * Gets the names of all the collections in this database.
     *
     * @return a list with all the names of all the collections in this database
     */
    suspend fun listCollectionNames(): List<String> = database.listCollectionNames().toList()

    /**
     * Gets the names of all the collections in this database.
     *
     * @param clientSession the client session with which to associate this operation
     * @return a publisher with all the names of all the collections in this database
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun listCollectionNames(clientSession: ClientSession): List<String> =
        database.listCollectionNames(clientSession).toList()

    /**
     * Finds all the collections in this database.
     *
     * @param clazz     the class to decode each document into
     * @param <TResult> the target document type of the iterable.
     * @return the fluent list collections interface
     * @mongodb.driver.manual reference/command/listCollections listCollections
     */
    inline fun <reified TResult> listCollections(): CoroutineListCollectionsPublisher<TResult> =
        database.listCollections(TResult::class.java).coroutine

    /**
     * Finds all the collections in this database.
     *
     * @param <TResult> the target document type of the iterable.
     * @return the fluent list collections interface
     * @mongodb.driver.manual reference/command/listCollections listCollections
     */
    inline fun <reified TResult> listCollections(clientSession: ClientSession): CoroutineListCollectionsPublisher<TResult> =
        database.listCollections(clientSession, TResult::class.java).coroutine

    /**
     * Create a new collection with the selected options
     *
     * @param collectionName the name for the new collection to create
     * @param options        various options for creating the collection
     * @mongodb.driver.manual reference/commands/create Create Command
     */
    suspend fun createCollection(collectionName: String, options: CreateCollectionOptions = CreateCollectionOptions())
            : Success = database.createCollection(collectionName, options).awaitSingle()

    /**
     * Create a new collection with the selected options
     *
     * @param clientSession the client session with which to associate this operation
     * @param collectionName the name for the new collection to create
     * @param options        various options for creating the collection
     * @mongodb.driver.manual reference/commands/create Create Command
     */
    suspend fun createCollection(
        clientSession: ClientSession,
        collectionName: String,
        options: CreateCollectionOptions = CreateCollectionOptions()
    ): Success = database.createCollection(clientSession, collectionName, options).awaitSingle()

    /**
     * Creates a view with the given name, backing collection/view name, aggregation pipeline, and options that defines the view.
     *
     * @param viewName the name of the view to create
     * @param viewOn   the backing collection/view for the view
     * @param pipeline the pipeline that defines the view
     * @param createViewOptions various options for creating the view
     * @return an observable identifying when the collection view has been created
     * @since 1.3
     * @mongodb.server.release 3.4
     * @mongodb.driver.manual reference/command/create Create Command
     */
    suspend fun createView(
        viewName: String,
        viewOn: String,
        pipeline: List<Bson>,
        createViewOptions: CreateViewOptions = CreateViewOptions()
    ): Success = database.createView(viewName, viewOn, pipeline, createViewOptions).awaitSingle()

    /**
     * Creates a view with the given name, backing collection/view name, aggregation pipeline, and options that defines the view.
     *
     * @param clientSession the client session with which to associate this operation
     * @param viewName the name of the view to create
     * @param viewOn   the backing collection/view for the view
     * @param pipeline the pipeline that defines the view
     * @param createViewOptions various options for creating the view
     * @since 1.3
     * @mongodb.server.release 3.4
     * @mongodb.driver.manual reference/command/create Create Command
     */
    suspend fun createView(
        clientSession: ClientSession,
        viewName: String,
        viewOn: String,
        pipeline: List<Bson>,
        createViewOptions: CreateViewOptions = CreateViewOptions()
    ): Success = database.createView(clientSession, viewName, viewOn, pipeline, createViewOptions).awaitSingle()

    /**
     * Creates a change stream for this database.
     *
     * @param pipeline    the aggregation pipeline to apply to the change stream
     * @param <TResult>   the target document type of the iterable.
     * @return the change stream iterable
     * @mongodb.driver.dochub core/changestreams Change Streams
     * @since 1.9
     * @mongodb.server.release 4.0
     */
    inline fun <reified TResult> watch(pipeline: List<Bson> = emptyList()): CoroutineChangeStreamPublisher<TResult> =
        database.watch(pipeline, TResult::class.java).coroutine

    /**
     * Creates a change stream for this database.
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
        database.watch(clientSession, pipeline, TResult::class.java).coroutine


    /** Extensions **/


    /**
     * Executes the given command in the context of the current database with the given read preference.
     *
     * @param command        the command to be run
     * @param newReadPreference the {@link com.mongodb.ReadPreference} to be used when executing the command
     * @param <TResult>      the type of the class to use instead of {@code Document}.
     */
    suspend inline fun <reified TResult : Any> runCommand(
        command: String,
        newReadPreference: ReadPreference = readPreference
    ): TResult? = runCommand(KMongoUtil.toBson(command), newReadPreference)

    /**
     * Drops this collection from the Database.
     *
     * @mongodb.driver.manual reference/command/drop/ Drop Collection
     */
    suspend inline fun <reified T : Any> dropCollection(): Success =
        dropCollection(KMongoUtil.defaultCollectionName(T::class))

    /**
     * Drops this collection from the Database.
     *
     * @mongodb.driver.manual reference/command/drop/ Drop Collection
     */
    suspend fun dropCollection(collectionName: String): Success =
        getCollection<Document>(collectionName).drop()

}