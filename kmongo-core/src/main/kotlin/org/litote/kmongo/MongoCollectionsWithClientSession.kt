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

package org.litote.kmongo

import com.mongodb.MongoCommandException
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.AggregateIterable
import com.mongodb.client.ClientSession
import com.mongodb.client.DistinctIterable
import com.mongodb.client.FindIterable
import com.mongodb.client.ListIndexesIterable
import com.mongodb.client.MapReduceIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.BulkWriteOptions
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.FindOneAndDeleteOptions
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.WriteModel
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.extractId
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
 * Counts the number of documents in the collection according to the given options.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter  the query filter
 * @param options the options describing the count
 *
 * @return the number of documents in the collection
 */
fun <T> MongoCollection<T>.countDocuments(
    clientSession: ClientSession,
    filter: String,
    options: CountOptions = CountOptions()
): Long =
    countDocuments(clientSession, toBson(filter), options)

/**
 * Gets the distinct values of the specified field name.
 *
 * @param clientSession the client session with which to associate this operation
 * @param fieldName   the field name
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable.
 *
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(
    clientSession: ClientSession,
    fieldName: String,
    filter: String = KMongoUtil.EMPTY_JSON
): DistinctIterable<TResult> = distinct(clientSession, fieldName, toBson(filter), TResult::class.java)


/**
 * Gets the distinct values of the specified field.
 *
 * @param clientSession the client session with which to associate this operation
 * @param field   the field
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable.
 *
 * @return an iterable of distinct values
 */
inline fun <reified T : Any, reified TResult> MongoCollection<T>.distinct(
    clientSession: ClientSession,
    field: KProperty1<T, TResult>,
    filter: Bson = EMPTY_BSON
): DistinctIterable<TResult> = distinct(clientSession, field.path(), filter, TResult::class.java)

/**
 * Finds all documents in the collection.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter the query filter
 * @return the find iterable interface
 */
fun <T> MongoCollection<T>.find(clientSession: ClientSession, filter: String = KMongoUtil.EMPTY_JSON): FindIterable<T> =
    find(clientSession, toBson(filter))

/**
 * Finds all documents in the collection.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filters the query filters
 * @return the find iterable interface
 */
fun <T> MongoCollection<T>.find(clientSession: ClientSession, vararg filters: Bson?): FindIterable<T> =
    find(clientSession, and(*filters))

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter the query filter
 * @return the first item returned or null
 */
fun <T> MongoCollection<T>.findOne(clientSession: ClientSession, filter: String = KMongoUtil.EMPTY_JSON): T? =
    find(clientSession, filter).firstOrNull()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter the query filter
 * @return the first item returned or null
 */
fun <T> MongoCollection<T>.findOne(clientSession: ClientSession, filter: Bson): T? =
    find(clientSession, filter).firstOrNull()

/**
 * Finds the first document that match the filters in the collection.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filters the query filters
 * @return the first item returned or null
 */
fun <T> MongoCollection<T>.findOne(clientSession: ClientSession, vararg filters: Bson?): T? =
    find(clientSession, *filters).firstOrNull()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filters the query filters
 * @return the first item returned or null
 */
inline fun <reified T : Any> MongoCollection<T>.findOne(clientSession: ClientSession, filters: () -> Bson): T? =
    findOne(clientSession, filters())

/**
 * Finds the document that match the id parameter.
 *
 * @param clientSession the client session with which to associate this operation
 * @param id       the object id
 * @return the first item returned or null
 */
fun <T> MongoCollection<T>.findOneById(clientSession: ClientSession, id: Any): T? =
    findOne(clientSession, KMongoUtil.idFilterQuery(id))

/**
 * Aggregates documents according to the specified aggregation pipeline.
 *
 * @param clientSession the client session with which to associate this operation
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable.
 *
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(
    clientSession: ClientSession,
    vararg pipeline: String
): AggregateIterable<TResult> =
    aggregate(clientSession, KMongoUtil.toBsonList(pipeline, codecRegistry), TResult::class.java)

/**
 * Aggregates documents according to the specified aggregation pipeline.
 *
 * @param clientSession the client session with which to associate this operation
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable.
 *
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(
    clientSession: ClientSession,
    vararg pipeline: Bson
): AggregateIterable<TResult> =
    aggregate(clientSession, pipeline.toList(), TResult::class.java)

/**
 * Aggregates documents according to the specified map-reduce function.
 *
 * @param clientSession the client session with which to associate this operation
 * @param mapFunction    A JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
 * @param reduceFunction A JavaScript function that "reduces" to a single object all the values associated with a particular key.
 * @param <TResult>      the target document type of the iterable.
 *
 *  @return an iterable containing the result of the map-reduce operation
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified TResult : Any> MongoCollection<*>.mapReduce(
    clientSession: ClientSession,
    mapFunction: String,
    reduceFunction: String
): MapReduceIterable<TResult> = mapReduceWith(clientSession, mapFunction, reduceFunction)

/**
 * Aggregates documents according to the specified map-reduce function.
 *
 * @param clientSession the client session with which to associate this operation
 * @param mapFunction    A JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
 * @param reduceFunction A JavaScript function that "reduces" to a single object all the values associated with a particular key.
 * @param <TResult>      the target document type of the iterable.
 *
 *  @return an iterable containing the result of the map-reduce operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.mapReduceWith(
    clientSession: ClientSession,
    mapFunction: String,
    reduceFunction: String
): MapReduceIterable<TResult> = mapReduce(clientSession, mapFunction, reduceFunction, TResult::class.java)

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param clientSession the client session with which to associate this operation
 * @param document the document to insert
 * @param options  the options to apply to the operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the insert command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoCommandException      if the write failed due to document validation reasons
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
inline fun <reified T : Any> MongoCollection<T>.insertOne(
    clientSession: ClientSession,
    document: String,
    options: InsertOneOptions = InsertOneOptions()
) = withDocumentClass<BsonDocument>().insertOne(clientSession, toBson(document, T::class), options)

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter the query filter to apply the the delete operation
 *
 * @return the result of the remove one operation
 *
 * @throws com.mongodb.MongoWriteException       if the write failed due some other failure specific to the delete command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.deleteOne(clientSession: ClientSession, filter: String): DeleteResult =
    deleteOne(clientSession, toBson(filter))

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filters the query filters to apply the the delete operation
 *
 * @return the result of the remove one operation
 *
 * @throws com.mongodb.MongoWriteException       if the write failed due some other failure specific to the delete command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.deleteOne(clientSession: ClientSession, vararg filters: Bson?): DeleteResult =
    deleteOne(clientSession, and(*filters))

/**
 * Removes at most one document from the id parameter.  If no documents match, the collection is not
 * modified.
 *
 * @param clientSession the client session with which to associate this operation
 * @param id   the object id
 *
 * @throws com.mongodb.MongoWriteException       if the write failed due some other failure specific to the delete command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.deleteOneById(clientSession: ClientSession, id: Any): DeleteResult =
    deleteOne(clientSession, KMongoUtil.idFilterQuery(id))

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 *
 * @return the result of the remove many operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the delete command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.deleteMany(
    clientSession: ClientSession,
    filter: String,
    options: DeleteOptions = DeleteOptions()
): DeleteResult =
    deleteMany(clientSession, toBson(filter), options)

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filters the query filters to apply the the delete operation
 * @param options  the options to apply to the delete operation
 *
 * @return the result of the remove many operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the delete command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.deleteMany(
    clientSession: ClientSession,
    vararg filters: Bson?,
    options: DeleteOptions = DeleteOptions()
): DeleteResult =
    deleteMany(clientSession, and(*filters), options)

/**
 * Save the document.
 * If the document has no id field, or if the document has a null id value, insert the document.
 * Otherwise, call [replaceOneById] with upsert true.
 *
 * @param clientSession the client session with which to associate this operation
 * @param document the document to save
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T : Any> MongoCollection<T>.save(clientSession: ClientSession, document: T) {
    val id = KMongoUtil.getIdValue(document)
    if (id != null) {
        replaceOneById(clientSession, id, document, ReplaceOptions().upsert(true))
    } else {
        insertOne(clientSession, document)
    }
}

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param id          the object id
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T : Any> MongoCollection<T>.replaceOneById(
    clientSession: ClientSession,
    id: Any,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult = withDocumentClass<BsonDocument>().replaceOne(
    clientSession,
    KMongoUtil.idFilterQuery(id),
    KMongoUtil.filterIdToBson(replacement),
    options
)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param replacement the document to replace - must have an non null id
 * @param options     the options to apply to the replace operation
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
inline fun <reified T : Any> MongoCollection<T>.replaceOne(
    clientSession: ClientSession,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult = replaceOneById(clientSession, extractId(replacement, T::class), replacement, options)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T : Any> MongoCollection<T>.replaceOne(
    clientSession: ClientSession,
    filter: String,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult = withDocumentClass<BsonDocument>()
    .replaceOne(
        clientSession,
        toBson(filter),
        KMongoUtil.filterIdToBson(replacement),
        options
    )

/**
 * Replace a document in the collection according to the specified arguments.
 * Same than [MongoCollection.replaceOne] but ensure that any _id present
 * in [replacement] is removed to avoid MongoWriteException such as:
 * "After applying the update, the (immutable) field '_id' was found to have been altered to _id"
 *
 * Note: Supports retryable writes on MongoDB server versions 3.6 or higher when the retryWrites setting is enabled.
 * @param clientSession the client session with which to associate this operation
 * @param filter        the query filter to apply the the replace operation
 * @param replacement   the replacement document
 * @param replaceOptions the options to apply to the replace operation
 * @return the result of the replace one operation
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the replace command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 * @since 3.7
 */
fun <T : Any> MongoCollection<T>.replaceOneWithFilter(
    clientSession: ClientSession,
    filter: Bson,
    replacement: T,
    replaceOptions: ReplaceOptions = ReplaceOptions()
): UpdateResult = withDocumentClass<BsonDocument>().replaceOne(
    clientSession,
    filter,
    KMongoUtil.filterIdToBson(replacement), replaceOptions
)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: String,
    update: String,
    options: UpdateOptions = UpdateOptions()
): UpdateResult = updateOne(clientSession, toBson(filter), toBson(update), options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param options  the options to apply to the update operation
 * @param updateOnlyNotNullProperties if true do not change null properties
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: String,
    update: Any,
    options: UpdateOptions = UpdateOptions(),
    updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
): UpdateResult =
    updateOne(
        clientSession,
        toBson(filter),
        toBsonModifier(update, updateOnlyNotNullProperties),
        options
    )

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 * @param updateOnlyNotNullProperties if true do not change null properties
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
inline fun <reified T : Any> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    target: T,
    options: UpdateOptions = UpdateOptions(),
    updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
): UpdateResult =
    updateOneById(clientSession, extractId(target, T::class), target, options, updateOnlyNotNullProperties)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param options  the options to apply to the update operation
 * @param updateOnlyNotNullProperties if true do not change null properties
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: Bson,
    target: Any,
    options: UpdateOptions = UpdateOptions(),
    updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
): UpdateResult =
    updateOne(clientSession, filter, toBsonModifier(target, updateOnlyNotNullProperties), options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter   a document describing the query filter
 * @param updates   the setTo describing the updates
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T : Any> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: Bson,
    vararg updates: SetTo<*>,
    updateOptions: UpdateOptions = UpdateOptions()
): UpdateResult = updateOne(clientSession, filter, set(*updates), updateOptions)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param id        the object id
 * @param update    the update object
 * @param options  the options to apply to the update operation
 * @param updateOnlyNotNullProperties if true do not change null properties
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.updateOneById(
    clientSession: ClientSession,
    id: Any,
    update: Any,
    options: UpdateOptions = UpdateOptions(),
    updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
): UpdateResult =
    updateOne(
        clientSession,
        KMongoUtil.idFilterQuery(id),
        toBsonModifier(update, updateOnlyNotNullProperties),
        options
    )

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter        a document describing the query filter, which may not be null.
 * @param update        a document describing the update, which may not be null. The update to apply must include only update operators.
 * @param updateOptions the options to apply to the update operation
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T : Any> MongoCollection<T>.updateMany(
    clientSession: ClientSession,
    filter: String,
    update: String,
    updateOptions: UpdateOptions = UpdateOptions()
): UpdateResult = updateMany(clientSession, toBson(filter), toBson(update), updateOptions)

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter        a document describing the query filter, which may not be null.
 * @param updates        a document describing the update, which may not be null. The update to apply must include only update operators.
 * @param updateOptions the options to apply to the update operation
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T : Any> MongoCollection<T>.updateMany(
    clientSession: ClientSession,
    filter: Bson,
    vararg updates: SetTo<*>,
    updateOptions: UpdateOptions = UpdateOptions()
): UpdateResult = updateMany(clientSession, filter, set(*updates), updateOptions)

/**
 * Atomically find a document and remove it.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter  the query filter to find the document with
 * @param options the options to apply to the operation
 *
 * @return the document that was removed.  If no documents matched the query filter, then null will be returned
 */
fun <T> MongoCollection<T>.findOneAndDelete(
    clientSession: ClientSession,
    filter: String,
    options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
): T? = findOneAndDelete(clientSession, toBson(filter), options)

/**
 * Atomically find a document and replace it.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter      the query filter to apply the the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the operation
 *
 * @return the document that was replaced.  Depending on the value of the `returnOriginal` property, this will either be the
 * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
 * returned
 */
fun <T> MongoCollection<T>.findOneAndReplace(
    clientSession: ClientSession,
    filter: String,
    replacement: T,
    options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
): T? = findOneAndReplace(clientSession, toBson(filter), replacement, options)

/**
 * Atomically find a document and update it.
 *
 * @param clientSession the client session with which to associate this operation
 * @param filter  a document describing the query filter, which may not be null.
 * @param update  a document describing the update, which may not be null. The update to apply must include only update operators.
 * @param options the options to apply to the operation
 *
 * @return the document that was updated.  Depending on the value of the `returnOriginal` property, this will either be the
 * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
 * returned
 */
fun <T> MongoCollection<T>.findOneAndUpdate(
    clientSession: ClientSession,
    filter: String,
    update: String,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): T? = findOneAndUpdate(clientSession, toBson(filter), toBson(update), options)

/**
 * Create an index with the given keys and options.
 *
 * @param clientSession the client session with which to associate this operation
 * @param keys an object describing the index key(s), which may not be null.
 * @param indexOptions the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.createIndex(
    clientSession: ClientSession,
    keys: String,
    indexOptions: IndexOptions = IndexOptions()
): String =
    createIndex(clientSession, toBson(keys), indexOptions)

/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param clientSession the client session with which to associate this operation
 * @param keys an object describing the index key(s), which may not be null.
 * @param indexOptions the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureIndex(
    clientSession: ClientSession,
    keys: Bson,
    indexOptions: IndexOptions = IndexOptions()
): String {
    return try {
        createIndex(clientSession, keys, indexOptions)
    } catch (e: MongoCommandException) {
        //there is an exception if the parameters of an existing index are changed.
        //then drop the index and create a new one
        try {
            dropIndex(clientSession, keys)
        } catch (e2: Exception) {
            //ignore
        }
        createIndex(clientSession, keys, indexOptions)
    }
}

/**
 * Create an ascending index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param clientSession the client session with which to associate this operation
 * @param keys the properties, which must contain at least one
 * @param indexOptions the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureIndex(
    clientSession: ClientSession,
    vararg properties: KProperty<*>,
    indexOptions: IndexOptions = IndexOptions()
): String = ensureIndex(clientSession, ascending(*properties), indexOptions)

/**
 * Create an [IndexOptions.unique] index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param clientSession the client session with which to associate this operation
 * @param keys the properties, which must contain at least one
 * @param indexOptions the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureUniqueIndex(
    clientSession: ClientSession,
    vararg properties: KProperty<*>,
    indexOptions: IndexOptions = IndexOptions()
): String = ensureIndex(clientSession, properties = *properties, indexOptions = indexOptions.unique(true))

/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param clientSession the client session with which to associate this operation
 * @param keys an object describing the index key(s), which may not be null.
 * @param indexOptions the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureIndex(
    clientSession: ClientSession,
    keys: String,
    indexOptions: IndexOptions = IndexOptions()
): String {
    return try {
        createIndex(clientSession, keys, indexOptions)
    } catch (e: MongoCommandException) {
        //there is an exception if the parameters of an existing index are changed.
        //then drop the index and create a new one
        try {
            dropIndexOfKeys(clientSession, keys)
        } catch (e2: Exception) {
            //ignore
        }
        createIndex(clientSession, keys, indexOptions)
    }
}

/**
 * Get all the indexes in this collection.
 *
 * @param clientSession the client session with which to associate this operation
 * @param <TResult>   the target document type of the iterable.
 * @return the list indexes iterable interface
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified TResult : Any> MongoCollection<*>.listIndexes(clientSession: ClientSession): ListIndexesIterable<TResult> =
    listTypedIndexes(clientSession)

/**
 * Get all the indexes in this collection.
 *
 * @param clientSession the client session with which to associate this operation
 * @param <TResult>   the target document type of the iterable.
 * @return the list indexes iterable interface
 */
inline fun <reified TResult : Any> MongoCollection<*>.listTypedIndexes(clientSession: ClientSession): ListIndexesIterable<TResult> =
    listIndexes(clientSession, TResult::class.java)


/**
 * Drops the index given the keys used to create it.
 *
 * @param clientSession the client session with which to associate this operation
 * @param keys the keys of the index to remove
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun <T> MongoCollection<T>.dropIndex(clientSession: ClientSession, keys: String) = dropIndexOfKeys(clientSession, keys)

/**
 * Drops the index given the keys used to create it.
 *
 * @param clientSession the client session with which to associate this operation
 * @param json the keys of the index to remove
 */
fun <T> MongoCollection<T>.dropIndexOfKeys(clientSession: ClientSession, json: String) =
    dropIndex(clientSession, toBson(json))


/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param clientSession the client session with which to associate this operation
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    clientSession: ClientSession,
    vararg requests: String,
    options: BulkWriteOptions = BulkWriteOptions()
): BulkWriteResult =
    withDocumentClass<BsonDocument>().bulkWrite(
        clientSession,
        KMongoUtil.toWriteModel(requests, codecRegistry, T::class),
        options
    )

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param clientSession the client session with which to associate this operation
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    clientSession: ClientSession,
    vararg requests: WriteModel<T>,
    options: BulkWriteOptions = BulkWriteOptions()
): BulkWriteResult = bulkWrite(clientSession, requests.toList(), options)


/**
 * Returns the specified field for all matching documents.
 *
 * @param clientSession the client session with which to associate this operation
 * @param property the property to return
 * @param query the optional find query
 * @param options the optional [FindIterable] modifiers
 * @return a property value iterable
 */
inline fun <T, reified F> MongoCollection<T>.projection(
    clientSession: ClientSession,
    property: KProperty<F>,
    query: Bson = EMPTY_BSON,
    options: (FindIterable<SingleProjection<F>>) -> FindIterable<SingleProjection<F>> = { it }
): MongoIterable<F> =
    withDocumentClass<SingleProjection<F>>()
        .withCodecRegistry(singleProjectionCodecRegistry(property.path(), F::class, codecRegistry))
        .find(clientSession, query)
        .let { options(it) }
        .projection(fields(excludeId(), include(property)))
        .map { it.field }

/**
 * Returns the specified two fields for all matching documents.
 *
 * @param clientSession the client session with which to associate this operation
 * @param property1 the first property to return
 * @param property2 the second property to return
 * @param query the optional find query
 * @param options the optional [FindIterable] modifiers
 * @return a pair of property values iterable
 */
inline fun <T, reified F1, reified F2> MongoCollection<T>.projection(
    clientSession: ClientSession,
    property1: KProperty<F1>,
    property2: KProperty<F2>,
    query: Bson = EMPTY_BSON,
    options: (FindIterable<PairProjection<F1, F2>>) -> FindIterable<PairProjection<F1, F2>> = { it }
): MongoIterable<Pair<F1?, F2?>> =
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
        .find(clientSession, query)
        .let { options(it) }
        .projection(fields(excludeId(), include(property1), include(property2)))
        .map { it.field1 to it.field2 }

/**
 * Returns the specified three fields for all matching documents.
 *
 * @param clientSession the client session with which to associate this operation
 * @param property1 the first property to return
 * @param property2 the second property to return
 * @param property3 the third property to return
 * @param query the optional find query
 * @param options the optional [FindIterable] modifiers
 * @return a triple of property values iterable
 */
inline fun <T, reified F1, reified F2, reified F3> MongoCollection<T>.projection(
    clientSession: ClientSession,
    property1: KProperty<F1>,
    property2: KProperty<F2>,
    property3: KProperty<F3>,
    query: Bson = EMPTY_BSON,
    options: (FindIterable<TripleProjection<F1, F2, F3>>) -> FindIterable<TripleProjection<F1, F2, F3>> = { it }
): MongoIterable<Triple<F1?, F2?, F3?>> =
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
        .find(clientSession, query)
        .let { options(it) }
        .projection(fields(excludeId(), include(property1), include(property2), include(property3)))
        .map { Triple(it.field1, it.field2, it.field3) }