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
package org.litote.kmongo

import com.mongodb.ReadPreference
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.AggregateIterable
import com.mongodb.client.DistinctIterable
import com.mongodb.client.FindIterable
import com.mongodb.client.ListIndexesIterable
import com.mongodb.client.MapReduceIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.BulkWriteOptions
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.FindOneAndDeleteOptions
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.EMPTY_JSON
import org.litote.kmongo.util.KMongoUtil.defaultCollectionName
import org.litote.kmongo.util.KMongoUtil.extractId
import org.litote.kmongo.util.KMongoUtil.filterIdToBson
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import org.litote.kmongo.util.KMongoUtil.setModifier
import org.litote.kmongo.util.KMongoUtil.toBson
import org.litote.kmongo.util.KMongoUtil.toBsonList
import org.litote.kmongo.util.KMongoUtil.toExtendedJson
import org.litote.kmongo.util.KMongoUtil.toWriteModel

//*******
//MongoDatabase extension methods
//*******

/**
 * Gets a collection.
 *
 * @param collectionName the name of the collection to return
 * @param <T>            the default target type of the collection to return
 * @return the collection
 */
inline fun <reified T : Any> MongoDatabase.getCollection(collectionName: String): MongoCollection<T>
        = getCollection(collectionName, T::class.java)

/**
 * Gets a collection.
 *
 * @param <T>            the default target type of the collection to return
 *                       - the name of the collection is determined by [defaultCollectionName]
 * @return the collection
 * @see defaultCollectionName
 */
inline fun <reified T : Any> MongoDatabase.getCollection(): MongoCollection<T>
        = getCollection(defaultCollectionName(T::class), T::class.java)

/**
 * Executes the given command in the context of the current database with the given read preference.
 *
 * @param command        the command to be run
 * @param readPreference the {@link com.mongodb.ReadPreference} to be used when executing the command
 * @param <TResult>      the type of the class to use instead of {@code Document}.
 */
inline fun <reified TResult : Any> MongoDatabase.runCommand(command: String, readPreference: ReadPreference): TResult
        = runCommand(toBson(command), readPreference, TResult::class.java)

/**
 * Executes the given command in the context of the current database with the given read preference.
 *
 * @param command        the command to be run
 * @param <TResult>      the type of the class to use instead of {@code Document}.
 */
inline fun <reified TResult : Any> MongoDatabase.runCommand(command: String): TResult
        = runCommand(command, readPreference)


//*******
//MongoCollection extension methods
//*******

/**
 * Create a new MongoCollection instance with a different default class to cast any documents returned from the database into..
 *
 * @param <NewTDocument> the default class to cast any documents returned from the database into.
 * @return a new MongoCollection instance with the different default class
 */
inline fun <reified NewTDocument : Any> MongoCollection<*>.withDocumentClass(): MongoCollection<NewTDocument>
        = withDocumentClass(NewTDocument::class.java)

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter  the query filter
 * @param options the options describing the count
 *
 * @return the number of documents in the collection
 */
fun <T> MongoCollection<T>.count(filter: String, options: CountOptions = CountOptions()): Long
        = count(toBson(filter), options)

/**
 * Gets the distinct values of the specified field name.
 *
 * @param fieldName   the field name
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable.
 *
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(fieldName: String, filter: String = EMPTY_JSON): DistinctIterable<TResult>
        = distinct(fieldName, toBson(filter), TResult::class.java)

/**
 * Finds all documents in the collection.
 *
 * @param filter the query filter
 * @return the find iterable interface
 */
fun <T> MongoCollection<T>.find(filter: String = EMPTY_JSON): FindIterable<T>
        = find(toBson(filter))


/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 * @return the first item returned or null
 */
fun <T> MongoCollection<T>.findOne(filter: String = EMPTY_JSON): T?
        = find(filter).first()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 * @return the first item returned or null
 */
fun <T> MongoCollection<T>.findOne(filter: Bson): T?
        = find(filter).first()

/**
 * Finds the document that match the [ObjectId] parameter.
 *
 * @param id       the object id
 * @return the first item returned or null
 */
fun <T> MongoCollection<T>.findOneById(id: Any): T?
        = findOne(idFilterQuery(id))

/**
 * Aggregates documents according to the specified aggregation pipeline.
 *
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable.
 *
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: String): AggregateIterable<TResult>
        = aggregate(toBsonList(pipeline, codecRegistry), TResult::class.java)

/**
 * Aggregates documents according to the specified aggregation pipeline.
 *
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable.
 *
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: Bson): AggregateIterable<TResult>
        = aggregate(pipeline.toList(), TResult::class.java)

/**
 * Aggregates documents according to the specified map-reduce function.
 *
 * @param mapFunction    A JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
 * @param reduceFunction A JavaScript function that "reduces" to a single object all the values associated with a particular key.
 * @param <TResult>      the target document type of the iterable.
 *
 *  @return an iterable containing the result of the map-reduce operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.mapReduce(mapFunction: String, reduceFunction: String): MapReduceIterable<TResult>
        = mapReduce(mapFunction, reduceFunction, TResult::class.java)

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
inline fun <reified T : Any> MongoCollection<T>.insertOne(document: String, options: InsertOneOptions = InsertOneOptions())
        = withDocumentClass<BsonDocument>().insertOne(toBson(document, T::class), options)

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filter the query filter to apply the the delete operation
 *
 * @return the result of the remove one operation
 *
 *  @throws com.mongodb.MongoWriteException       if the write failed due some other failure specific to the delete command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.deleteOne(filter: String): DeleteResult
        = deleteOne(toBson(filter))

/**
 * Removes at most one document from the [ObjectId] parameter.  If no documents match, the collection is not
 * modified.
 *
 * @param id   the object id
 *
 *  @throws com.mongodb.MongoWriteException       if the write failed due some other failure specific to the delete command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.deleteOneById(id: Any): DeleteResult
        = deleteOne(idFilterQuery(id))

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filter the query filter to apply the the delete operation
 *
 * @return the result of the remove many operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the delete command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.deleteMany(filter: String): DeleteResult
        = deleteMany(toBson(filter))

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
fun <T : Any> MongoCollection<T>.save(document: T) {
    val id = KMongoUtil.getIdValue(document)
    if (id != null) {
        replaceOneById(id, document, UpdateOptions().upsert(true))
    } else {
        insertOne(document)
    }
}


/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param id          the object id
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T : Any> MongoCollection<T>.replaceOneById(id: Any, replacement: T, options: UpdateOptions = UpdateOptions()): UpdateResult
        = replaceOne(idFilterQuery(id), replacement, options)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param replacement the document to replace - must have an non null id
 * @param options     the options to apply to the replace operation
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
inline fun <reified T : Any> MongoCollection<T>.replaceOne(replacement: T, options: UpdateOptions = UpdateOptions()): UpdateResult
        = replaceOneById(extractId(replacement, T::class), replacement, options)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 * @return the result of the replace one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T : Any> MongoCollection<T>.replaceOne(filter: String, replacement: T, options: UpdateOptions = UpdateOptions()): UpdateResult
        = withDocumentClass<BsonDocument>().replaceOne(toBson(filter), filterIdToBson(replacement), options)

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
fun <T> MongoCollection<T>.updateOne(filter: String, update: String, options: UpdateOptions = UpdateOptions()): UpdateResult
        = updateOne(toBson(filter), toBson(update), options)

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
fun <T> MongoCollection<T>.updateOne(filter: String, update: Any, options: UpdateOptions = UpdateOptions()): UpdateResult
        = updateOne(filter, setModifier(update), options)

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
inline fun <reified T : Any> MongoCollection<T>.updateOne(target: T, options: UpdateOptions = UpdateOptions()): UpdateResult
        = updateOneById(extractId(target, T::class), target, options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param id        the object id
 * @param update    a document describing the update. The update to apply must include only update operators.
 *
 * @return the result of the update one operation
 *
 * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to the update command
 * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil the write concern
 * @throws com.mongodb.MongoException             if the write failed due some other failure
 */
fun <T> MongoCollection<T>.updateOneById(id: Any, update: String, options: UpdateOptions = UpdateOptions()): UpdateResult
        = updateOne(idFilterQuery(id), update, options)

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
fun <T> MongoCollection<T>.updateOneById(id: Any, update: Any, options: UpdateOptions = UpdateOptions()): UpdateResult
        =
        //strange but update can be a String
        if (update is String) {
            updateOneById(idFilterQuery(id), update, options)
        } else {
            updateOne(idFilterQuery(id), setModifier(update), options)
        }

/**
 * Update all documents in the collection according to the specified arguments.
 *
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
fun <T : Any> MongoCollection<T>.updateMany(filter: String, update: String, updateOptions: UpdateOptions = UpdateOptions()): UpdateResult
        = updateMany(toBson(filter), toBson(update), updateOptions)

/**
 * Atomically find a document and remove it.

 * @param filter  the query filter to find the document with
 * @param options the options to apply to the operation
 *
 * @return the document that was removed.  If no documents matched the query filter, then null will be returned
 */
fun <T> MongoCollection<T>.findOneAndDelete(filter: String, options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()): T
        = findOneAndDelete(toBson(filter), options)

/**
 * Atomically find a document and replace it.

 * @param filter      the query filter to apply the the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the operation
 *
 * @return the document that was replaced.  Depending on the value of the `returnOriginal` property, this will either be the
 * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
 * returned
 */
fun <T> MongoCollection<T>.findOneAndReplace(filter: String, replacement: T, options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()): T
        = findOneAndReplace(toBson(filter), replacement, options)

/**
 * Atomically find a document and update it.

 * @param filter  a document describing the query filter, which may not be null.
 * @param update  a document describing the update, which may not be null. The update to apply must include only update operators.
 * @param options the options to apply to the operation
 *
 * @return the document that was updated.  Depending on the value of the `returnOriginal` property, this will either be the
 * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
 * returned
 */
fun <T> MongoCollection<T>.findOneAndUpdate(filter: String, update: String, options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()): T
        = findOneAndUpdate(toBson(filter), toBson(update), options)

/**
 * Create an index with the given keys and options.

 * @param keys                an object describing the index key(s), which may not be null.
 * @param indexOptions the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.createIndex(keys: String, indexOptions: IndexOptions = IndexOptions()): String
        = createIndex(toBson(keys), indexOptions)

/**
 * Get all the indexes in this collection.

 * @param <TResult>   the target document type of the iterable.
 * @return the list indexes iterable interface
 */
inline fun <reified TResult : Any> MongoCollection<*>.listIndexes(): ListIndexesIterable<TResult>
        = listIndexes(TResult::class.java)


/**
 * Drops the index given the keys used to create it.

 * @param keys the keys of the index to remove
 */
fun <T> MongoCollection<T>.dropIndex(keys: String)
        = dropIndex(toBson(keys))

/**
 * Executes a mix of inserts, updates, replaces, and deletes.

 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(vararg requests: String, options: BulkWriteOptions = BulkWriteOptions()): BulkWriteResult
        = withDocumentClass<BsonDocument>().bulkWrite(toWriteModel(requests, codecRegistry, T::class), options)


//*******
//IndexModel extension methods
//*******

/**
 * Construct an instance with the given keys and options.
 *
 * @param keys the index keys
 * @param options the index options
 */
fun IndexModel.IndexModel(keys: String, options: IndexOptions = IndexOptions()): IndexModel
        = IndexModel(toBson(keys), options)

//*******
//DistinctIterable extension methods
//*******

/**
 * Sets the query filter to apply to the query.
 *
 * @param filter the filter, which may be null
 * @return this
 */
fun <T> DistinctIterable<T>.filter(filter: String): DistinctIterable<T>
        = filter(toBson(filter))

//*******
//FindIterable extension methods
//*******

/**
 * Sets the query filter to apply to the query.
 *
 * @param filter the filter, which may be null
 * @return this
 */
fun <T> FindIterable<T>.filter(filter: String): FindIterable<T>
        = filter(toBson(filter))

/**
 * Sets the query modifiers to apply to this operation.
 *
 * @param modifiers the query modifiers to apply
 * @return this
 */
fun <T> FindIterable<T>.modifiers(modifiers: String): FindIterable<T>
        = modifiers(toBson(modifiers))

/**
 * Sets a document describing the fields to return for all matching documents.
 *
 * @param projection the project document
 * @return this
 */
fun <T> FindIterable<T>.projection(projection: String): FindIterable<T>
        = projection(toBson(projection))

/**
 * Sets the sort criteria to apply to the query.
 *
 * @param sort the sort criteria
 * @return this
 */
fun <T> FindIterable<T>.sort(sort: String): FindIterable<T>
        = sort(toBson(sort))

//*******
//MapReduceIterable extension methods
//*******

/**
 * Sets the global variables that are accessible in the map, reduce and finalize functions.
 *
 * @param scope the global variables that are accessible in the map, reduce and finalize functions.
 * @return this
 */
fun <T> MapReduceIterable<T>.scope(scope: String): MapReduceIterable<T>
        = scope(toBson(scope))

/**
 * Sets the sort criteria to apply to the query.
 *
 * @param sort the sort criteria, which may be null
 * @return this
 */
fun <T> MapReduceIterable<T>.sort(sort: String): MapReduceIterable<T>
        = sort(toBson(sort))

/**
 * Sets the query filter to apply to the query.
 *
 * @param filter the filter to apply to the query
 * @return this
 */
fun <T> MapReduceIterable<T>.filter(filter: String): MapReduceIterable<T>
        = filter(toBson(filter))

//*******
//MongoIterable extension methods
//*******

/**
 * Iterates over all the documents, adding each to the given target.
 *
 * @param target   the collection to insert into
 * @param callback a callback that will be passed the target containing all documents
 */
fun <TResult> MongoIterable<TResult>.toList(): List<TResult>
        = into(mutableListOf<TResult>())

//*******
//json extension property
//*******

/**
 * Get the extended json representation of this object
 *
 * See [Mongo extended json format](https://docs.mongodb.com/manual/reference/mongodb-extended-json) for details
 */
val Any.json: String
    get() = toExtendedJson(this)

/**
 * Format this string to remove space(s) between $ and next char
 */
fun String.formatJson(): String
        = KMongoUtil.formatJson(this)

