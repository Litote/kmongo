/*
 * Copyright (C) 2016 Litote
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

import com.mongodb.async.client.AggregateIterable
import com.mongodb.async.client.DistinctIterable
import com.mongodb.async.client.FindIterable
import com.mongodb.async.client.ListIndexesIterable
import com.mongodb.async.client.MapReduceIterable
import com.mongodb.async.client.MongoCollection
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.BulkWriteOptions
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.FindOneAndDeleteOptions
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil

/**
 * Create a new MongoCollection instance with a different default class to cast any documents returned from the database into..
 *
 * @param <NewTDocument> the default class to cast any documents returned from the database into.
 * @return a new MongoCollection instance with the different default class
 */
inline fun <reified NewTDocument : Any> MongoCollection<*>.withDocumentClass(): MongoCollection<NewTDocument>
        = withDocumentClass(NewTDocument::class.java)

/**
 * Counts the number of documents
 *
 * @return count of all collection
 */
suspend fun <T> MongoCollection<T>.count(): Long {
    return singleResult { count(it) } ?: 0L
}

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @return count of filtered collection
 */
suspend fun <T> MongoCollection<T>.count(filter: String): Long {
    return count(filter, CountOptions())
}

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @return count of filtered collection
 */
suspend fun <T> MongoCollection<T>.count(filter: String, options: CountOptions): Long {
    return singleResult { count(KMongoUtil.toBson(filter), options, it) } ?: 0L
}

/**
 * Gets the distinct values of the specified field name.
 *
 * @param fieldName   the field name
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(fieldName: String): DistinctIterable<TResult>
        = distinct(fieldName, KMongoUtil.EMPTY_JSON)

/**
 * Gets the distinct values of the specified field name.
 *
 * @param fieldName   the field name
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(fieldName: String, filter: String): DistinctIterable<TResult>
        = distinct(fieldName, KMongoUtil.toBson(filter), TResult::class.java)

/**
 * Finds all documents that match the filter in the collection.
 *
 * @param  filter the query filter
 * @return the find iterable interface
 */
fun <T : Any> MongoCollection<T>.find(filter: String): FindIterable<T>
        = find(KMongoUtil.toBson(filter))

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 */
suspend fun <T : Any> MongoCollection<T>.findOne(filter: String = KMongoUtil.EMPTY_JSON): T? {
    return singleResult { find(filter).first(it) }
}

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 */
suspend fun <T : Any> MongoCollection<T>.findOne(filter: Bson): T? {
    return singleResult { find(filter).first(it) }
}

/**
 * Finds the document that match the [ObjectId] parameter.
 *
 * @param id       the object id
 */
suspend fun <T : Any> MongoCollection<T>.findOneById(id: Any): T? {
    return findOne(KMongoUtil.idFilterQuery(id))
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
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: String): AggregateIterable<TResult>
        = aggregate(KMongoUtil.toBsonList(pipeline, codecRegistry), TResult::class.java)

/**
 * Aggregates documents according to the specified aggregation pipeline.  If the pipeline ends with a $out stage, the returned
 * iterable will be a query of the collection that the aggregation was written to.  Note that in this case the pipeline will be
 * executed even if the iterable is never iterated.
 *
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: Bson): AggregateIterable<TResult>
        = aggregate(pipeline.toList(), TResult::class.java)

/**
 * Aggregates documents according to the specified map-reduce function.
 *
 * @param mapFunction    a JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
 * @param reduceFunction a JavaScript function that "reduces" to a single object all the values associated with a particular key.
 * @param <TResult>      the target document type of the iterable.
 * *
 * @return an iterable containing the result of the map-reduce operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.mapReduceTyped(mapFunction: String, reduceFunction: String): MapReduceIterable<TResult>
        = mapReduce(mapFunction, reduceFunction, TResult::class.java)

/**
 * Inserts one or more documents.  A call to this method is equivalent to a call to the {@code bulkWrite} method
 *
 * @param documents the documents to insert
 * @param options   the options to apply to the operation
 * @throws com.mongodb.MongoBulkWriteException if there's an exception in the bulk write operation
 * @throws com.mongodb.MongoException          if the write failed due some other failure
 * @see com.mongodb.async.client.MongoCollection#bulkWrite
 */
suspend fun <T : Any> MongoCollection<T>.insertMany(documents: List<T>, options: InsertManyOptions): Void? {
    return singleResult { insertMany(documents, options, it) }
}

/**
 * Inserts one or more documents.  A call to this method is equivalent to a call to the {@code bulkWrite} method
 *
 * @param documents the documents to insert
 * @throws com.mongodb.MongoBulkWriteException if there's an exception in the bulk write operation
 * @throws com.mongodb.MongoException          if the write failed due some other failure
 * @see com.mongodb.async.client.MongoCollection#bulkWrite
 */
suspend fun <T : Any> MongoCollection<T>.insertMany(documents: List<T>): Void? {
    return insertMany(documents, InsertManyOptions())
}

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.

 * @param document the document to insert
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <TDocument : Any> MongoCollection<TDocument>.insertOne(document: TDocument): Void? {
    return insertOne(document, InsertOneOptions())
}

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.

 * @param document the document to insert
 * @param options  the options to apply to the operation
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoCommandException      returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <TDocument : Any> MongoCollection<TDocument>.insertOne(document: TDocument, options: InsertOneOptions): Void? {
    return singleResult { insertOne(document, options, it) }
}

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoCommandException
 * @throws com.mongodb.MongoException
 */
suspend inline fun <reified T : Any> MongoCollection<T>.insertOne(document: String): Void?
        = insertOne(document, InsertOneOptions())

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param options  the options to apply to the operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoCommandException      returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend inline fun <reified T : Any> MongoCollection<T>.insertOne(document: String, options: InsertOneOptions): Void? {
    return singleResult { withDocumentClass<BsonDocument>().insertOne(KMongoUtil.toBson(document, T::class), options, it) }
}


/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filter   the query filter to apply the the delete operation
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoException
 */
suspend fun <T> MongoCollection<T>.deleteOne(filter: String): DeleteResult? {
    return singleResult { deleteOne(KMongoUtil.toBson(filter), it) }
}

/**
 * Removes at most one document from the [ObjectId] parameter.  If no documents match, the collection is not
 * modified.
 *
 * @param id   the object id
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoException
 */
suspend fun <T> MongoCollection<T>.deleteOneById(id: Any): DeleteResult? {
    return deleteOne(KMongoUtil.idFilterQuery(id))
}

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filter   the query filter to apply the the delete operation
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoException
 */
suspend fun <T> MongoCollection<T>.deleteMany(filter: String): DeleteResult? {
    return singleResult { deleteMany(KMongoUtil.toBson(filter), it) }
}


/**
 * Save the document.
 * If the document has no id field, or if the document has a null id value, insert the document.
 * Otherwise, call [replaceOneById] with upsert true.
 *
 * @param document the document to save
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <T : Any> MongoCollection<T>.save(document: T): Void? {
    val id = KMongoUtil.getIdValue(document)
    return if (id != null) {
        replaceOneById(id, document, UpdateOptions().upsert(true))
        null
    } else {
        singleResult<Void> { insertOne(document, it) }
    }
}

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param id          the object id
 * @param replacement the replacement document
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <T : Any> MongoCollection<T>.replaceOneById(id: Any, replacement: T): UpdateResult? {
    return replaceOneById(id, replacement, UpdateOptions())
}


/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param id          the object id
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <T : Any> MongoCollection<T>.replaceOneById(id: Any, replacement: T, options: UpdateOptions): UpdateResult? {
    return replaceOne(KMongoUtil.idFilterQuery(id), replacement, options)
}

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param replacement the document to replace - must have an non null id
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend inline fun <reified T : Any> MongoCollection<T>.replaceOne(replacement: T): UpdateResult? {
    return replaceOneById(KMongoUtil.extractId(replacement, T::class), replacement)
}

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
suspend fun <T : Any> MongoCollection<T>.replaceOne(filter: String, replacement: T, options: UpdateOptions): UpdateResult? {
    return singleResult { withDocumentClass<BsonDocument>().replaceOne(KMongoUtil.toBson(filter), KMongoUtil.filterIdToBson(replacement), options, it) }
}

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
suspend fun <T : Any> MongoCollection<T>.replaceOne(filter: String, replacement: T): UpdateResult? {
    return replaceOne(filter, replacement, UpdateOptions())
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param callback the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <T> MongoCollection<T>.updateOne(filter: String, update: String): UpdateResult? {
    return updateOne(filter, update, UpdateOptions())
}

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
suspend fun <T> MongoCollection<T>.updateOne(filter: String, update: String, options: UpdateOptions): UpdateResult? {
    return singleResult { updateOne(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), options, it) }
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param callback the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <T> MongoCollection<T>.updateOne(filter: String, update: Any): UpdateResult? {
    return updateOne(filter, KMongoUtil.setModifier(update), UpdateOptions())
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param target  the update object - must have an non null id
 * @param callback  the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend inline fun <reified T : Any> MongoCollection<T>.updateOne(target: T): UpdateResult? {
    return updateOneById(KMongoUtil.extractId(target, T::class), target)
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param id        the object id
 * @param update    a document describing the update. The update to apply must include only update operators.
 * @param callback  the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <T> MongoCollection<T>.updateOneById(id: Any, update: String): UpdateResult?
        = updateOneById(id, update, UpdateOptions())

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param id        the object id
 * @param update    a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the update operation
 * @param callback  the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <T> MongoCollection<T>.updateOneById(id: Any, update: String, options: UpdateOptions): UpdateResult?
        = updateOne(KMongoUtil.idFilterQuery(id), update, options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param id        the object id
 * @param update    the update object
 * @param callback  the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <T> MongoCollection<T>.updateOneById(id: Any, update: Any): UpdateResult?
        = updateOneById(id, update, UpdateOptions())

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
suspend fun <T> MongoCollection<T>.updateOneById(id: Any, update: Any, options: UpdateOptions): UpdateResult? {
    //strange but update can be a String
    return if (update is String) {
        updateOneById(KMongoUtil.idFilterQuery(id), update, options)
    } else {
        updateOne(KMongoUtil.idFilterQuery(id), KMongoUtil.setModifier(update), options)
    }
}

/**
 * Update all documents in the collection according to the specified arguments.
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
suspend fun <T> MongoCollection<T>.updateMany(filter: String, update: String, options: UpdateOptions): UpdateResult? {
    return singleResult { updateMany(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), options, it) }
}

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param callback the callback passed the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        returned via the callback
 * @throws com.mongodb.MongoWriteConcernException returned via the callback
 * @throws com.mongodb.MongoException             returned via the callback
 */
suspend fun <T> MongoCollection<T>.updateMany(filter: String, update: String): UpdateResult? {
    return updateMany(filter, update, UpdateOptions())
}

/**
 * Atomically find a document and remove it.
 *
 * @param filter   the query filter to find the document with
 * @param options  the options to apply to the operation
 * @param callback the callback passed the document that was removed.  If no documents matched the query filter, then null will be returned
 */
suspend fun <T : Any> MongoCollection<T>.findOneAndDelete(filter: String, options: FindOneAndDeleteOptions): T? {
    return singleResult { findOneAndDelete(KMongoUtil.toBson(filter), options, it) }
}

/**
 * Atomically find a document and remove it.
 *
 * @param filter   the query filter to find the document with
 * @param callback the callback passed the document that was removed.  If no documents matched the query filter, then null will be returned
 */
suspend fun <T : Any> MongoCollection<T>.findOneAndDelete(filter: String): T? {
    return findOneAndDelete(filter, FindOneAndDeleteOptions())
}

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
suspend fun <T> MongoCollection<T>.findOneAndReplace(filter: String, replacement: T, options: FindOneAndReplaceOptions): T? {
    return singleResult { findOneAndReplace(KMongoUtil.toBson(filter), replacement, options, it) }
}

/**
 * Atomically find a document and replace it.
 *
 * @param filter      the query filter to apply the the replace operation
 * @param replacement the replacement document
 *
 * @param callback    the callback passed the document that was replaced.  Depending on the value of the `returnDocument`
 *                    property, this will either be the document as it was before the update or as it is after the update.  If no
 *                    documents matched the query filter, then null will be returned
 */
suspend fun <T : Any> MongoCollection<T>.findOneAndReplace(filter: String, replacement: T): T? {
    return findOneAndReplace(filter, replacement, FindOneAndReplaceOptions())
}

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
suspend fun <T : Any> MongoCollection<T>.findOneAndUpdate(filter: String, update: String, options: FindOneAndUpdateOptions): T? {
    return singleResult { findOneAndUpdate(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), options, it) }
}

/**
 * Atomically find a document and update it.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 *
 * @param callback the callback passed the document that was updated.  Depending on the value of the `returnOriginal` property,
 *                 this will either be the document as it was before the update or as it is after the update.  If no documents matched
 *                  the query filter, then null will be returned
 */
suspend fun <T : Any> MongoCollection<T>.findOneAndUpdate(filter: String, update: String): T? {
    return findOneAndUpdate(filter, update, FindOneAndUpdateOptions())
}

/**
 * Creates an index.  If successful, the callback will be executed with the name of the created index as the result.

 * @param key      an object describing the index key(s)
 * @param callback the callback that is completed once the index has been created
 */
suspend fun <T> MongoCollection<T>.createIndex(key: String): String? {
    return createIndex(key, IndexOptions())
}

/**
 * Creates an index.  If successful, the callback will be executed with the name of the created index as the result.

 * @param key      an object describing the index key(s)
 * @param options  the options for the index
 * @param callback the callback that is completed once the index has been created
 */
suspend fun <T> MongoCollection<T>.createIndex(key: String, options: IndexOptions): String? {
    return singleResult { createIndex(KMongoUtil.toBson(key), options, it) }
}


/**
 * Get all the indexes in this collection.

 * @param <TResult>   the target document type of the iterable.
 * @return the list indexes iterable interface
 */
inline fun <reified TResult : Any> MongoCollection<*>.listIndexesTyped(): ListIndexesIterable<TResult>
        = listIndexes(TResult::class.java)

/**
 * Drops the index given the keys used to create it.

 * @param keys the keys of the index to remove
 * @param callback  the callback that is completed once the index has been dropped
 */
suspend fun <T> MongoCollection<T>.dropIndex(keys: String): Void? {
    return singleResult { dropIndex(KMongoUtil.toBson(keys), it) }
}

/**
 * Executes a mix of inserts, updates, replaces, and deletes.

 * @param requests the writes to execute
 * @param callback the callback passed the result of the bulk write
 */
suspend inline fun <reified T : Any> MongoCollection<T>.bulkWrite(vararg requests: String): BulkWriteResult? {
    return singleResult { withDocumentClass<BsonDocument>().bulkWrite(KMongoUtil.toWriteModel(requests, codecRegistry, T::class), BulkWriteOptions(), it) }
}

/**
 * Executes a mix of inserts, updates, replaces, and deletes.

 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 * @param callback the callback passed the result of the bulk write
 */
suspend inline fun <reified T : Any> MongoCollection<T>.bulkWrite(vararg requests: String, options: BulkWriteOptions): BulkWriteResult? {
    return singleResult { withDocumentClass<BsonDocument>().bulkWrite(KMongoUtil.toWriteModel(requests, codecRegistry, T::class), options, it) }
}
