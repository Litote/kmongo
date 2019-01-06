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

package org.litote.kmongo.async

import com.mongodb.async.client.AggregateIterable
import com.mongodb.async.client.DistinctIterable
import com.mongodb.async.client.FindIterable
import com.mongodb.async.client.ListIndexesIterable
import com.mongodb.async.client.MapReduceIterable
import com.mongodb.async.client.MongoCollection
import com.mongodb.bulk.BulkWriteResult
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
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.SetTo
import org.litote.kmongo.and
import org.litote.kmongo.ascending
import org.litote.kmongo.path
import org.litote.kmongo.set
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @param callback the callback passed the number of documents in the collection
 */
@Deprecated("use countDocuments instead")
fun <T> MongoCollection<T>.count(filter: String, callback: (Long?, Throwable?) -> Unit) =
    count(filter, CountOptions(), callback)

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @param options  the options describing the count
 * @param callback the callback passed the number of documents in the collection
 */
@Deprecated("use countDocuments instead")
fun <T> MongoCollection<T>.count(filter: String, options: CountOptions, callback: (Long?, Throwable?) -> Unit) =
    count(KMongoUtil.toBson(filter), options, callback)

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @param callback the callback passed the number of documents in the collection
 */
fun <T> MongoCollection<T>.countDocuments(filter: String, callback: (Long?, Throwable?) -> Unit) =
    countDocuments(filter, CountOptions(), callback)

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @param options  the options describing the count
 * @param callback the callback passed the number of documents in the collection
 */
fun <T> MongoCollection<T>.countDocuments(
    filter: String,
    options: CountOptions,
    callback: (Long?, Throwable?) -> Unit
) =
    countDocuments(KMongoUtil.toBson(filter), options, callback)


/**
 * Gets the distinct values of the specified field name.
 *
 * @param fieldName   the field name
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(fieldName: String): DistinctIterable<TResult> =
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
): DistinctIterable<TResult> = distinct(fieldName, KMongoUtil.toBson(filter), TResult::class.java)

/**
 * Gets the distinct values of the specified field.
 *
 * @param fieldName   the field
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable.
 *
 * @return an iterable of distinct values
 */
inline fun <reified T : Any, reified TResult> MongoCollection<T>.distinct(
    field: KProperty1<T, TResult>,
    filter: Bson = EMPTY_BSON
): DistinctIterable<TResult> = distinct(field.path(), filter, TResult::class.java)

/**
 * Finds all documents that match the filter in the collection.
 *
 * @param  filter the query filter
 * @return the find iterable interface
 */
fun <T> MongoCollection<T>.find(filter: String): FindIterable<T> = find(KMongoUtil.toBson(filter))

/**
 * Finds all documents in the collection.
 *
 * @param filters the query filters
 * @return the find iterable interface
 */
fun <T> MongoCollection<T>.find(vararg filters: Bson?): FindIterable<T> = find(and(*filters))

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 * @param callback a callback that is passed the first item or null
 */
fun <T> MongoCollection<T>.findOne(filter: String = KMongoUtil.EMPTY_JSON, callback: (T?, Throwable?) -> Unit) =
    find(filter).first(callback)

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 * @param callback a callback that is passed the first item or null
 */
fun <T> MongoCollection<T>.findOne(filter: Bson, callback: (T?, Throwable?) -> Unit) =
    find(filter).first(callback)

/**
 * Finds the first document that match the filters in the collection.
 *
 * @param filters the query filters
 * @param callback a callback that is passed the first item or null
 */
fun <T> MongoCollection<T>.findOne(vararg filters: Bson?, callback: (T?, Throwable?) -> Unit) =
    findOne(and(*filters), callback)

/**
 * Finds the document that match the id parameter.
 *
 * @param id       the object id
 * @param callback a callback that is passed the first item or null
 */
fun <T> MongoCollection<T>.findOneById(id: Any, callback: (T?, Throwable?) -> Unit) =
    findOne(KMongoUtil.idFilterQuery(id), callback)

/**
 * Aggregates documents according to the specified aggregation pipeline.  If the pipeline ends with a $out stage, the returned
 * iterable will be a query of the collection that the aggregation was written to.  Note that in this case the pipeline will be
 * executed even if the iterable is never iterated.
 *
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: String): AggregateIterable<TResult> =
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
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: Bson): AggregateIterable<TResult> =
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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified TResult : Any> MongoCollection<*>.mapReduce(
    mapFunction: String,
    reduceFunction: String
): MapReduceIterable<TResult> = mapReduceWith(mapFunction, reduceFunction)

/**
 * Aggregates documents according to the specified map-reduce function.
 *
 * @param mapFunction    a JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
 * @param reduceFunction a JavaScript function that "reduces" to a single object all the values associated with a particular key.
 * @param <TResult>      the target document type of the iterable.
 * *
 * @return an iterable containing the result of the map-reduce operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.mapReduceWith(
    mapFunction: String,
    reduceFunction: String
): MapReduceIterable<TResult> = mapReduce(mapFunction, reduceFunction, TResult::class.java)


/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param callback the callback that is completed once the insert has completed
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoCommandException      returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
inline fun <reified T : Any> MongoCollection<T>.insertOne(
    document: String,
    noinline callback: (Void?, Throwable?) -> Unit
) = insertOne(document, InsertOneOptions(), callback)

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param options  the options to apply to the operation
 * @param callback the callback that is completed once the insert has completed
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoCommandException      returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
inline fun <reified T : Any> MongoCollection<T>.insertOne(
    document: String,
    options: InsertOneOptions,
    noinline callback: (Void?, Throwable?) -> Unit
) = withDocumentClass<BsonDocument>().insertOne(KMongoUtil.toBson(document, T::class), options, callback)


/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filter   the query filter to apply the the delete operation
 * @param callback the callback passed the result of the remove one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.deleteOne(filter: String, callback: (DeleteResult?, Throwable?) -> Unit) =
    deleteOne(KMongoUtil.toBson(filter), callback)

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filters   the query filters to apply the the delete operation
 * @param callback the callback passed the result of the remove one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.deleteOne(vararg filters: Bson?, callback: (DeleteResult?, Throwable?) -> Unit) =
    deleteOne(and(*filters), callback)

/**
 * Removes at most one document from the id parameter.  If no documents match, the collection is not
 * modified.
 *
 * @param id   the object id
 * @param callback the callback passed the result of the remove one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.deleteOneById(id: Any, callback: (DeleteResult?, Throwable?) -> Unit) =
    deleteOne(KMongoUtil.idFilterQuery(id), callback)

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filter   the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 * @param callback the callback passed the result of the remove many operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.deleteMany(
    filter: String,
    options: DeleteOptions = DeleteOptions(),
    callback: (DeleteResult?, Throwable?) -> Unit
) = deleteMany(KMongoUtil.toBson(filter), options, callback)

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filter   the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 * @param callback the callback passed the result of the remove many operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.deleteMany(
    vararg filters: Bson?,
    options: DeleteOptions = DeleteOptions(),
    callback: (DeleteResult?, Throwable?) -> Unit
) = deleteMany(and(*filters), options, callback)

/**
 * Save the document.
 * If the document has no id field, or if the document has a null id value, insert the document.
 * Otherwise, call [replaceOneById] with upsert true.
 *
 * @param document the document to save
 * @param callback the callback passed the result of the save operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T : Any> MongoCollection<T>.save(document: T, callback: (Void?, Throwable?) -> Unit) {
    val id = KMongoUtil.getIdValue(document)
    if (id != null) {
        replaceOneById(id, document, ReplaceOptions().upsert(true), { _, t -> callback.invoke(null, t) })
    } else {
        insertOne(document, callback)
    }
}

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param id          the object id
 * @param replacement the replacement document
 * @param callback    the callback passed the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T : Any> MongoCollection<T>.replaceOneById(
    id: Any,
    replacement: T,
    callback: (UpdateResult?, Throwable?) -> Unit
) = replaceOneById(id, replacement, ReplaceOptions(), callback)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param id          the object id
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 * @param callback    the callback passed the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T : Any> MongoCollection<T>.replaceOneById(
    id: Any,
    replacement: T,
    options: ReplaceOptions,
    callback: (UpdateResult?, Throwable?) -> Unit
) = withDocumentClass<BsonDocument>().replaceOne(
    KMongoUtil.idFilterQuery(id),
    KMongoUtil.filterIdToBson(replacement), options, callback
)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param replacement the replacement document - must have a non null id
 * @param callback    the callback passed the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
inline fun <reified T : Any> MongoCollection<T>.replaceOne(
    replacement: T,
    noinline callback: (UpdateResult?, Throwable?) -> Unit
) = replaceOneById(KMongoUtil.extractId(replacement, T::class), replacement, callback)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param callback    the callback passed the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
inline fun <reified T : Any> MongoCollection<T>.replaceOne(
    filter: Bson,
    replacement: T,
    noinline callback: (UpdateResult?, Throwable?) -> Unit
) = withDocumentClass<BsonDocument>().replaceOne(
    filter,
    KMongoUtil.filterIdToBson(replacement),
    callback
)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 * @param callback    the callback passed the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
inline fun <reified T : Any> MongoCollection<T>.replaceOne(
    filter: Bson,
    replacement: T,
    options: ReplaceOptions,
    noinline callback: (UpdateResult?, Throwable?) -> Unit
) = withDocumentClass<BsonDocument>().replaceOne(
    filter,
    KMongoUtil.filterIdToBson(replacement),
    options,
    callback
)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 * @param callback    the callback passed the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T : Any> MongoCollection<T>.replaceOne(
    filter: String,
    replacement: T,
    options: ReplaceOptions,
    callback: (UpdateResult?, Throwable?) -> Unit
) = withDocumentClass<BsonDocument>().replaceOne(
    KMongoUtil.toBson(filter),
    KMongoUtil.filterIdToBson(replacement),
    options,
    callback
)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param callback    the callback passed the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T : Any> MongoCollection<T>.replaceOne(
    filter: String,
    replacement: T,
    callback: (UpdateResult?, Throwable?) -> Unit
) = replaceOne(filter, replacement, ReplaceOptions(), callback)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the update operation
 * @param callback the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.updateOne(
    filter: String,
    update: String,
    options: UpdateOptions = UpdateOptions(),
    callback: (UpdateResult?, Throwable?) -> Unit
) = updateOne(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), options, callback)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param options  the options to apply to the update operation
 * @param callback the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.updateOne(
    filter: String,
    update: Any,
    options: UpdateOptions = UpdateOptions(),
    callback: (UpdateResult?, Throwable?) -> Unit
) = updateOne(KMongoUtil.toBson(filter), KMongoUtil.setModifier(update), options, callback)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param options  the options to apply to the update operation
 * @param callback  the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.updateOne(
    filter: Bson,
    target: Any,
    options: UpdateOptions = UpdateOptions(),
    callback: (UpdateResult?, Throwable?) -> Unit
) = updateOne(filter, KMongoUtil.toBsonModifier(target), options, callback)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 * @param callback  the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
inline fun <reified T : Any> MongoCollection<T>.updateOne(
    target: T,
    options: UpdateOptions = UpdateOptions(),
    noinline callback: (UpdateResult?, Throwable?) -> Unit
) {
    return updateOneById(KMongoUtil.extractId(target, T::class), target, options, callback)
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param id        the object id
 * @param update    the update object
 * @param options  the options to apply to the update operation
 * @param callback  the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.updateOneById(
    id: Any,
    update: Any,
    options: UpdateOptions = UpdateOptions(),
    callback: (UpdateResult?, Throwable?) -> Unit
) = updateOne(KMongoUtil.idFilterQuery(id), KMongoUtil.toBsonModifier(update), options, callback)

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param updateOptions  the options to apply to the update operation
 * @param callback the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T> MongoCollection<T>.updateMany(
    filter: String,
    update: String,
    updateOptions: UpdateOptions = UpdateOptions(),
    callback: (UpdateResult?, Throwable?) -> Unit
) = updateMany(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), updateOptions, callback)

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter        a document describing the query filter, which may not be null.
 * @param updates        a document describing the update, which may not be null. The update to apply must include only update operators.
 * @param updateOptions the options to apply to the update operation
 * @param callback the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
fun <T : Any> MongoCollection<T>.updateMany(
    filter: Bson,
    vararg updates: SetTo<*>,
    updateOptions: UpdateOptions = UpdateOptions(),
    callback: (UpdateResult?, Throwable?) -> Unit
) = updateMany(filter, set(*updates), updateOptions, callback)

/**
 * Atomically find a document and remove it.
 *
 * @param filter   the query filter to find the document with
 * @param options  the options to apply to the operation
 * @param callback the callback passed the document that was removed.  If no documents matched the query filter, then null will be returned
 */
fun <T> MongoCollection<T>.findOneAndDelete(
    filter: String,
    options: FindOneAndDeleteOptions = FindOneAndDeleteOptions(),
    callback: (T?, Throwable?) -> Unit
) = findOneAndDelete(KMongoUtil.toBson(filter), options, callback)

/**
 * Atomically find a document and replace it.
 *
 * @param filter      the query filter to apply the the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the operation
 *
 * @param callback    the callback passed the document that was replaced.  Depending on the value of the `returnDocument`
 *                    property, this will either be the document as it was before the update or as it is after the update.  If no
 *                    documents matched the query filter, then null will be returned
 */
fun <T> MongoCollection<T>.findOneAndReplace(
    filter: String,
    replacement: T,
    options: FindOneAndReplaceOptions = FindOneAndReplaceOptions(),
    callback: (T?, Throwable?) -> Unit
) = findOneAndReplace(KMongoUtil.toBson(filter), replacement, options, callback)

/**
 * Atomically find a document and update it.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the operation
 *
 * @param callback the callback passed the document that was updated.  Depending on the value of the `returnOriginal` property,
 *                 this will either be the document as it was before the update or as it is after the update.  If no documents matched
 *                  the query filter, then null will be returned
 */
fun <T> MongoCollection<T>.findOneAndUpdate(
    filter: String,
    update: String,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions(),
    callback: (T?, Throwable?) -> Unit
) = findOneAndUpdate(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), options, callback)

/**
 * Creates an index.  If successful, the callback will be executed with the name of the created index as the result.

 * @param key      an object describing the index key(s)
 * @param callback the callback that is completed once the index has been created
 */
fun <T> MongoCollection<T>.createIndex(key: String, callback: (String?, Throwable?) -> Unit) =
    createIndex(key, IndexOptions(), callback)

/**
 * Creates an index.  If successful, the callback will be executed with the name of the created index as the result.

 * @param key      an object describing the index key(s)
 * @param options  the options for the index
 * @param callback the callback that is completed once the index has been created
 */
fun <T> MongoCollection<T>.createIndex(key: String, options: IndexOptions, callback: (String?, Throwable?) -> Unit) =
    createIndex(KMongoUtil.toBson(key), options, callback)

/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * If successful, the callback will be executed with the name of the created index as the result.
 *
 * @param keys      an object describing the index key(s)
 * @param indexOptions  the options for the index
 * @param callback the callback that is completed once the index has been created
 */
fun <T> MongoCollection<T>.ensureIndex(
    keys: String,
    indexOptions: IndexOptions = IndexOptions(),
    callback: (String?, Throwable?) -> Unit = { _, _ -> }
) {
    createIndex(keys, indexOptions) { s, t ->
        if (t != null) {
            dropIndexOfKeys(keys) { _, t2 ->
                if (t2 != null) {
                    callback.invoke(null, t)
                } else {
                    createIndex(keys, indexOptions, callback)
                }
            }
        } else {
            callback.invoke(s, null)
        }
    }
}

/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * If successful, the callback will be executed with the name of the created index as the result.
 *
 * @param keys      an object describing the index key(s)
 * @param indexOptions  the options for the index
 * @param callback the callback that is completed once the index has been created
 */
fun <T> MongoCollection<T>.ensureIndex(
    keys: Bson,
    indexOptions: IndexOptions = IndexOptions(),
    callback: (String?, Throwable?) -> Unit = { _, _ -> }
) {
    createIndex(keys, indexOptions, { s, t ->
        if (t != null) {
            dropIndex(keys, { _, t2 ->
                if (t2 != null) {
                    callback.invoke(null, t)
                } else {
                    createIndex(keys, indexOptions, callback)
                }
            })
        } else {
            callback.invoke(s, null)
        }
    })
}

/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * If successful, the callback will be executed with the name of the created index as the result.
 *
 * @param properties      the properties, which must contain at least one
 * @param indexOptions  the options for the index
 * @param callback the callback that is completed once the index has been created
 */
fun <T> MongoCollection<T>.ensureIndex(
    vararg properties: KProperty<*>,
    indexOptions: IndexOptions = IndexOptions(),
    callback: (String?, Throwable?) -> Unit = { _, _ -> }
) = ensureIndex(ascending(*properties), indexOptions, callback)

/**
 * Create an [IndexOptions.unique] index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param properties the properties, which must contain at least one
 * @param indexOptions the options for the index
 * @param callback the callback that is completed once the index has been created
 */
fun <T> MongoCollection<T>.ensureUniqueIndex(
    vararg properties: KProperty<*>,
    indexOptions: IndexOptions = IndexOptions(),
    callback: (String?, Throwable?) -> Unit = { _, _ -> }
) = ensureIndex(properties = *properties, indexOptions = indexOptions.unique(true), callback = callback)


/**
 * Get all the indexes in this collection.

 * @param <TResult>   the target document type of the iterable.
 * @return the list indexes iterable interface
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified TResult : Any> MongoCollection<*>.listIndexes(): ListIndexesIterable<TResult> = listTypedIndexes()

/**
 * Get all the indexes in this collection.

 * @param <TResult>   the target document type of the iterable.
 * @return the list indexes iterable interface
 */
inline fun <reified TResult : Any> MongoCollection<*>.listTypedIndexes(): ListIndexesIterable<TResult> =
    listIndexes(TResult::class.java)

/**
 * Drops the index given the keys used to create it.

 * @param keys the keys of the index to remove
 * @param callback  the callback that is completed once the index has been dropped
 */
fun <T> MongoCollection<T>.dropIndex(keys: String, callback: (Void?, Throwable?) -> Unit) =
    dropIndexOfKeys(keys, callback)

/**
 * Drops the index given the keys used to create it.

 * @param keys the keys of the index to remove
 * @param callback  the callback that is completed once the index has been dropped
 */
fun <T> MongoCollection<T>.dropIndexOfKeys(keys: String, callback: (Void?, Throwable?) -> Unit) =
    dropIndex(KMongoUtil.toBson(keys), callback)

/**
 * Executes a mix of inserts, updates, replaces, and deletes.

 * @param requests the writes to execute
 * @param callback the callback passed the result of the bulk write
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    vararg requests: String,
    noinline callback: (BulkWriteResult?, Throwable?) -> Unit
) = withDocumentClass<BsonDocument>().bulkWrite(
    KMongoUtil.toWriteModel(requests, codecRegistry, T::class),
    BulkWriteOptions(),
    callback
)

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 * @param callback the callback passed the result of the bulk write
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    vararg requests: String,
    options: BulkWriteOptions = BulkWriteOptions(),
    noinline callback: (BulkWriteResult?, Throwable?) -> Unit
) = withDocumentClass<BsonDocument>().bulkWrite(
    KMongoUtil.toWriteModel(requests, codecRegistry, T::class),
    options,
    callback
)

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 * @param callback the callback passed the result of the bulk write
 *
 * @return the result of the bulk write
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    vararg requests: WriteModel<T>,
    options: BulkWriteOptions = BulkWriteOptions(),
    noinline callback: (BulkWriteResult?, Throwable?) -> Unit
) = bulkWrite(requests.toList(), options, callback)

