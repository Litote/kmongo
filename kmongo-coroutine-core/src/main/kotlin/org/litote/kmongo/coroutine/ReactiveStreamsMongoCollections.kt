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

import com.mongodb.MongoCommandException
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.BulkWriteOptions
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.FindOneAndDeleteOptions
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.WriteModel
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.AggregatePublisher
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.DistinctPublisher
import com.mongodb.reactivestreams.client.FindPublisher
import com.mongodb.reactivestreams.client.ListIndexesPublisher
import com.mongodb.reactivestreams.client.MapReducePublisher
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.Success
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.SetTo
import org.litote.kmongo.and
import org.litote.kmongo.ascending
import org.litote.kmongo.path
import org.litote.kmongo.reactivestreams.withDocumentClass
import org.litote.kmongo.set
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param options  the options to apply to the operation
 */
suspend fun <T> MongoCollection<T>.insertOneAndAwait(
    document: T,
    options: InsertOneOptions = InsertOneOptions()
): Success = insertOne(document, options).awaitSingle()

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param document the document to insert
 * @param options  the options to apply to the operation
 */
suspend fun <T> MongoCollection<T>.insertOneAndAwait(
    clientSession: ClientSession,
    document: T,
    options: InsertOneOptions = InsertOneOptions()
): Success = insertOne(clientSession, document, options).awaitSingle()

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filter   the query filter
 * @return count of filtered collection
 */
suspend fun <T> MongoCollection<T>.countDocumentsAndAwait(
    clientSession: ClientSession,
    filter: Bson = EMPTY_BSON,
    options: CountOptions = CountOptions()
) = countDocuments(clientSession, filter, options).awaitSingle()

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @return count of filtered collection
 */
suspend fun <T> MongoCollection<T>.countDocumentsAndAwait(
    filter: Bson = EMPTY_BSON,
    options: CountOptions = CountOptions()
) = countDocuments(filter, options).awaitSingle()

/**
 * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when talking with a
 * server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to error handling.
 *
 * @param documents the documents to insert
 * @param options   the options to apply to the operation
 */
suspend fun <T> MongoCollection<T>.insertManyAndAwait(
    documents: List<T>, options: InsertManyOptions = InsertManyOptions()
): Success = insertMany(documents, options).awaitSingle()

/**
 * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when talking with a
 * server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to error handling.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param documents the documents to insert
 * @param options   the options to apply to the operation
 */
suspend fun <T> MongoCollection<T>.insertManyAndAwait(
    clientSession: ClientSession,
    documents: List<T>,
    options: InsertManyOptions = InsertManyOptions()
): Success =
    insertMany(clientSession, documents, options).awaitSingle()

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply the the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 */
suspend fun <T> MongoCollection<T>.replaceOneAndAwait(
    filter: Bson,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
)
        : UpdateResult = replaceOne(filter, replacement, options).awaitSingle()

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply the the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 */
suspend fun <T> MongoCollection<T>.replaceOneAndAwait(
    clientSession: ClientSession,
    filter: Bson,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult = replaceOne(clientSession, filter, replacement, options).awaitSingle()

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @return count of filtered collection
 */
suspend fun <T> MongoCollection<T>.countDocuments(filter: String, options: CountOptions = CountOptions()): Long =
    countDocuments(KMongoUtil.toBson(filter), options).awaitSingle()

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filter   the query filter
 * @param options  optional parameter, the options describing the count * @return count of filtered collection
 */
suspend fun <T> MongoCollection<T>.countDocuments(
    clientSession: ClientSession,
    filter: String,
    options: CountOptions = CountOptions()
): Long = countDocuments(clientSession, KMongoUtil.toBson(filter), options).awaitSingle()


/**
 * Gets the distinct values of the specified field name.
 *
 * @param fieldName   the field name
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(fieldName: String): DistinctPublisher<TResult> =
    distinct(fieldName, KMongoUtil.EMPTY_JSON)

/**
 * Gets the distinct values of the specified field name.
 *
 * @param fieldName   the field name
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(
    fieldName: String,
    filter: String
): DistinctPublisher<TResult> = distinct(fieldName, KMongoUtil.toBson(filter), TResult::class.java)

/**
 * Gets the distinct values of the specified field.
 *
 * @param field   the field
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable.
 *
 * @return an iterable of distinct values
 */
inline fun <reified T : Any, reified TResult> MongoCollection<T>.distinct(
    field: KProperty1<T, TResult>,
    filter: Bson = EMPTY_BSON
): DistinctPublisher<TResult> = distinct(field.path(), filter, TResult::class.java)

/**
 * Finds all documents that match the filter in the collection.
 *
 * @param  filter the query filter
 * @return the find iterable interface
 */
fun <T : Any> MongoCollection<T>.find(filter: String): FindPublisher<T> = find(KMongoUtil.toBson(filter))

/**
 * Finds all documents in the collection.
 *
 * @param filters the query filters
 * @return the find iterable interface
 */
fun <T> MongoCollection<T>.find(vararg filters: Bson?): FindPublisher<T> = find(and(*filters))

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 */
suspend fun <T : Any> MongoCollection<T>.findOne(filter: String = KMongoUtil.EMPTY_JSON): T? =
    find(filter).awaitFirstOrNull()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filter the query filter
 */
suspend fun <T : Any> MongoCollection<T>.findOne(
    clientSession: ClientSession,
    filter: String = KMongoUtil.EMPTY_JSON
): T? = find(clientSession, KMongoUtil.toBson(filter)).awaitFirstOrNull()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 */
suspend fun <T : Any> MongoCollection<T>.findOne(filter: Bson): T? = find(filter).awaitFirstOrNull()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filter the query filter
 */
suspend fun <T : Any> MongoCollection<T>.findOne(clientSession: ClientSession, filter: Bson): T? =
    find(clientSession, filter).awaitFirstOrNull()

/**
 * Finds the first document that match the filters in the collection.
 *
 * @param filters the query filters
 * @return the first item returned or null
 */
suspend fun <T> MongoCollection<T>.findOne(vararg filters: Bson?): T? = find(*filters).awaitFirstOrNull()

/**
 * Finds the document that match the id parameter.
 *
 * @param id       the object id
 */
suspend fun <T : Any> MongoCollection<T>.findOneById(id: Any): T? = findOne(KMongoUtil.idFilterQuery(id))

/**
 * Finds the document that match the id parameter.
 *
 * @param id       the object id
 * @param clientSession  the client session with which to associate this operation
 */
suspend fun <T : Any> MongoCollection<T>.findOneById(id: Any, clientSession: ClientSession): T? {
    return findOne(clientSession, KMongoUtil.idFilterQuery(id))
}

/**
 * Aggregates documents according to the specified aggregation pipeline.  If the pipeline ends with a $out stage, the returned
 * iterable will be a query of the collection that the aggregation was written to.  Note that in this case the pipeline will be
 * executed even if the iterable is never iterated.
 *
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: String): AggregatePublisher<TResult> =
    aggregate(KMongoUtil.toBsonList(pipeline, codecRegistry), TResult::class.java)

/**
 * Aggregates documents according to the specified aggregation pipeline.  If the pipeline ends with a $out stage, the returned
 * iterable will be a query of the collection that the aggregation was written to.  Note that in this case the pipeline will be
 * executed even if the iterable is never iterated.
 *
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: Bson): AggregatePublisher<TResult> =
    aggregate(pipeline.toList(), TResult::class.java)

/**
 * Aggregates documents according to the specified map-reduce function.
 *
 * @param mapFunction    a JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
 * @param reduceFunction a JavaScript function that "reduces" to a single object all the values associated with a particular key.
 * @param <TResult>      the target document type of the iterable.
 * *
 * @return an iterable containing the result of the map-reduce operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.mapReduceTyped(
    mapFunction: String,
    reduceFunction: String
): MapReducePublisher<TResult> = mapReduce(mapFunction, reduceFunction, TResult::class.java)

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param options  the options to apply to the operation
 */
suspend inline fun <reified T : Any> MongoCollection<T>.insertOne(
    document: String,
    options: InsertOneOptions = InsertOneOptions()
): Success =
    withDocumentClass<BsonDocument>().insertOne(
        KMongoUtil.toBson(document, T::class),
        options
    ).awaitSingle()

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.

 * @param clientSession  the client session with which to associate this operation
 * @param document the document to insert
 * @param options  the options to apply to the operation
 */
suspend inline fun <reified T : Any> MongoCollection<T>.insertOne(
    clientSession: ClientSession,
    document: String,
    options: InsertOneOptions = InsertOneOptions()
) =
    withDocumentClass<BsonDocument>().insertOne(
        clientSession,
        KMongoUtil.toBson(document, T::class),
        options
    ).awaitSingle()

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filter   the query filter to apply the the delete operation
 *
 * @return the result of the remove one operation
 */
suspend fun <T> MongoCollection<T>.deleteOne(
    filter: String,
    deleteOptions: DeleteOptions = DeleteOptions()
): DeleteResult = deleteOne(KMongoUtil.toBson(filter), deleteOptions).awaitSingle()

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filter   the query filter to apply the the delete operation
 *
 * @return the result of the remove one operation
 */
suspend fun <T> MongoCollection<T>.deleteOne(
    clientSession: ClientSession,
    filter: String,
    deleteOptions: DeleteOptions = DeleteOptions()
): DeleteResult = deleteOne(clientSession, KMongoUtil.toBson(filter), deleteOptions).awaitSingle()

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filters the query filters to apply the the delete operation
 *
 * @return the result of the remove one operation
 */
suspend fun <T> MongoCollection<T>.deleteOne(
    vararg filters: Bson?,
    deleteOptions: DeleteOptions = DeleteOptions()
): DeleteResult = deleteOne(and(*filters), deleteOptions).awaitSingle()

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 * @param clientSession  the client session with which to associate this operation
 * @param filters the query filters to apply the the delete operation
 *
 * @return the result of the remove one operation
 */
suspend fun <T> MongoCollection<T>.deleteOne(
    clientSession: ClientSession,
    vararg filters: Bson?,
    deleteOptions: DeleteOptions = DeleteOptions()
): DeleteResult = deleteOne(clientSession, and(*filters), deleteOptions).awaitSingle()

/**
 * Removes at most one document from the id parameter.  If no documents match, the collection is not
 * modified.
 *
 * @param id   the object id
 */
suspend fun <T> MongoCollection<T>.deleteOneById(id: Any): DeleteResult =
    deleteOne(KMongoUtil.idFilterQuery(id)).awaitSingle()


/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 * @param filter   the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 *
 * @return the result of the remove many operation
 */
suspend fun <T> MongoCollection<T>.deleteMany(
    filter: String,
    options: DeleteOptions = DeleteOptions()
): DeleteResult = deleteMany(KMongoUtil.toBson(filter), options).awaitSingle()

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filter   the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 *
 * @return the result of the remove many operation
 */
suspend fun <T> MongoCollection<T>.deleteMany(
    clientSession: ClientSession,
    filter: String,
    options: DeleteOptions = DeleteOptions()
): DeleteResult = deleteMany(clientSession, KMongoUtil.toBson(filter), options).awaitSingle()

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filters   the query filters to apply the the delete operation
 * @param options  the options to apply to the delete operation
 *
 * @return the result of the remove many operation
 */
suspend fun <T> MongoCollection<T>.deleteMany(
    vararg filters: Bson?,
    options: DeleteOptions = DeleteOptions()
): DeleteResult = deleteMany(and(*filters), options).awaitSingle()

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filters   the query filters to apply the the delete operation
 * @param options  the options to apply to the delete operation
 *
 * @return the result of the remove many operation
 */
suspend fun <T> MongoCollection<T>.deleteMany(
    clientSession: ClientSession,
    vararg filters: Bson?,
    options: DeleteOptions = DeleteOptions()
): DeleteResult = deleteMany(clientSession, and(*filters), options).awaitSingle()

/**
 * Save the document.
 * If the document has no id field, or if the document has a null id value, insert the document.
 * Otherwise, call [replaceOneById] with upsert true.
 *
 * @param document the document to save
 */
suspend fun <T : Any> MongoCollection<T>.save(document: T): UpdateResult? {
    val id = KMongoUtil.getIdValue(document)
    return if (id != null) {
        replaceOneById(id, document, ReplaceOptions().upsert(true))
    } else {
        insertOne(document).awaitSingle()
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
suspend fun <T : Any> MongoCollection<T>.replaceOneById(
    id: Any,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult = replaceOneWithoutId(KMongoUtil.idFilterQuery(id), replacement, options)

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
suspend fun <T : Any> MongoCollection<T>.replaceOneById(
    clientSession: ClientSession,
    id: Any,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult = replaceOneWithoutId(clientSession, KMongoUtil.idFilterQuery(id), replacement, options)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param replacement the document to replace - must have an non null id
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the replace one operation
 */
suspend inline fun <reified T : Any> MongoCollection<T>.replaceOne(
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult = replaceOneById(KMongoUtil.extractId(replacement, T::class), replacement, options)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the replace one operation
 */
suspend fun <T : Any> MongoCollection<T>.replaceOne(
    filter: String,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult = replaceOne(KMongoUtil.toBson(filter), replacement, options).awaitSingle()

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
suspend fun <T : Any> MongoCollection<T>.replaceOneWithoutId(
    filter: Bson,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult =
    withDocumentClass<BsonDocument>().replaceOne(
        filter,
        KMongoUtil.filterIdToBson(replacement),
        options
    ).awaitSingle()

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
suspend fun <T : Any> MongoCollection<T>.replaceOneWithoutId(
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
    ).awaitSingle()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
suspend fun <T> MongoCollection<T>.updateOne(
    filter: String,
    update: String,
    options: UpdateOptions = UpdateOptions()
): UpdateResult = updateOne(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), options).awaitSingle()

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
suspend fun <T> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: String,
    update: String,
    options: UpdateOptions = UpdateOptions()
): UpdateResult =
    updateOne(
        clientSession,
        KMongoUtil.toBson(filter),
        KMongoUtil.toBson(update),
        options
    ).awaitSingle()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
suspend fun <T> MongoCollection<T>.updateOne(
    filter: String,
    update: Any,
    options: UpdateOptions = UpdateOptions()
): UpdateResult = updateOne(KMongoUtil.toBson(filter), KMongoUtil.setModifier(update), options).awaitSingle()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
suspend fun <T> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: String,
    update: Any,
    options: UpdateOptions = UpdateOptions()
): UpdateResult = updateOne(
    clientSession,
    KMongoUtil.toBson(filter),
    KMongoUtil.setModifier(update),
    options
).awaitSingle()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
suspend inline fun <reified T : Any> MongoCollection<T>.updateOne(
    filter: Bson,
    target: T,
    options: UpdateOptions = UpdateOptions()
): UpdateResult = updateOne(filter, KMongoUtil.toBsonModifier(target), options).awaitSingle()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filter   a document describing the query filter
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
suspend inline fun <reified T : Any> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: Bson,
    target: T,
    options: UpdateOptions = UpdateOptions()
): UpdateResult = updateOne(clientSession, filter, KMongoUtil.toBsonModifier(target), options).awaitSingle()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
suspend inline fun <reified T : Any> MongoCollection<T>.updateOne(
    target: T,
    options: UpdateOptions = UpdateOptions()
): UpdateResult = updateOneById(KMongoUtil.extractId(target, T::class), target, options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
suspend inline fun <reified T : Any> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    target: T,
    options: UpdateOptions = UpdateOptions()
): UpdateResult? {
    return updateOneById(clientSession, KMongoUtil.extractId(target, T::class), target, options)
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param id        the object id
 * @param update    the update object
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
suspend fun <T> MongoCollection<T>.updateOneById(
    id: Any,
    update: Any,
    options: UpdateOptions = UpdateOptions()
): UpdateResult =
    updateOne(
        KMongoUtil.idFilterQuery(id),
        KMongoUtil.toBsonModifier(update),
        options
    ).awaitSingle()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param id        the object id
 * @param update    the update object
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
suspend fun <T> MongoCollection<T>.updateOneById(
    clientSession: ClientSession,
    id: Any,
    update: Any,
    options: UpdateOptions = UpdateOptions()
): UpdateResult =
    updateOne(
        clientSession,
        KMongoUtil.idFilterQuery(id),
        KMongoUtil.toBsonModifier(update),
        options
    ).awaitSingle()

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param updateOptions the options to apply to the update operation
 *
 * @return the result of the update many operation
 */
suspend fun <T> MongoCollection<T>.updateMany(
    filter: String,
    update: String,
    updateOptions: UpdateOptions = UpdateOptions()
): UpdateResult = updateMany(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), updateOptions).awaitSingle()

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
suspend fun <T> MongoCollection<T>.updateMany(
    clientSession: ClientSession,
    filter: String,
    update: String,
    updateOptions: UpdateOptions = UpdateOptions()
): UpdateResult =
    updateMany(
        clientSession,
        KMongoUtil.toBson(filter),
        KMongoUtil.toBson(update),
        updateOptions
    ).awaitSingle()

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param updateOptions the options to apply to the update operation
 *
 * @return the result of the update many operation
 */
suspend fun <T> MongoCollection<T>.updateMany(
    filter: Bson,
    vararg updates: SetTo<*>,
    updateOptions: UpdateOptions = UpdateOptions()
): UpdateResult = updateMany(filter, set(*updates), updateOptions).awaitSingle()

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
suspend fun <T> MongoCollection<T>.updateMany(
    clientSession: ClientSession,
    filter: Bson,
    vararg updates: SetTo<*>,
    updateOptions: UpdateOptions = UpdateOptions()
): UpdateResult = updateMany(clientSession, filter, set(*updates), updateOptions).awaitSingle()

/**
 * Atomically find a document and remove it.
 *
 * @param filter   the query filter to find the document with
 * @param options  the options to apply to the operation
 *
 * @return the document that was removed.  If no documents matched the query filter, then null will be returned
 */
suspend fun <T : Any> MongoCollection<T>.findOneAndDelete(
    filter: String,
    options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
): T? = findOneAndDelete(KMongoUtil.toBson(filter), options).awaitFirstOrNull()

/**
 * Atomically find a document and remove it.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param filter   the query filter to find the document with
 * @param options  the options to apply to the operation
 *
 * @return the document that was removed.  If no documents matched the query filter, then null will be returned
 */
suspend fun <T : Any> MongoCollection<T>.findOneAndDelete(
    clientSession: ClientSession,
    filter: String,
    options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
): T? = findOneAndDelete(clientSession, KMongoUtil.toBson(filter), options).awaitFirstOrNull()

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
suspend fun <T> MongoCollection<T>.findOneAndReplace(
    filter: String,
    replacement: T,
    options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
): T? = findOneAndReplace(KMongoUtil.toBson(filter), replacement, options).awaitFirstOrNull()

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
suspend fun <T> MongoCollection<T>.findOneAndReplace(
    clientSession: ClientSession,
    filter: String,
    replacement: T,
    options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
): T? =
    findOneAndReplace(
        clientSession,
        KMongoUtil.toBson(filter),
        replacement,
        options
    ).awaitFirstOrNull()

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
suspend fun <T : Any> MongoCollection<T>.findOneAndUpdate(
    filter: String,
    update: String,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): T? = findOneAndUpdate(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), options).awaitFirstOrNull()

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
suspend fun <T : Any> MongoCollection<T>.findOneAndUpdate(
    clientSession: ClientSession,
    filter: String,
    update: String,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): T? =
    findOneAndUpdate(
        clientSession,
        KMongoUtil.toBson(filter),
        KMongoUtil.toBson(update),
        options
    ).awaitFirstOrNull()

/**
 * Creates an index.  If successful, the callback will be executed with the name of the created index as the result.
 *
 * @param key      an object describing the index key(s)
 * @param options  the options for the index
 * @return the index name
 */
suspend fun <T> MongoCollection<T>.createIndex(
    key: String,
    options: IndexOptions = IndexOptions()
): String = createIndex(KMongoUtil.toBson(key), options).awaitSingle()

/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param keys      an object describing the index key(s)
 * @param indexOptions  the options for the index
 * @return the index name
 */
suspend fun <T> MongoCollection<T>.ensureIndex(
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
suspend fun <T> MongoCollection<T>.ensureIndex(
    keys: Bson,
    indexOptions: IndexOptions = IndexOptions()
): String? =
    try {
        createIndex(keys, indexOptions).awaitSingle()
    } catch (e: MongoCommandException) {
        //there is an exception if the parameters of an existing index are changed.
        //then drop the index and create a new one
        dropIndex(keys).awaitSingle()
        createIndex(keys, indexOptions).awaitSingle()
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
suspend fun <T> MongoCollection<T>.ensureIndex(
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
suspend fun <T> MongoCollection<T>.ensureUniqueIndex(
    vararg properties: KProperty<*>,
    indexOptions: IndexOptions = IndexOptions()
): String? = ensureIndex(ascending(*properties), indexOptions.unique(true))

/**
 * Get all the indexes in this collection.
 *
 * @param <TResult>   the target document type of the iterable.
 * @return the list indexes iterable interface
 */
inline fun <reified TResult : Any> MongoCollection<*>.listTypedIndexes(): ListIndexesPublisher<TResult> =
    listIndexes(TResult::class.java)

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 *
 * @return the result of the bulk write
 */
suspend inline fun <reified T : Any> MongoCollection<T>.bulkWrite(vararg requests: String): BulkWriteResult =
    withDocumentClass<BsonDocument>().bulkWrite(
        KMongoUtil.toWriteModel(
            requests,
            codecRegistry,
            T::class
        ),
        BulkWriteOptions()
    ).awaitSingle()

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
suspend inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
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
    ).awaitSingle()

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param clientSession  the client session with which to associate this operation
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
suspend inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
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
    ).awaitSingle()

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
suspend inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    vararg requests: WriteModel<T>,
    options: BulkWriteOptions = BulkWriteOptions()
): BulkWriteResult = bulkWrite(requests.toList(), options).awaitSingle()