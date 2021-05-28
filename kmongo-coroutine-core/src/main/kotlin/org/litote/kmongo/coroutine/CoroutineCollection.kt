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

import com.mongodb.MongoCommandException
import com.mongodb.MongoNamespace
import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.BulkWriteOptions
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.CreateIndexOptions
import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.DropIndexOptions
import com.mongodb.client.model.EstimatedDocumentCountOptions
import com.mongodb.client.model.FindOneAndDeleteOptions
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.RenameCollectionOptions
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.WriteModel
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertManyResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.SetTo
import org.litote.kmongo.and
import org.litote.kmongo.ascending
import org.litote.kmongo.excludeId
import org.litote.kmongo.fields
import org.litote.kmongo.include
import org.litote.kmongo.path
import org.litote.kmongo.reactivestreams.map
import org.litote.kmongo.set
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.EMPTY_JSON
import org.litote.kmongo.util.KMongoUtil.extractId
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import org.litote.kmongo.util.KMongoUtil.setModifier
import org.litote.kmongo.util.KMongoUtil.toBson
import org.litote.kmongo.util.KMongoUtil.toBsonModifier
import org.litote.kmongo.util.PairProjection
import org.litote.kmongo.util.SingleProjection
import org.litote.kmongo.util.TripleProjection
import org.litote.kmongo.util.UpdateConfiguration
import org.litote.kmongo.util.pairProjectionCodecRegistry
import org.litote.kmongo.util.singleProjectionCodecRegistry
import org.litote.kmongo.util.tripleProjectionCodecRegistry
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Gets coroutine version of [MongoCollection].
 */
val <T : Any> MongoCollection<T>.coroutine: CoroutineCollection<T> get() = CoroutineCollection(this)

/**
 * A wrapper around [MongoCollection].
 * Provides coroutine methods for [Reactive Streams driver](https://mongodb.github.io/mongo-java-driver-reactivestreams/).
 */
class CoroutineCollection<T : Any>(val collection: MongoCollection<T>) {

    /**
     * Gets the namespace of this collection.
     *
     * @return the namespace
     */
    val namespace: MongoNamespace get() = collection.namespace


    /**
     * Get the class of documents stored in this collection.
     *
     * @return the class
     */
    val documentClass: Class<T> get() = collection.documentClass

    /**
     * Get the codec registry for the MongoCollection.
     *
     * @return the [org.bson.codecs.configuration.CodecRegistry]
     */
    val codecRegistry: CodecRegistry get() = collection.codecRegistry

    /**
     * Get the read preference for the MongoCollection.
     *
     * @return the [com.mongodb.ReadPreference]
     */
    val readPreference: ReadPreference get() = collection.readPreference

    /**
     * Get the write concern for the MongoCollection.
     *
     * @return the [com.mongodb.WriteConcern]
     */
    val writeConcern: WriteConcern get() = collection.writeConcern

    /**
     * Get the read concern for the MongoCollection.
     *
     * @return the [com.mongodb.ReadConcern]
     * @mongodb.server.release 3.2
     * @since 1.2
     */
    val readConcern: ReadConcern get() = collection.readConcern

    /**
     * Create a new MongoCollection instance with a different default class to cast any documents returned from the database into..
     *
     * @param <NewT> The type that the new collection will encode documents from and decode documents to
     * @return a new MongoCollection instance with the different default class
     */
    inline fun <reified NewT : Any> withDocumentClass(): CoroutineCollection<NewT> =
        collection.withDocumentClass(NewT::class.java).coroutine

    /**
     * Create a new MongoCollection instance with a different codec registry.
     *
     * @param codecRegistry the new [org.bson.codecs.configuration.CodecRegistry] for the collection
     * @return a new MongoCollection instance with the different codec registry
     */
    fun withCodecRegistry(codecRegistry: CodecRegistry): CoroutineCollection<T> =
        collection.withCodecRegistry(codecRegistry).coroutine

    /**
     * Create a new MongoCollection instance with a different read preference.
     *
     * @param readPreference the new [com.mongodb.ReadPreference] for the collection
     * @return a new MongoCollection instance with the different readPreference
     */
    fun withReadPreference(readPreference: ReadPreference): CoroutineCollection<T> =
        collection.withReadPreference(readPreference).coroutine

    /**
     * Create a new MongoCollection instance with a different write concern.
     *
     * @param writeConcern the new [com.mongodb.WriteConcern] for the collection
     * @return a new MongoCollection instance with the different writeConcern
     */
    fun withWriteConcern(writeConcern: WriteConcern): CoroutineCollection<T> =
        collection.withWriteConcern(writeConcern).coroutine

    /**
     * Create a new MongoCollection instance with a different read concern.
     *
     * @param readConcern the new [ReadConcern] for the collection
     * @return a new MongoCollection instance with the different ReadConcern
     * @mongodb.server.release 3.2
     * @since 1.2
     */
    fun withReadConcern(readConcern: ReadConcern): CoroutineCollection<T> =
        collection.withReadConcern(readConcern).coroutine

    /**
     * Gets an estimate of the count of documents in a collection using collection metadata.
     *
     * @param options the options describing the count
     * @since 1.9
     */
    suspend fun estimatedDocumentCount(options: EstimatedDocumentCountOptions = EstimatedDocumentCountOptions()): Long =
        collection.estimatedDocumentCount(options).awaitSingle()

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     *
     *
     * Note: When migrating from `count()` to `countDocuments()` the following query operators must be replaced:
     *
     * <pre>
     *
     * +-------------+--------------------------------+
     * | Operator    | Replacement                    |
     * +=============+================================+
     * | $where      |  $expr                         |
     * +-------------+--------------------------------+
     * | $near       |  $geoWithin with $center       |
     * +-------------+--------------------------------+
     * | $nearSphere |  $geoWithin with $centerSphere |
     * +-------------+--------------------------------+
    </pre> *
     *
     * @param filter  the query filter
     * @param options the options describing the count
     * @since 1.9
     */
    suspend fun countDocuments(filter: Bson = EMPTY_BSON, options: CountOptions = CountOptions()): Long =
        collection.countDocuments(filter, options).awaitSingle()

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     *
     *
     * Note: When migrating from `count()` to `countDocuments()` the following query operators must be replaced:
     *
     * <pre>
     *
     * +-------------+--------------------------------+
     * | Operator    | Replacement                    |
     * +=============+================================+
     * | $where      |  $expr                         |
     * +-------------+--------------------------------+
     * | $near       |  $geoWithin with $center       |
     * +-------------+--------------------------------+
     * | $nearSphere |  $geoWithin with $centerSphere |
     * +-------------+--------------------------------+
    </pre> *
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter  the query filter
     * @param options the options describing the count
     * @mongodb.server.release 3.6
     * @since 1.9
     */
    suspend fun countDocuments(
        clientSession: ClientSession,
        filter: Bson = EMPTY_BSON,
        options: CountOptions = CountOptions()
    ): Long =
        collection.countDocuments(clientSession, filter, options).awaitSingle()

    /**
     * Gets the distinct values of the specified field name.
     *
     * @param fieldName   the field name
     * @param filter      the query filter
     * @param <T>   the target type of the iterable.
     * @mongodb.driver.manual reference/command/distinct/ Distinct
     */
    inline fun <reified T : Any> distinct(
        fieldName: String,
        filter: Bson = EMPTY_BSON
    ): CoroutineDistinctPublisher<T> =
        collection.distinct(fieldName, filter, T::class.java).coroutine


    /**
     * Gets the distinct values of the specified field name.
     *
     * @param clientSession the client session with which to associate this operation
     * @param fieldName   the field name
     * @param filter      the query filter
     * @param <T>   the target type of the iterable.
     * @mongodb.driver.manual reference/command/distinct/ Distinct
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    inline fun <reified T : Any> distinct(
        clientSession: ClientSession,
        fieldName: String,
        filter: Bson
    ): CoroutineDistinctPublisher<T> =
        collection.distinct(clientSession, fieldName, filter, T::class.java).coroutine


    /**
     * Finds all documents in the collection.
     *
     * @param filter the query filter
     * @mongodb.driver.manual tutorial/query-documents/ Find
     */
    fun find(filter: Bson = EMPTY_BSON): CoroutineFindPublisher<T> = collection.find(filter).coroutine

    /**
     * Finds all documents in the collection.
     *
     * @param filter    the query filter
     * @param <T> the target document type of the iterable.
     * @mongodb.driver.manual tutorial/query-documents/ Find
     */
    inline fun <reified T : Any> findAndCast(filter: Bson = EMPTY_BSON): CoroutineFindPublisher<T> =
        collection.find(filter, T::class.java).coroutine

    /**
     * Finds all documents in the collection.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter the query filter
     * @mongodb.driver.manual tutorial/query-documents/ Find
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    fun find(clientSession: ClientSession, filter: Bson = EMPTY_BSON): CoroutineFindPublisher<T> =
        collection.find(clientSession, filter).coroutine

    /**
     * Finds all documents in the collection.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter    the query filter
     * @param <T> the target document type of the iterable.
     * @mongodb.driver.manual tutorial/query-documents/ Find
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    inline fun <reified T : Any> findAndCast(
        clientSession: ClientSession,
        filter: Bson
    ): CoroutineFindPublisher<T> = collection.find(clientSession, filter, T::class.java).coroutine

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param pipeline  the aggregate pipeline
     * @param <T> the target document type of the iterable.
     * @return a publisher containing the result of the aggregation operation
     * @mongodb.driver.manual aggregation/ Aggregation
     */
    inline fun <reified T : Any> aggregate(pipeline: List<Bson>): CoroutineAggregatePublisher<T> =
        collection.aggregate(pipeline, T::class.java).coroutine

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param clientSession the client session with which to associate this operation
     * @param pipeline  the aggregate pipeline
     * @param <T> the target document type of the iterable.
     * @return a publisher containing the result of the aggregation operation
     * @mongodb.driver.manual aggregation/ Aggregation
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    inline fun <reified T : Any> aggregate(
        clientSession: ClientSession,
        pipeline: List<Bson>
    ): CoroutineAggregatePublisher<T> =
        collection.aggregate(clientSession, pipeline, T::class.java).coroutine

    /**
     * Creates a change stream for this collection.
     *
     * @param pipeline    the aggregation pipeline to apply to the change stream
     * @param <T>   the target document type of the iterable.
     * @return the change stream iterable
     * @mongodb.driver.manual reference/operator/aggregation/changeStream $changeStream
     * @since 1.6
     */
    inline fun <reified T : Any> watch(pipeline: List<Bson> = emptyList()): CoroutineChangeStreamPublisher<T> =
        collection.watch(pipeline, T::class.java).coroutine

    /**
     * Creates a change stream for this collection.
     *
     * @param clientSession the client session with which to associate this operation
     * @param pipeline    the aggregation pipeline to apply to the change stream
     * @param <T>   the target document type of the iterable.
     * @return the change stream iterable
     * @mongodb.driver.manual reference/operator/aggregation/changeStream $changeStream
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    inline fun <reified T : Any> watch(
        clientSession: ClientSession,
        pipeline: List<Bson>
    ): CoroutineChangeStreamPublisher<T> =
        collection.watch(clientSession, pipeline, T::class.java).coroutine


    /**
     * Aggregates documents according to the specified map-reduce function.
     *
     * @param mapFunction    A JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
     * @param reduceFunction A JavaScript function that "reduces" to a single object all the values associated with a particular key.
     * @param <T>      the target document type of the iterable.
     * @return a publisher containing the result of the map-reduce operation
     * @mongodb.driver.manual reference/command/mapReduce/ map-reduce
     */
    inline fun <reified T : Any> mapReduce(
        mapFunction: String,
        reduceFunction: String
    ): CoroutineMapReducePublisher<T> = collection.mapReduce(mapFunction, reduceFunction, T::class.java).coroutine


    /**
     * Aggregates documents according to the specified map-reduce function.
     *
     * @param clientSession the client session with which to associate this operation
     * @param mapFunction    A JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
     * @param reduceFunction A JavaScript function that "reduces" to a single object all the values associated with a particular key.
     * @param <T>      the target document type of the iterable.
     * @return a publisher containing the result of the map-reduce operation
     * @mongodb.driver.manual reference/command/mapReduce/ map-reduce
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    inline fun <reified T : Any> mapReduce(
        clientSession: ClientSession, mapFunction: String, reduceFunction: String
    ): CoroutineMapReducePublisher<T> =
        collection.mapReduce(clientSession, mapFunction, reduceFunction, T::class.java).coroutine

    /**
     * Executes a mix of inserts, updates, replaces, and deletes.
     *
     * @param requests the writes to execute
     * @param options  the options to apply to the bulk write operation
     * @return the BulkWriteResult
     */
    suspend fun bulkWrite(
        requests: List<WriteModel<out T>>,
        options: BulkWriteOptions = BulkWriteOptions()
    ): BulkWriteResult = collection.bulkWrite(requests, options).awaitSingle()


    /**
     * Executes a mix of inserts, updates, replaces, and deletes.
     *
     * @param clientSession the client session with which to associate this operation
     * @param requests the writes to execute
     * @param options  the options to apply to the bulk write operation
     * @return the BulkWriteResult
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun bulkWrite(
        clientSession: ClientSession, requests: List<WriteModel<out T>>,
        options: BulkWriteOptions = BulkWriteOptions()
    ): BulkWriteResult = collection.bulkWrite(clientSession, requests, options).awaitSingle()


    /**
     * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
     *
     * @param document the document to insert
     * @param options  the options to apply to the operation
     * com.mongodb.DuplicateKeyException or com.mongodb.MongoException
     * @since 1.2
     */
    suspend fun insertOne(document: T, options: InsertOneOptions = InsertOneOptions()): InsertOneResult =
        collection.insertOne(document, options).awaitSingle()

    /**
     * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
     *
     * @param clientSession the client session with which to associate this operation
     * @param document the document to insert
     * @param options  the options to apply to the operation
     * com.mongodb.DuplicateKeyException or com.mongodb.MongoException
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun insertOne(
        clientSession: ClientSession,
        document: T,
        options: InsertOneOptions = InsertOneOptions()
    ): InsertOneResult = collection.insertOne(clientSession, document, options).awaitSingle()


    /**
     * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when talking with a
     * server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to error handling.
     *
     * @param documents the documents to insert
     * @param options   the options to apply to the operation
     * com.mongodb.DuplicateKeyException or com.mongodb.MongoException
     */
    suspend fun insertMany(documents: List<T>, options: InsertManyOptions = InsertManyOptions()): InsertManyResult =
        collection.insertMany(documents, options).awaitSingle()

    /**
     * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when talking with a
     * server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to error handling.
     *
     * @param clientSession the client session with which to associate this operation
     * @param documents the documents to insert
     * @param options   the options to apply to the operation
     * com.mongodb.DuplicateKeyException or com.mongodb.MongoException
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun insertMany(
        clientSession: ClientSession,
        documents: List<T>,
        options: InsertManyOptions = InsertManyOptions()
    ): InsertManyResult =
        collection.insertMany(clientSession, documents, options).awaitSingle()

    /**
     * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
     * modified.
     *
     * @param filter the query filter to apply the the delete operation
     * @param options the options to apply to the delete operation
     * @return the DeleteResult or an com.mongodb.MongoException
     * @since 1.5
     */
    suspend fun deleteOne(filter: Bson, options: DeleteOptions = DeleteOptions()): DeleteResult =
        collection.deleteOne(filter, options).awaitSingle()

    /**
     * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
     * modified.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter the query filter to apply the the delete operation
     * @param options the options to apply to the delete operation
     * @return the DeleteResult or an com.mongodb.MongoException
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun deleteOne(
        clientSession: ClientSession,
        filter: Bson,
        options: DeleteOptions = DeleteOptions()
    ): DeleteResult =
        collection.deleteOne(clientSession, filter, options).awaitSingle()


    /**
     * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
     *
     * @param filter the query filter to apply the the delete operation
     * @param options the options to apply to the delete operation
     * @return the DeleteResult or an com.mongodb.MongoException
     * @since 1.5
     */
    suspend fun deleteMany(filter: Bson, options: DeleteOptions = DeleteOptions()): DeleteResult =
        collection.deleteMany(filter, options).awaitSingle()

    /**
     * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter the query filter to apply the the delete operation
     * @param options the options to apply to the delete operation
     * @return the DeleteResult or an com.mongodb.MongoException
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun deleteMany(
        clientSession: ClientSession,
        filter: Bson,
        options: DeleteOptions = DeleteOptions()
    ): DeleteResult =
        collection.deleteMany(clientSession, filter, options).awaitSingle()

    /**
     * Replace a document in the collection according to the specified arguments.
     *
     * @param filter      the query filter to apply the the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the replace operation
     * @return he UpdateResult
     * @mongodb.driver.manual tutorial/modify-documents/#replace-the-document Replace
     * @since 1.8
     */
    suspend fun replaceOne(filter: Bson, replacement: T, options: ReplaceOptions = ReplaceOptions()): UpdateResult =
        collection.replaceOne(filter, replacement, options).awaitSingle()

    /**
     * Replace a document in the collection according to the specified arguments.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter      the query filter to apply the the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the replace operation
     * @return the UpdateResult
     * @mongodb.driver.manual tutorial/modify-documents/#replace-the-document Replace
     * @mongodb.server.release 3.6
     * @since 1.8
     */
    suspend fun replaceOne(
        clientSession: ClientSession,
        filter: Bson,
        replacement: T,
        options: ReplaceOptions = ReplaceOptions()
    ): UpdateResult =
        collection.replaceOne(clientSession, filter, replacement, options).awaitSingle()

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter  a document describing the query filter, which may not be null.
     * @param update  a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param options the options to apply to the update operation
     * @return the UpdateResult
     * @mongodb.driver.manual tutorial/modify-documents/ Updates
     * @mongodb.driver.manual reference/operator/update/ Update Operators
     */
    suspend fun updateOne(filter: Bson, update: Bson, options: UpdateOptions = UpdateOptions()): UpdateResult =
        collection.updateOne(filter, update, options).awaitSingle()

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter  a document describing the query filter, which may not be null.
     * @param update  a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param options the options to apply to the update operation
     * @return the UpdateResult
     * @mongodb.driver.manual tutorial/modify-documents/ Updates
     * @mongodb.driver.manual reference/operator/update/ Update Operators
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun updateOne(
        clientSession: ClientSession,
        filter: Bson,
        update: Bson,
        options: UpdateOptions = UpdateOptions()
    ): UpdateResult =
        collection.updateOne(clientSession, filter, update, options).awaitSingle()

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter  a document describing the query filter, which may not be null.
     * @param update  a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param options the options to apply to the update operation
     * @return the UpdateResult
     * @mongodb.driver.manual tutorial/modify-documents/ Updates
     * @mongodb.driver.manual reference/operator/update/ Update Operators
     */
    suspend fun updateMany(filter: Bson, update: Bson, options: UpdateOptions = UpdateOptions()): UpdateResult =
        collection.updateMany(filter, update, options).awaitSingle()


    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter  a document describing the query filter, which may not be null.
     * @param update  a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param options the options to apply to the update operation
     * @return the UpdateResult
     * @mongodb.driver.manual tutorial/modify-documents/ Updates
     * @mongodb.driver.manual reference/operator/update/ Update Operators
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun updateMany(
        clientSession: ClientSession,
        filter: Bson,
        update: Bson,
        options: UpdateOptions = UpdateOptions()
    ): UpdateResult =
        collection.updateMany(clientSession, filter, update, options).awaitSingle()

    /**
     * Atomically find a document and remove it.
     *
     * @param filter  the query filter to find the document with
     * @param options the options to apply to the operation
     * @return the document that was removed.  If no documents matched the query filter, then null will be
     * returned
     */
    suspend fun findOneAndDelete(filter: Bson, options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()): T? =
        collection.findOneAndDelete(filter, options).awaitFirstOrNull()

    /**
     * Atomically find a document and remove it.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter  the query filter to find the document with
     * @param options the options to apply to the operation
     * @return a publisher with a single element the document that was removed.  If no documents matched the query filter, then null will be
     * returned
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun findOneAndDelete(
        clientSession: ClientSession,
        filter: Bson,
        options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
    ): T? = collection.findOneAndDelete(clientSession, filter, options).awaitFirstOrNull()

    /**
     * Atomically find a document and replace it.
     *
     * @param filter      the query filter to apply the the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the operation
     * @return the document that was replaced.  Depending on the value of the `returnOriginal`
     * property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
     * query filter, then null will be returned
     */
    suspend fun findOneAndReplace(
        filter: Bson,
        replacement: T,
        options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
    ): T? = collection.findOneAndReplace(filter, replacement, options).awaitFirstOrNull()

    /**
     * Atomically find a document and replace it.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter      the query filter to apply the the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the operation
     * @return the document that was replaced.  Depending on the value of the `returnOriginal`
     * property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
     * query filter, then null will be returned
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun findOneAndReplace(
        clientSession: ClientSession, filter: Bson, replacement: T,
        options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
    ): T? = collection.findOneAndReplace(clientSession, filter, replacement, options).awaitFirstOrNull()

    /**
     * Atomically find a document and update it.
     *
     * @param filter  a document describing the query filter, which may not be null.
     * @param update  a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param options the options to apply to the operation
     * @return the document that was updated.  Depending on the value of the `returnOriginal`
     * property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
     * query filter, then null will be returned
     */
    suspend fun findOneAndUpdate(
        filter: Bson,
        update: Bson,
        options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
    ): T? =
        collection.findOneAndUpdate(filter, update, options).awaitFirstOrNull()

    /**
     * Atomically find a document and update it.
     *
     * @param clientSession the client session with which to associate this operation
     * @param filter  a document describing the query filter, which may not be null.
     * @param update  a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param options the options to apply to the operation
     * @return a publisher with a single element the document that was updated.  Depending on the value of the `returnOriginal`
     * property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
     * query filter, then null will be returned
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun findOneAndUpdate(
        clientSession: ClientSession,
        filter: Bson,
        update: Bson,
        options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
    ): T? = collection.findOneAndUpdate(clientSession, filter, update, options).awaitFirstOrNull()

    /**
     * Drops this collection from the Database.
     *
     * @mongodb.driver.manual reference/command/drop/ Drop Collection
     */
    suspend fun drop() = collection.drop().awaitFirstOrNull()

    /**
     * Drops this collection from the Database.
     *
     * @param clientSession the client session with which to associate this operation
     * @mongodb.driver.manual reference/command/drop/ Drop Collection
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun drop(clientSession: ClientSession) = collection.drop(clientSession).awaitFirstOrNull()

    /**
     * Creates an index.
     *
     * @param key     an object describing the index key(s), which may not be null.
     * @param options the options for the index
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/method/db.collection.ensureIndex Ensure Index
     */
    suspend fun createIndex(key: Bson, options: IndexOptions = IndexOptions()): String =
        collection.createIndex(key, options).awaitSingle()

    /**
     * Creates an index.
     *
     * @param clientSession the client session with which to associate this operation
     * @param key     an object describing the index key(s), which may not be null.
     * @param options the options for the index
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/method/db.collection.ensureIndex Ensure Index
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun createIndex(clientSession: ClientSession, key: Bson, options: IndexOptions = IndexOptions()): String =
        collection.createIndex(clientSession, key, options).awaitSingle()

    /**
     * Create multiple indexes.
     *
     * @param indexes the list of indexes
     * @param createIndexOptions options to use when creating indexes
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/command/createIndexes Create indexes
     * @mongodb.server.release 2.6
     * @since 1.7
     */
    suspend fun createIndexes(
        indexes: List<IndexModel>,
        createIndexOptions: CreateIndexOptions = CreateIndexOptions()
    ): String =
        collection.createIndexes(indexes, createIndexOptions).awaitSingle()

    /**
     * Create multiple indexes.
     *
     * @param clientSession the client session with which to associate this operation
     * @param indexes the list of indexes
     * @param createIndexOptions options to use when creating indexes
     * @return a publisher with a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/command/createIndexes Create indexes
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun createIndexes(
        clientSession: ClientSession,
        indexes: List<IndexModel>,
        createIndexOptions: CreateIndexOptions = CreateIndexOptions()
    ): String =
        collection.createIndexes(clientSession, indexes, createIndexOptions).awaitSingle()

    /**
     * Get all the indexes in this collection.
     *
     * @param <T> the target document type of the iterable.
     * @return the fluent list indexes interface
     * @mongodb.driver.manual reference/command/listIndexes/ listIndexes
     */
    inline fun <reified T : Any> listIndexes(): CoroutineListIndexesPublisher<T> =
        collection.listIndexes(T::class.java).coroutine

    /**
     * Get all the indexes in this collection.
     *
     * @param clientSession the client session with which to associate this operation
     * @param <T> the target document type of the iterable.
     * @return the fluent list indexes interface
     * @mongodb.driver.manual reference/command/listIndexes/ listIndexes
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    inline fun <reified T : Any> listIndexes(
        clientSession: ClientSession
    ): CoroutineListIndexesPublisher<T> = collection.listIndexes(clientSession, T::class.java).coroutine

    /**
     * Drops the given index.
     *
     * @param indexName the name of the index to remove
     * @param dropIndexOptions options to use when dropping indexes
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/command/dropIndexes/ Drop Indexes
     * @since 1.7
     */
    suspend fun dropIndex(indexName: String, dropIndexOptions: DropIndexOptions = DropIndexOptions()) =
        collection.dropIndex(indexName, dropIndexOptions).awaitFirstOrNull()

    /**
     * Drops the index given the keys used to create it.
     *
     * @param keys the keys of the index to remove
     * @param dropIndexOptions options to use when dropping indexes
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/command/dropIndexes/ Drop indexes
     * @since 1.7
     */
    suspend fun dropIndex(keys: Bson, dropIndexOptions: DropIndexOptions = DropIndexOptions()) =
        collection.dropIndex(keys, dropIndexOptions).awaitFirstOrNull()

    /**
     * Drops the given index.
     *
     * @param clientSession the client session with which to associate this operation
     * @param indexName the name of the index to remove
     * @param dropIndexOptions options to use when dropping indexes
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/command/dropIndexes/ Drop Indexes
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun dropIndex(
        clientSession: ClientSession,
        indexName: String,
        dropIndexOptions: DropIndexOptions = DropIndexOptions()
    ) = collection.dropIndex(clientSession, indexName, dropIndexOptions).awaitFirstOrNull()

    /**
     * Drops the index given the keys used to create it.
     *
     * @param clientSession the client session with which to associate this operation
     * @param keys the keys of the index to remove
     * @param dropIndexOptions options to use when dropping indexes
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/command/dropIndexes/ Drop indexes
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun dropIndex(
        clientSession: ClientSession,
        keys: Bson,
        dropIndexOptions: DropIndexOptions = DropIndexOptions()
    ) = collection.dropIndex(clientSession, keys, dropIndexOptions).awaitFirstOrNull()


    /**
     * Drop all the indexes on this collection, except for the default on _id.
     *
     * @param dropIndexOptions options to use when dropping indexes
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/command/dropIndexes/ Drop Indexes
     * @since 1.7
     */
    suspend fun dropIndexes(dropIndexOptions: DropIndexOptions = DropIndexOptions()) =
        collection.dropIndexes(dropIndexOptions).awaitFirstOrNull()


    /**
     * Drop all the indexes on this collection, except for the default on _id.
     *
     * @param clientSession the client session with which to associate this operation
     * @param dropIndexOptions options to use when dropping indexes
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/command/dropIndexes/ Drop Indexes
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun dropIndexes(
        clientSession: ClientSession,
        dropIndexOptions: DropIndexOptions = DropIndexOptions()
    ) = collection.dropIndexes(clientSession, dropIndexOptions).awaitFirstOrNull()

    /**
     * Rename the collection with oldCollectionName to the newCollectionName.
     *
     * @param newCollectionNamespace the name the collection will be renamed to
     * @param options                the options for renaming a collection
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/commands/renameCollection Rename collection
     */
    suspend fun renameCollection(
        newCollectionNamespace: MongoNamespace,
        options: RenameCollectionOptions = RenameCollectionOptions()
    ) = collection.renameCollection(newCollectionNamespace, options).awaitFirstOrNull()

    /**
     * Rename the collection with oldCollectionName to the newCollectionName.
     *
     * @param clientSession the client session with which to associate this operation
     * @param newCollectionNamespace the name the collection will be renamed to
     * @param options                the options for renaming a collection
     * @return a single element indicating when the operation has completed
     * @mongodb.driver.manual reference/commands/renameCollection Rename collection
     * @mongodb.server.release 3.6
     * @since 1.7
     */
    suspend fun renameCollection(
        clientSession: ClientSession, newCollectionNamespace: MongoNamespace,
        options: RenameCollectionOptions = RenameCollectionOptions()
    ) = collection.renameCollection(clientSession, newCollectionNamespace, options).awaitFirstOrNull()


    /** KMongo extensions **/

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     * @param filter   the query filter
     * @return count of filtered collection
     */
    suspend fun countDocuments(filter: String, options: CountOptions = CountOptions()): Long =
        countDocuments(toBson(filter), options)

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   the query filter
     * @param options  optional parameter, the options describing the count * @return count of filtered collection
     */
    suspend fun countDocuments(
        clientSession: ClientSession,
        filter: String,
        options: CountOptions = CountOptions()
    ): Long = countDocuments(clientSession, toBson(filter), options)


    /**
     * Gets the distinct values of the specified field name.
     *
     * @param fieldName   the field name
     * @param filter      the query filter
     * @param <Type>   the target type of the iterable
     */
    inline fun <reified Type : Any> distinct(
        fieldName: String,
        filter: String
    ): CoroutineDistinctPublisher<Type> = distinct(fieldName, toBson(filter))

    /**
     * Gets the distinct values of the specified field.
     *
     * @param field   the field
     * @param filter      the query filter
     * @param <Type>   the target type of the iterable.
     */
    inline fun <reified Type : Any> distinct(
        field: KProperty1<T, Type>,
        filter: Bson = EMPTY_BSON
    ): CoroutineDistinctPublisher<Type> = distinct(field.path(), filter)

    /**
     * Finds all documents that match the filter in the collection.
     *
     * @param  filter the query filter
     * @return the find iterable interface
     */
    fun find(filter: String): CoroutineFindPublisher<T> = find(toBson(filter))

    /**
     * Finds all documents in the collection.
     *
     * @param filters the query filters
     * @return the find iterable interface
     */
    fun find(vararg filters: Bson?): CoroutineFindPublisher<T> = find(and(*filters))

    /**
     * Finds the first document that match the filter in the collection.
     *
     * @param filter the query filter
     */
    suspend fun findOne(filter: String = KMongoUtil.EMPTY_JSON): T? = find(filter).first()

    /**
     * Finds the first document that match the filter in the collection.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter the query filter
     */
    suspend fun findOne(
        clientSession: ClientSession,
        filter: String = KMongoUtil.EMPTY_JSON
    ): T? = find(clientSession, toBson(filter)).first()

    /**
     * Finds the first document that match the filter in the collection.
     *
     * @param filter the query filter
     */
    suspend fun findOne(filter: Bson): T? = find(filter).first()

    /**
     * Finds the first document that match the filter in the collection.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter the query filter
     */
    suspend fun findOne(clientSession: ClientSession, filter: Bson = EMPTY_BSON): T? =
        find(clientSession, filter).first()

    /**
     * Finds the first document that match the filters in the collection.
     *
     * @param filters the query filters
     * @return the first item returned or null
     */
    suspend fun findOne(vararg filters: Bson?): T? = find(*filters).first()

    /**
     * Finds the document that match the id parameter.
     *
     * @param id       the object id
     */
    suspend fun findOneById(id: Any): T? = findOne(idFilterQuery(id))

    /**
     * Finds the document that match the id parameter.
     *
     * @param id       the object id
     * @param clientSession  the client session with which to associate this operation
     */
    suspend fun findOneById(id: Any, clientSession: ClientSession): T? {
        return findOne(clientSession, idFilterQuery(id))
    }

    /**
     * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
     * modified.
     *
     * @param filter   the query filter to apply the the delete operation
     *
     * @return the result of the remove one operation
     */
    suspend fun deleteOne(
        filter: String,
        deleteOptions: DeleteOptions = DeleteOptions()
    ): DeleteResult = deleteOne(toBson(filter), deleteOptions)

    /**
     * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
     * modified.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   the query filter to apply the the delete operation
     *
     * @return the result of the remove one operation
     */
    suspend fun deleteOne(
        clientSession: ClientSession,
        filter: String,
        deleteOptions: DeleteOptions = DeleteOptions()
    ): DeleteResult = deleteOne(clientSession, toBson(filter), deleteOptions)

    /**
     * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
     * modified.
     *
     * @param filters the query filters to apply the the delete operation
     *
     * @return the result of the remove one operation
     */
    suspend fun deleteOne(
        vararg filters: Bson?,
        deleteOptions: DeleteOptions = DeleteOptions()
    ): DeleteResult = deleteOne(and(*filters), deleteOptions)

    /**
     * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
     * modified.
     * @param clientSession  the client session with which to associate this operation
     * @param filters the query filters to apply the the delete operation
     *
     * @return the result of the remove one operation
     */
    suspend fun deleteOne(
        clientSession: ClientSession,
        vararg filters: Bson?,
        deleteOptions: DeleteOptions = DeleteOptions()
    ): DeleteResult = deleteOne(clientSession, and(*filters), deleteOptions)

    /**
     * Removes at most one document from the id parameter.  If no documents match, the collection is not
     * modified.
     *
     * @param id   the object id
     */
    suspend fun deleteOneById(id: Any): DeleteResult =
        deleteOne(idFilterQuery(id))

    /**
     * Removes at most one document from the id parameter.  If no documents match, the collection is not
     * modified.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param id   the object id
     */
    suspend fun deleteOneById(clientSession: ClientSession, id: Any): DeleteResult =
        deleteOne(clientSession, idFilterQuery(id))


    /**
     * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
     * @param filter   the query filter to apply the the delete operation
     * @param options  the options to apply to the delete operation
     *
     * @return the result of the remove many operation
     */
    suspend fun deleteMany(
        filter: String = EMPTY_JSON,
        options: DeleteOptions = DeleteOptions()
    ): DeleteResult = deleteMany(toBson(filter), options)

    /**
     * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   the query filter to apply the the delete operation
     * @param options  the options to apply to the delete operation
     *
     * @return the result of the remove many operation
     */
    suspend fun deleteMany(
        clientSession: ClientSession,
        filter: String = EMPTY_JSON,
        options: DeleteOptions = DeleteOptions()
    ): DeleteResult = deleteMany(clientSession, toBson(filter), options)

    /**
     * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
     *
     * @param filters   the query filters to apply the the delete operation
     * @param options  the options to apply to the delete operation
     *
     * @return the result of the remove many operation
     */
    suspend fun deleteMany(
        vararg filters: Bson?,
        options: DeleteOptions = DeleteOptions()
    ): DeleteResult = deleteMany(and(*filters), options)

    /**
     * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filters   the query filters to apply the the delete operation
     * @param options  the options to apply to the delete operation
     *
     * @return the result of the remove many operation
     */
    suspend fun deleteMany(
        clientSession: ClientSession,
        vararg filters: Bson?,
        options: DeleteOptions = DeleteOptions()
    ): DeleteResult = deleteMany(clientSession, and(*filters), options)

    /**
     * Save the document.
     * If the document has no id field, or if the document has a null id value, insert the document.
     * Otherwise, call [replaceOneById] with upsert true.
     *
     * @param document the document to save
     */
    suspend fun save(document: T): UpdateResult? {
        val id = KMongoUtil.getIdValue(document)
        return if (id != null) {
            replaceOneById(id, document, ReplaceOptions().upsert(true))
        } else {
            insertOne(document)
            null
        }
    }

    /**
     * Replace a document in the collection according to the specified arguments.
     *
     * @param id          the object id
     * @param replacement the replacement document
     * @param options     the options to apply to the replace operation
     *
     * @return the result of the replace one operation
     */
    suspend fun <T : Any> replaceOneById(
        id: Any,
        replacement: T,
        options: ReplaceOptions = ReplaceOptions()
    ): UpdateResult = replaceOneWithoutId<T>(idFilterQuery(id), replacement, options)

    /**
     * Replace a document in the collection according to the specified arguments.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param id          the object id
     * @param replacement the replacement document
     * @param options     the options to apply to the replace operation
     *
     * @return the result of the replace one operation
     */
    suspend fun replaceOneById(
        clientSession: ClientSession,
        id: Any,
        replacement: T,
        options: ReplaceOptions = ReplaceOptions()
    ): UpdateResult = replaceOneWithoutId(clientSession, idFilterQuery(id), replacement, options)

    /**
     * Replace a document in the collection according to the specified arguments.
     *
     * @param filter      the query filter to apply to the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the replace operation
     *
     * @return the result of the replace one operation
     */
    suspend fun replaceOne(
        filter: String,
        replacement: T,
        options: ReplaceOptions = ReplaceOptions()
    ): UpdateResult = replaceOne(toBson(filter), replacement, options)

    /**
     * Replace a document in the collection according to the specified arguments.
     * The id of the provided document is not used, in order to avoid updated id error.
     * You may have to use [UpdateResult.getUpsertedId] in order to retrieve the generated id.
     *
     * @param filter      the query filter to apply to the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the replace operation
     *
     * @return the result of the replace one operation
     */
    suspend fun <T : Any> replaceOneWithoutId(
        filter: Bson,
        replacement: T,
        options: ReplaceOptions = ReplaceOptions()
    ): UpdateResult =
        withDocumentClass<BsonDocument>().replaceOne(
            filter,
            KMongoUtil.filterIdToBson(replacement),
            options
        )

    /**
     * Replace a document in the collection according to the specified arguments.
     * The id of the provided document is not used, in order to avoid updated id error.
     * You may have to use [UpdateResult.getUpsertedId] in order to retrieve the generated id.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter      the query filter to apply to the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the replace operation
     *
     * @return the result of the replace one operation
     */
    suspend fun replaceOneWithoutId(
        clientSession: ClientSession,
        filter: Bson,
        replacement: T,
        options: ReplaceOptions = ReplaceOptions()
    ): UpdateResult =
        withDocumentClass<BsonDocument>().replaceOne(
            clientSession,
            filter,
            KMongoUtil.filterIdToBson(replacement),
            options
        )

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter   a document describing the query filter
     * @param update   a document describing the update. The update to apply must include only update operators.
     * @param options  the options to apply to the update operation
     *
     * @return the result of the update one operation
     */
    suspend fun updateOne(
        filter: String,
        update: String,
        options: UpdateOptions = UpdateOptions()
    ): UpdateResult = updateOne(toBson(filter), toBson(update), options)

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   a document describing the query filter
     * @param update   a document describing the update. The update to apply must include only update operators.
     * @param options  the options to apply to the update operation
     *
     * @return the result of the update one operation
     */
    suspend fun updateOne(
        clientSession: ClientSession,
        filter: String,
        update: String,
        options: UpdateOptions = UpdateOptions()
    ): UpdateResult =
        updateOne(
            clientSession,
            toBson(filter),
            toBson(update),
            options
        )

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter   a document describing the query filter
     * @param update   the update object
     * @param options  the options to apply to the update operation
     * @param updateOnlyNotNullProperties if true do not change null properties
     *
     * @return the result of the update one operation
     */
    suspend fun updateOne(
        filter: String,
        update: Any,
        options: UpdateOptions = UpdateOptions(),
        updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
    ): UpdateResult = updateOne(toBson(filter), setModifier(update, updateOnlyNotNullProperties), options)

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   a document describing the query filter
     * @param update   the update object
     * @param options  the options to apply to the update operation
     * @param updateOnlyNotNullProperties if true do not change null properties
     *
     * @return the result of the update one operation
     */
    suspend fun updateOne(
        clientSession: ClientSession,
        filter: String,
        update: Any,
        options: UpdateOptions = UpdateOptions(),
        updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
    ): UpdateResult = updateOne(
        clientSession,
        toBson(filter),
        setModifier(update, updateOnlyNotNullProperties),
        options
    )

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter   a document describing the query filter
     * @param target  the update object - must have an non null id
     * @param options  the options to apply to the update operation
     * @param updateOnlyNotNullProperties if true do not change null properties
     *
     * @return the result of the update one operation
     */
    suspend fun updateOne(
        filter: Bson,
        target: T,
        options: UpdateOptions = UpdateOptions(),
        updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
    ): UpdateResult = updateOne(filter, toBsonModifier(target, updateOnlyNotNullProperties), options)

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   a document describing the query filter
     * @param target  the update object - must have an non null id
     * @param options  the options to apply to the update operation
     * @param updateOnlyNotNullProperties if true do not change null properties
     *
     * @return the result of the update one operation
     */
    suspend fun updateOne(
        clientSession: ClientSession,
        filter: Bson,
        target: T,
        options: UpdateOptions = UpdateOptions(),
        updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
    ): UpdateResult =
        updateOne(clientSession, filter, toBsonModifier(target, updateOnlyNotNullProperties), options)

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param id        the object id
     * @param update    the update object
     * @param options  the options to apply to the update operation
     * @param updateOnlyNotNullProperties if true do not change null properties
     *
     * @return the result of the update one operation
     */
    suspend fun updateOneById(
        id: Any,
        update: Any,
        options: UpdateOptions = UpdateOptions(),
        updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
    ): UpdateResult =
        updateOne(
            idFilterQuery(id),
            toBsonModifier(update, updateOnlyNotNullProperties),
            options
        )

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param id        the object id
     * @param update    the update object
     * @param options  the options to apply to the update operation
     * @param updateOnlyNotNullProperties if true do not change null properties
     *
     * @return the result of the update one operation
     */
    suspend fun updateOneById(
        clientSession: ClientSession,
        id: Any,
        update: Any,
        options: UpdateOptions = UpdateOptions(),
        updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
    ): UpdateResult =
        updateOne(
            clientSession,
            idFilterQuery(id),
            toBsonModifier(update, updateOnlyNotNullProperties),
            options
        )

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter   a document describing the query filter
     * @param update   a document describing the update. The update to apply must include only update operators.
     * @param updateOptions the options to apply to the update operation
     *
     * @return the result of the update many operation
     */
    suspend fun updateMany(
        filter: String,
        update: String,
        updateOptions: UpdateOptions = UpdateOptions()
    ): UpdateResult = updateMany(toBson(filter), toBson(update), updateOptions)

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   a document describing the query filter
     * @param update   a document describing the update. The update to apply must include only update operators.
     * @param updateOptions the options to apply to the update operation
     *
     * @return the result of the update many operation
     */
    suspend fun updateMany(
        clientSession: ClientSession,
        filter: String,
        update: String,
        updateOptions: UpdateOptions = UpdateOptions()
    ): UpdateResult =
        updateMany(
            clientSession,
            toBson(filter),
            toBson(update),
            updateOptions
        )

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter   a document describing the query filter
     * @param update   a document describing the update. The update to apply must include only update operators.
     * @param updateOptions the options to apply to the update operation
     *
     * @return the result of the update many operation
     */
    suspend fun updateMany(
        filter: Bson,
        vararg updates: SetTo<*>,
        updateOptions: UpdateOptions = UpdateOptions()
    ): UpdateResult = updateMany(filter, set(*updates), updateOptions)

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   a document describing the query filter
     * @param update   a document describing the update. The update to apply must include only update operators.
     * @param updateOptions the options to apply to the update operation
     *
     * @return the result of the update many operation
     */
    suspend fun updateMany(
        clientSession: ClientSession,
        filter: Bson,
        vararg updates: SetTo<*>,
        updateOptions: UpdateOptions = UpdateOptions()
    ): UpdateResult = updateMany(clientSession, filter, set(*updates), updateOptions)

    /**
     * Atomically find a document and remove it.
     *
     * @param filter   the query filter to find the document with
     * @param options  the options to apply to the operation
     *
     * @return the document that was removed.  If no documents matched the query filter, then null will be returned
     */
    suspend fun findOneAndDelete(
        filter: String,
        options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
    ): T? = findOneAndDelete(toBson(filter), options)

    /**
     * Atomically find a document and remove it.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   the query filter to find the document with
     * @param options  the options to apply to the operation
     *
     * @return the document that was removed.  If no documents matched the query filter, then null will be returned
     */
    suspend fun findOneAndDelete(
        clientSession: ClientSession,
        filter: String,
        options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
    ): T? = findOneAndDelete(clientSession, toBson(filter), options)

    /**
     * Atomically find a document and replace it.
     *
     * @param filter      the query filter to apply the the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the operation
     *
     * @return the document that was replaced.  Depending on the value of the `returnOriginal` property, this will either be the
     * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
     * returned
     */
    suspend fun findOneAndReplace(
        filter: String,
        replacement: T,
        options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
    ): T? = findOneAndReplace(toBson(filter), replacement, options)

    /**
     * Atomically find a document and replace it.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter      the query filter to apply the the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the operation
     *
     * @return the document that was replaced.  Depending on the value of the `returnOriginal` property, this will either be the
     * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
     * returned
     */
    suspend fun findOneAndReplace(
        clientSession: ClientSession,
        filter: String,
        replacement: T,
        options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
    ): T? =
        findOneAndReplace(
            clientSession,
            toBson(filter),
            replacement,
            options
        )

    /**
     * Atomically find a document and update it.
     *
     * @param filter   a document describing the query filter
     * @param update   a document describing the update. The update to apply must include only update operators.
     * @param options  the options to apply to the operation
     *
     * @return the document that was updated.  Depending on the value of the `returnOriginal` property, this will either be the
     * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
     * returned
     */
    suspend fun findOneAndUpdate(
        filter: String,
        update: String,
        options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
    ): T? = findOneAndUpdate(toBson(filter), toBson(update), options)

    /**
     * Atomically find a document and update it.
     *
     * @param clientSession  the client session with which to associate this operation
     * @param filter   a document describing the query filter
     * @param update   a document describing the update. The update to apply must include only update operators.
     * @param options  the options to apply to the operation
     *
     * @return the document that was updated.  Depending on the value of the `returnOriginal` property, this will either be the
     * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
     * returned
     */
    suspend fun findOneAndUpdate(
        clientSession: ClientSession,
        filter: String,
        update: String,
        options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
    ): T? =
        findOneAndUpdate(
            clientSession,
            toBson(filter),
            toBson(update),
            options
        )

    /**
     * Creates an index.  If successful, the callback will be executed with the name of the created index as the result.
     *
     * @param key      an object describing the index key(s)
     * @param options  the options for the index
     * @return the index name
     */
    suspend fun createIndex(
        key: String,
        options: IndexOptions = IndexOptions()
    ): String = createIndex(toBson(key), options)

    /**
     * Create an index with the given keys and options.
     * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
     * already exists, then drop the existing index and create a new one.
     *
     * @param keys      an object describing the index key(s)
     * @param indexOptions  the options for the index
     * @return the index name
     */
    suspend fun ensureIndex(
        keys: String,
        indexOptions: IndexOptions = IndexOptions()
    ): String? =
        try {
            createIndex(keys, indexOptions)
        } catch (e: MongoCommandException) {
            //there is an exception if the parameters of an existing index are changed.
            //then drop the index and create a new one
            dropIndex(keys)
            createIndex(keys, indexOptions)
        }

    /**
     * Create an index with the given keys and options.
     * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
     * already exists, then drop the existing index and create a new one.
     *
     * @param keys      an object describing the index key(s)
     * @param indexOptions  the options for the index
     * @return the index name
     */
    suspend fun ensureIndex(
        keys: Bson,
        indexOptions: IndexOptions = IndexOptions()
    ): String? =
        try {
            createIndex(keys, indexOptions)
        } catch (e: MongoCommandException) {
            //there is an exception if the parameters of an existing index are changed.
            //then drop the index and create a new one
            dropIndex(keys)
            createIndex(keys, indexOptions)
        }

    /**
     * Create an index with the given keys and options.
     * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
     * already exists, then drop the existing index and create a new one.
     *
     * @param properties    the properties, which must contain at least one
     * @param indexOptions  the options for the index
     * @return the index name
     */
    suspend fun ensureIndex(
        vararg properties: KProperty<*>,
        indexOptions: IndexOptions = IndexOptions()
    ): String? = ensureIndex(ascending(*properties), indexOptions)

    /**
     * Create an [IndexOptions.unique] index with the given keys and options.
     * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
     * already exists, then drop the existing index and create a new one.
     *
     * @param properties    the properties, which must contain at least one
     * @param indexOptions  the options for the index
     * @return the index name
     */
    suspend fun ensureUniqueIndex(
        vararg properties: KProperty<*>,
        indexOptions: IndexOptions = IndexOptions()
    ): String? = ensureIndex(ascending(*properties), indexOptions.unique(true))

    /**
     * Executes a mix of inserts, updates, replaces, and deletes.
     *
     * @param requests the writes to execute
     * @param options  the options to apply to the bulk write operation
     *
     * @return the result of the bulk write
     */
    suspend inline fun bulkWrite(
        vararg requests: WriteModel<T>,
        options: BulkWriteOptions = BulkWriteOptions()
    ): BulkWriteResult = bulkWrite(requests.toList(), options)


}

//extensions


/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param options  the options to apply to the operation
 */
suspend inline fun <reified T : Any> CoroutineCollection<T>.insertOne(
    document: String,
    options: InsertOneOptions = InsertOneOptions()
): InsertOneResult =
    withDocumentClass<BsonDocument>().insertOne(
        toBson(document, T::class),
        options
    )

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.

 * @param clientSession  the client session with which to associate this operation
 * @param document the document to insert
 * @param options  the options to apply to the operation
 */
suspend inline fun <reified T : Any> CoroutineCollection<T>.insertOne(
    clientSession: ClientSession,
    document: String,
    options: InsertOneOptions = InsertOneOptions()
): InsertOneResult =
    withDocumentClass<BsonDocument>().insertOne(
        clientSession,
        toBson(document, T::class),
        options
    )

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param replacement the document to replace - must have an non null id
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the replace one operation
 */
suspend inline fun <reified T : Any> CoroutineCollection<T>.replaceOne(
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult = replaceOneById(extractId(replacement, T::class), replacement, options)


/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 * @param updateOnlyNotNullProperties if true do not change null properties
 *
 * @return the result of the update one operation
 */
suspend inline fun <reified T : Any> CoroutineCollection<T>.updateOne(
    target: T,
    options: UpdateOptions = UpdateOptions(),
    updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
): UpdateResult = updateOneById(extractId(target, T::class), target, options, updateOnlyNotNullProperties)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 * @param updateOnlyNotNullProperties if true do not change null properties
 *
 * @return the result of the update one operation
 */
suspend inline fun <reified T : Any> CoroutineCollection<T>.updateOne(
    clientSession: ClientSession,
    target: T,
    options: UpdateOptions = UpdateOptions(),
    updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
): UpdateResult {
    return updateOneById(
        clientSession,
        extractId(target, T::class),
        target,
        options,
        updateOnlyNotNullProperties
    )
}


/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
suspend inline fun <reified T : Any> CoroutineCollection<T>.bulkWrite(
    clientSession: ClientSession,
    vararg requests: String,
    options: BulkWriteOptions = BulkWriteOptions()
): BulkWriteResult =
    withDocumentClass<BsonDocument>().bulkWrite(
        clientSession,
        KMongoUtil.toWriteModel(
            requests,
            codecRegistry,
            T::class
        ),
        options
    )

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
suspend inline fun <reified T : Any> CoroutineCollection<T>.bulkWrite(
    vararg requests: String,
    options: BulkWriteOptions = BulkWriteOptions()
): BulkWriteResult =
    withDocumentClass<BsonDocument>().bulkWrite(
        KMongoUtil.toWriteModel(
            requests,
            codecRegistry,
            T::class
        ),
        options
    )

/**
 * Aggregates documents according to the specified aggregation pipeline.  If the pipeline ends with a $out stage, the returned
 * iterable will be a query of the collection that the aggregation was written to.  Note that in this case the pipeline will be
 * executed even if the iterable is never iterated.
 *
 * @param pipeline    the aggregate pipeline
 * @param <T>   the target document type of the iterable
 */
inline fun <reified T : Any> CoroutineCollection<*>.aggregate(vararg pipeline: String): CoroutineAggregatePublisher<T> =
    aggregate(KMongoUtil.toBsonList(pipeline, codecRegistry))

/**
 * Aggregates documents according to the specified aggregation pipeline.  If the pipeline ends with a $out stage, the returned
 * iterable will be a query of the collection that the aggregation was written to.  Note that in this case the pipeline will be
 * executed even if the iterable is never iterated.
 *
 * @param pipeline    the aggregate pipeline
 * @param <T>   the target document type of the iterable
 */
inline fun <reified T : Any> CoroutineCollection<*>.aggregate(vararg pipeline: Bson): CoroutineAggregatePublisher<T> =
    aggregate(pipeline.toList())

/**
 * Returns the specified field for all matching documents.
 *
 * @param property the property to return
 * @param query the optional find query
 * @param options the optional [CoroutineFindPublisher] modifiers
 * @return a property value CoroutineFindPublisher
 */
inline fun <reified F : Any> CoroutineCollection<*>.projection(
    property: KProperty<F?>,
    query: Bson = EMPTY_BSON,
    options: (CoroutineFindPublisher<SingleProjection<F>>) -> CoroutineFindPublisher<SingleProjection<F>> = { it }
): CoroutineFindPublisher<F> =
    CoroutineFindPublisher(
        withDocumentClass<SingleProjection<F>>()
            .withCodecRegistry(singleProjectionCodecRegistry(property.path(), F::class, codecRegistry))
            .find(query)
            .let { options(it) }
            .projection(fields(excludeId(), include(property)))
            .publisher
            .map { it?.field }
    )

/**
 * Returns the specified two fields for all matching documents.
 *
 * @param property1 the first property to return
 * @param property2 the second property to return
 * @param query the optional find query
 * @param options the optional [CoroutineFindPublisher] modifiers
 * @return a pair of property values CoroutineFindPublisher
 */
inline fun <reified F1 : Any, reified F2 : Any> CoroutineCollection<*>.projection(
    property1: KProperty<F1?>,
    property2: KProperty<F2?>,
    query: Bson = EMPTY_BSON,
    options: (CoroutineFindPublisher<PairProjection<F1, F2>>) -> CoroutineFindPublisher<PairProjection<F1, F2>> = { it }
): CoroutineFindPublisher<Pair<F1?, F2?>> =
    CoroutineFindPublisher(
        withDocumentClass<PairProjection<F1, F2>>()
            .withCodecRegistry(
                pairProjectionCodecRegistry(
                    property1.path(),
                    F1::class,
                    property2.path(),
                    F2::class,
                    codecRegistry
                )
            )
            .find(query)
            .let { options(it) }
            .projection(fields(excludeId(), include(property1), include(property2)))
            .publisher
            .map { it?.field1 to it?.field2 }
    )

/**
 * Returns the specified three fields for all matching documents.
 *
 * @param property1 the first property to return
 * @param property2 the second property to return
 * @param property3 the third property to return
 * @param query the optional find query
 * @param options the optional [CoroutineFindPublisher] modifiers
 * @return a triple of property values CoroutineFindPublisher
 */
inline fun <reified F1 : Any, reified F2 : Any, reified F3 : Any> CoroutineCollection<*>.projection(
    property1: KProperty<F1?>,
    property2: KProperty<F2?>,
    property3: KProperty<F3?>,
    query: Bson = EMPTY_BSON,
    options: (CoroutineFindPublisher<TripleProjection<F1, F2, F3>>) -> CoroutineFindPublisher<TripleProjection<F1, F2, F3>> = { it }
): CoroutineFindPublisher<Triple<F1?, F2?, F3?>> =
    CoroutineFindPublisher(
        withDocumentClass<TripleProjection<F1, F2, F3>>()
            .withCodecRegistry(
                tripleProjectionCodecRegistry(
                    property1.path(),
                    F1::class,
                    property2.path(),
                    F2::class,
                    property3.path(),
                    F3::class,
                    codecRegistry
                )
            )
            .find(query)
            .let { options(it) }
            .projection(fields(excludeId(), include(property1), include(property2), include(property3)))
            .publisher
            .map { Triple(it?.field1, it?.field2, it?.field3) }
    )
