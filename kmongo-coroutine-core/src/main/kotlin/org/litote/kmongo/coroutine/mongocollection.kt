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

import com.mongodb.MongoCommandException
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
import com.mongodb.client.model.InsertManyOptions
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
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import org.litote.kmongo.util.KMongoUtil.setModifier
import org.litote.kmongo.util.KMongoUtil.toBson
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Create a new MongoCollection instance with a different default class to cast any documents returned from the database into..
 *
 * @param <NewTDocument> the default class to cast any documents returned from the database into.
 * @return a new MongoCollection instance with the different default class
 */
@Deprecated("use same function with org.litote.kmongo.async package - will be removed in 4.0")
inline fun <reified NewTDocument : Any> MongoCollection<*>.withDocumentClass(): MongoCollection<NewTDocument> =
    withDocumentClass(NewTDocument::class.java)

/**
 * Counts the number of documents
 *
 * @return count of all collection
 */
@Deprecated("use countDocuments instead")
suspend fun <T> MongoCollection<T>.count(): Long {
    return singleResult { count(it) } ?: 0L
}

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @return count of filtered collection
 */
@Deprecated("use countDocuments instead")
suspend fun <T> MongoCollection<T>.count(filter: String, options: CountOptions = CountOptions()): Long {
    return singleResult { count(toBson(filter), options, it) } ?: 0L
}

/**
 * Counts the number of documents
 *
 * @return count of all collection
 */
suspend fun <T> MongoCollection<T>.countDocuments(): Long {
    return singleResult { countDocuments(it) } ?: 0L
}

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @return count of filtered collection
 */
suspend fun <T> MongoCollection<T>.countDocuments(filter: String, options: CountOptions = CountOptions()): Long {
    return singleResult { countDocuments(toBson(filter), options, it) } ?: 0L
}

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
): DistinctIterable<TResult> = distinct(fieldName, toBson(filter), TResult::class.java)

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
): DistinctIterable<TResult> = distinct(field.path(), filter, TResult::class.java)

/**
 * Finds all documents that match the filter in the collection.
 *
 * @param  filter the query filter
 * @return the find iterable interface
 */
fun <T : Any> MongoCollection<T>.find(filter: String): FindIterable<T> = find(toBson(filter))

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
 * Finds the first document that match the filters in the collection.
 *
 * @param filters the query filters
 * @return the first item returned or null
 */
suspend fun <T> MongoCollection<T>.findOne(vararg filters: Bson?): T? =
    find(*filters).first()

/**
 * Finds the document that match the id parameter.
 *
 * @param id       the object id
 */
suspend fun <T : Any> MongoCollection<T>.findOneById(id: Any): T? {
    return findOne(idFilterQuery(id))
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
inline fun <reified TResult : Any> MongoCollection<*>.mapReduceTyped(
    mapFunction: String,
    reduceFunction: String
): MapReduceIterable<TResult> = mapReduce(mapFunction, reduceFunction, TResult::class.java)

/**
 * Inserts one or more documents.  A call to this method is equivalent to a call to the {@code bulkWrite} method
 *
 * @param documents the documents to insert
 * @param options   the options to apply to the operation
 * @throws com.mongodb.MongoBulkWriteException if there's an exception in the bulk write operation
 * @throws com.mongodb.MongoException          if the write failed due some other failure
 * @see com.mongodb.async.client.MongoCollection#bulkWrite
 */
suspend fun <T : Any> MongoCollection<T>.insertMany(
    documents: List<T>,
    options: InsertManyOptions = InsertManyOptions()
): Void? {
    return singleResult { insertMany(documents, options, it) }
}

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param options  the options to apply to the operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the insert command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoCommandException      if the write failed due to document validation reasons
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <TDocument : Any> MongoCollection<TDocument>.insertOne(
    document: TDocument,
    options: InsertOneOptions = InsertOneOptions()
): Void? {
    return singleResult { insertOne(document, options, it) }
}

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param options  the options to apply to the operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the insert command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoCommandException      if the write failed due to document validation reasons
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend inline fun <reified T : Any> MongoCollection<T>.insertOne(
    document: String,
    options: InsertOneOptions = InsertOneOptions()
): Void? {
    return singleResult {
        withDocumentClass<BsonDocument>().insertOne(
            toBson(document, T::class),
            options,
            it
        )
    }
}

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filter   the query filter to apply the the delete operation
 *
 * @return the result of the remove one operation
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoException
 */
suspend fun <T> MongoCollection<T>.deleteOne(
    filter: String,
    deleteOptions: DeleteOptions = DeleteOptions()
): DeleteResult? = singleResult { deleteOne(toBson(filter), deleteOptions, it) }

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filters the query filters to apply the the delete operation
 *
 * @return the result of the remove one operation
 *
 * @throws com.mongodb.MongoWriteException       if the write failed due some other failure specific to the delete command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <T> MongoCollection<T>.deleteOne(
    vararg filters: Bson?,
    deleteOptions: DeleteOptions = DeleteOptions()
): DeleteResult? = singleResult { deleteOne(and(*filters), deleteOptions, it) }

/**
 * Removes at most one document from the id parameter.  If no documents match, the collection is not
 * modified.
 *
 * @param id   the object id
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoException
 */
suspend fun <T> MongoCollection<T>.deleteOneById(id: Any): DeleteResult? {
    return deleteOne(idFilterQuery(id))
}

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filter   the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 *
 * @return the result of the remove many operation
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoException
 */
suspend fun <T> MongoCollection<T>.deleteMany(
    filter: String,
    options: DeleteOptions = DeleteOptions()
): DeleteResult? {
    return singleResult { deleteMany(toBson(filter), options, it) }
}

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filters   the query filters to apply the the delete operation
 * @param options  the options to apply to the delete operation
 *
 * @return the result of the remove many operation
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoException
 */
suspend fun <T> MongoCollection<T>.deleteMany(
    vararg filters: Bson?,
    options: DeleteOptions = DeleteOptions()
): DeleteResult? = singleResult { deleteMany(and(*filters), options, it) }

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
        replaceOneById(id, document, ReplaceOptions().upsert(true))
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
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <T : Any> MongoCollection<T>.replaceOneById(
    id: Any,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult? {
    return replaceOne(idFilterQuery(id), replacement, options)
}

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param replacement the document to replace - must have an non null id
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend inline fun <reified T : Any> MongoCollection<T>.replaceOne(
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult? {
    return replaceOneById(KMongoUtil.extractId(replacement, T::class), replacement, options)
}

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <T : Any> MongoCollection<T>.replaceOne(
    filter: String,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult? {
    return replaceOne(toBson(filter), replacement, options)
}

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <T : Any> MongoCollection<T>.replaceOne(
    filter: Bson,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): UpdateResult? {
    return singleResult {
        withDocumentClass<BsonDocument>().replaceOne(
            filter,
            KMongoUtil.filterIdToBson(replacement),
            options,
            it
        )
    }
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
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
suspend fun <T> MongoCollection<T>.updateOne(
    filter: String,
    update: String,
    options: UpdateOptions = UpdateOptions()
): UpdateResult? {
    return singleResult { updateOne(toBson(filter), toBson(update), options, it) }
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <T> MongoCollection<T>.updateOne(
    filter: String,
    update: Any,
    options: UpdateOptions = UpdateOptions()
): UpdateResult? = singleResult { updateOne(toBson(filter), setModifier(update), options, it) }

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend inline fun <reified T : Any> MongoCollection<T>.updateOne(
    filter: Bson,
    target: T,
    options: UpdateOptions = UpdateOptions()
): UpdateResult? {
    return singleResult { updateOne(filter, KMongoUtil.toBsonModifier(target), options, it) }
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend inline fun <reified T : Any> MongoCollection<T>.updateOne(
    target: T,
    options: UpdateOptions = UpdateOptions()
): UpdateResult? {
    return updateOneById(KMongoUtil.extractId(target, T::class), target, options)
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param id        the object id
 * @param update    the update object
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <T> MongoCollection<T>.updateOneById(
    id: Any,
    update: Any,
    options: UpdateOptions = UpdateOptions()
): UpdateResult? =
    singleResult {
        updateOne(
            idFilterQuery(id),
            KMongoUtil.toBsonModifier(update),
            options,
            it
        )
    }

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param updateOptions the options to apply to the update operation
 *
 * @return the result of the update many operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <T> MongoCollection<T>.updateMany(
    filter: String,
    update: String,
    updateOptions: UpdateOptions = UpdateOptions()
): UpdateResult? {
    return singleResult { updateMany(toBson(filter), toBson(update), updateOptions, it) }
}

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param updateOptions the options to apply to the update operation
 *
 * @return the result of the update many operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
suspend fun <T> MongoCollection<T>.updateMany(
    filter: Bson,
    vararg updates: SetTo<*>,
    updateOptions: UpdateOptions = UpdateOptions()
): UpdateResult? = singleResult { updateMany(filter, set(*updates), updateOptions, it) }

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
): T? {
    return singleResult { findOneAndDelete(toBson(filter), options, it) }
}

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
): T? {
    return singleResult { findOneAndReplace(toBson(filter), replacement, options, it) }
}

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
): T? {
    return singleResult { findOneAndUpdate(toBson(filter), toBson(update), options, it) }
}

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
): String? {
    return singleResult { createIndex(toBson(key), options, it) }
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
        singleResult { createIndex(keys, indexOptions, it) }
    } catch (e: MongoCommandException) {
        //there is an exception if the parameters of an existing index are changed.
        //then drop the index and create a new one
        singleResult<Void> { dropIndex(keys, it) }
        singleResult { createIndex(keys, indexOptions, it) }
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
inline fun <reified TResult : Any> MongoCollection<*>.listTypedIndexes(): ListIndexesIterable<TResult> =
    listIndexes(TResult::class.java)

/**
 * Drops the index given the keys used to create it.
 *
 * @param keys the keys of the index to remove
 */
suspend fun <T> MongoCollection<T>.dropIndex(keys: String): Void? {
    return singleResult { dropIndex(toBson(keys), it) }
}

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 *
 * @return the result of the bulk write
 */
suspend inline fun <reified T : Any> MongoCollection<T>.bulkWrite(vararg requests: String): BulkWriteResult? {
    return singleResult {
        withDocumentClass<BsonDocument>().bulkWrite(
            KMongoUtil.toWriteModel(
                requests,
                codecRegistry,
                T::class
            ), BulkWriteOptions(), it
        )
    }
}

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
): BulkWriteResult? {
    return singleResult {
        withDocumentClass<BsonDocument>().bulkWrite(
            KMongoUtil.toWriteModel(
                requests,
                codecRegistry,
                T::class
            ), options, it
        )
    }
}

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
): BulkWriteResult? {
    return singleResult { bulkWrite(requests.toList(), options, it) }
}