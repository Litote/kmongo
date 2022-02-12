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

package org.litote.kmongo.reactivestreams

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
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.AggregatePublisher
import com.mongodb.reactivestreams.client.DistinctPublisher
import com.mongodb.reactivestreams.client.FindPublisher
import com.mongodb.reactivestreams.client.ListIndexesPublisher
import com.mongodb.reactivestreams.client.MapReducePublisher
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.SetTo
import org.litote.kmongo.and
import org.litote.kmongo.excludeId
import org.litote.kmongo.fields
import org.litote.kmongo.include
import org.litote.kmongo.path
import org.litote.kmongo.set
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.PairProjection
import org.litote.kmongo.util.SingleProjection
import org.litote.kmongo.util.TripleProjection
import org.litote.kmongo.util.UpdateConfiguration
import org.litote.kmongo.util.pairProjectionCodecRegistry
import org.litote.kmongo.util.singleProjectionCodecRegistry
import org.litote.kmongo.util.tripleProjectionCodecRegistry
import org.reactivestreams.Publisher
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 */
fun <T> MongoCollection<T>.countDocuments(filter: String): Publisher<Long> =
    countDocuments(filter, CountOptions())

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 */
fun <T> MongoCollection<T>.countDocuments(filter: String, options: CountOptions): Publisher<Long> =
    countDocuments(KMongoUtil.toBson(filter), options)


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
 * @param fieldName   the field
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
fun <T> MongoCollection<T>.find(filter: String): FindPublisher<T> = find(KMongoUtil.toBson(filter))

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
fun <T> MongoCollection<T>.findOne(filter: String = KMongoUtil.EMPTY_JSON): Publisher<T> = find(filter).first()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 */
fun <T> MongoCollection<T>.findOne(filter: Bson): Publisher<T> = find(filter).first()

/**
 * Finds the first document that match the filters in the collection.
 *
 * @param filters the query filters
 */
fun <T> MongoCollection<T>.findOne(vararg filters: Bson?): Publisher<T> =
    findOne(and(*filters))

/**
 * Finds the document that match the id parameter.
 *
 * @param id       the object id
 */
fun <T> MongoCollection<T>.findOneById(id: Any): Publisher<T> =
    findOne(KMongoUtil.idFilterQuery(id))

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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified TResult : Any> MongoCollection<*>.mapReduce(
    mapFunction: String,
    reduceFunction: String
): MapReducePublisher<TResult> = mapReduceWith(mapFunction, reduceFunction)

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
): MapReducePublisher<TResult> = mapReduce(mapFunction, reduceFunction, TResult::class.java)


/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 */
inline fun <reified T : Any> MongoCollection<T>.insertOne(
    document: String
): Publisher<InsertOneResult> = insertOne(document, InsertOneOptions())

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param options  the options to apply to the operation
 */
inline fun <reified T : Any> MongoCollection<T>.insertOne(
    document: String,
    options: InsertOneOptions
): Publisher<InsertOneResult> =
    withDocumentClass<BsonDocument>().insertOne(KMongoUtil.toBson(document, T::class), options)


/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filter   the query filter to apply the the delete operation
 */
fun <T> MongoCollection<T>.deleteOne(filter: String): Publisher<DeleteResult> = deleteOne(KMongoUtil.toBson(filter))

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filters   the query filters to apply the the delete operation
 */
fun <T> MongoCollection<T>.deleteOne(vararg filters: Bson?): Publisher<DeleteResult> = deleteOne(and(*filters))

/**
 * Removes at most one document from the id parameter.  If no documents match, the collection is not
 * modified.
 *
 * @param id   the object id
 */
fun <T> MongoCollection<T>.deleteOneById(id: Any): Publisher<DeleteResult> =
    deleteOne(KMongoUtil.idFilterQuery(id))

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filter   the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 */
fun <T> MongoCollection<T>.deleteMany(
    filter: String,
    options: DeleteOptions = DeleteOptions()
): Publisher<DeleteResult> = deleteMany(KMongoUtil.toBson(filter), options)

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filter   the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 */
fun <T> MongoCollection<T>.deleteMany(
    vararg filters: Bson?,
    options: DeleteOptions = DeleteOptions()
): Publisher<DeleteResult> = deleteMany(and(*filters), options)

/**
 * Save the document.
 * If the document has no id field, or if the document has a null id value, insert the document.
 * Otherwise, call [replaceOneById] with upsert true.
 *
 * @param document the document to save
 */
fun <T : Any> MongoCollection<T>.save(document: T): Publisher<*> {
    val id = KMongoUtil.getIdValue(document)
    return if (id != null) {
        replaceOneById(id, document, ReplaceOptions().upsert(true))
    } else {
        insertOne(document)
    }
}

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param id          the object id
 * @param replacement the replacement document
 */
fun <T : Any> MongoCollection<T>.replaceOneById(
    id: Any,
    replacement: T
): Publisher<UpdateResult> = replaceOneById(id, replacement, ReplaceOptions())

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param id          the object id
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 */
fun <T : Any> MongoCollection<T>.replaceOneById(
    id: Any,
    replacement: T,
    options: ReplaceOptions
): Publisher<UpdateResult> = withDocumentClass<BsonDocument>().replaceOne(
    KMongoUtil.idFilterQuery(id),
    KMongoUtil.filterIdToBson(replacement), options
)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param replacement the replacement document - must have a non null id
 */
inline fun <reified T : Any> MongoCollection<T>.replaceOne(
    replacement: T
): Publisher<UpdateResult> = replaceOneById(KMongoUtil.extractId(replacement, T::class), replacement)

/**
 * Replace a document in the collection according to the specified arguments.
 * The id of the provided document is not used, in order to avoid updated id error.
 * You may have to use [UpdateResult.getUpsertedId] in order to retrieve the generated id.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 */
inline fun <reified T : Any> MongoCollection<T>.replaceOneWithoutId(
    filter: Bson,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Publisher<UpdateResult> = withDocumentClass<BsonDocument>().replaceOne(
    filter,
    KMongoUtil.filterIdToBson(replacement),
    options
)

/**
 * Replace a document in the collection according to the specified arguments.
 * The id of the provided document is not used, in order to avoid updated id error.
 * You may have to use [UpdateResult.getUpsertedId] in order to retrieve the generated id.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 */
fun <T : Any> MongoCollection<T>.replaceOneWithoutId(
    filter: String,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Publisher<UpdateResult> = withDocumentClass<BsonDocument>().replaceOne(
    KMongoUtil.toBson(filter),
    KMongoUtil.filterIdToBson(replacement),
    options
)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 */
fun <T : Any> MongoCollection<T>.replaceOne(
    filter: String,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Publisher<UpdateResult> = replaceOne(KMongoUtil.toBson(filter), replacement, options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the update operation
 */
fun <T> MongoCollection<T>.updateOne(
    filter: String,
    update: String,
    options: UpdateOptions = UpdateOptions()
): Publisher<UpdateResult> = updateOne(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param options  the options to apply to the update operation
 * @param updateOnlyNotNullProperties if true do not change null properties
 */
fun <T> MongoCollection<T>.updateOne(
    filter: String,
    update: Any,
    options: UpdateOptions = UpdateOptions(),
    updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
): Publisher<UpdateResult> = updateOne(KMongoUtil.toBson(filter), KMongoUtil.setModifier(update, updateOnlyNotNullProperties), options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   the update object
 * @param options  the options to apply to the update operation
 */
fun <T> MongoCollection<T>.updateOne(
    filter: Bson,
    target: Any,
    options: UpdateOptions = UpdateOptions()
): Publisher<UpdateResult> = updateOne(filter, KMongoUtil.toBsonModifier(target), options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 */
inline fun <reified T : Any> MongoCollection<T>.updateOne(
    target: T,
    options: UpdateOptions = UpdateOptions()
): Publisher<UpdateResult> {
    return updateOneById(KMongoUtil.extractId(target, T::class), target, options)
}

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param id        the object id
 * @param update    the update object
 * @param options  the options to apply to the update operation
 */
fun <T> MongoCollection<T>.updateOneById(
    id: Any,
    update: Any,
    options: UpdateOptions = UpdateOptions()
): Publisher<UpdateResult> = updateOne(KMongoUtil.idFilterQuery(id), KMongoUtil.toBsonModifier(update), options)

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param updateOptions  the options to apply to the update operation
 */
fun <T> MongoCollection<T>.updateMany(
    filter: String,
    update: String,
    updateOptions: UpdateOptions = UpdateOptions()
): Publisher<UpdateResult> = updateMany(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), updateOptions)

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter        a document describing the query filter, which may not be null.
 * @param updates        a document describing the update, which may not be null. The update to apply must include only update operators.
 * @param updateOptions the options to apply to the update operation
 */
fun <T : Any> MongoCollection<T>.updateMany(
    filter: Bson,
    vararg updates: SetTo<*>,
    updateOptions: UpdateOptions = UpdateOptions()
): Publisher<UpdateResult> = updateMany(filter, set(*updates), updateOptions)

/**
 * Atomically find a document and remove it.
 *
 * @param filter   the query filter to find the document with
 * @param options  the options to apply to the operation
 */
fun <T> MongoCollection<T>.findOneAndDelete(
    filter: String,
    options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
): Publisher<T> = findOneAndDelete(KMongoUtil.toBson(filter), options)

/**
 * Atomically find a document and replace it.
 *
 * @param filter      the query filter to apply the the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the operation
 */
fun <T> MongoCollection<T>.findOneAndReplace(
    filter: String,
    replacement: T,
    options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
): Publisher<T> = findOneAndReplace(KMongoUtil.toBson(filter), replacement, options)

/**
 * Atomically find a document and update it.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the operation
 */
fun <T> MongoCollection<T>.findOneAndUpdate(
    filter: String,
    update: String,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Publisher<T> = findOneAndUpdate(KMongoUtil.toBson(filter), KMongoUtil.toBson(update), options)

/**
 * Creates an index.
 *
 * @param key      an object describing the index key(s)
 */
fun <T> MongoCollection<T>.createIndex(key: String): Publisher<String> =
    createIndex(key, IndexOptions())

/**
 * Creates an index.

 * @param key      an object describing the index key(s)
 * @param options  the options for the index
 */
fun <T> MongoCollection<T>.createIndex(key: String, options: IndexOptions): Publisher<String> =
    createIndex(KMongoUtil.toBson(key), options)

/**
 * Get all the indexes in this collection.

 * @param <TResult>   the target document type of the iterable.
 * @return the list indexes iterable interface
 */
inline fun <reified TResult : Any> MongoCollection<*>.listTypedIndexes(): ListIndexesPublisher<TResult> =
    listIndexes(TResult::class.java)

/**
 * Drops the index given the keys used to create it.

 * @param keys the keys of the index to remove
 */
fun <T> MongoCollection<T>.dropIndexOfKeys(keys: String): Publisher<Void> = dropIndex(KMongoUtil.toBson(keys))

/**
 * Executes a mix of inserts, updates, replaces, and deletes.

 * @param requests the writes to execute
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    vararg requests: String
): Publisher<BulkWriteResult> = withDocumentClass<BsonDocument>().bulkWrite(
    KMongoUtil.toWriteModel(requests, codecRegistry, T::class),
    BulkWriteOptions()
)

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    vararg requests: String,
    options: BulkWriteOptions = BulkWriteOptions()
): Publisher<BulkWriteResult> = withDocumentClass<BsonDocument>().bulkWrite(
    KMongoUtil.toWriteModel(requests, codecRegistry, T::class),
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
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    vararg requests: WriteModel<T>,
    options: BulkWriteOptions = BulkWriteOptions()
): Publisher<BulkWriteResult> = bulkWrite(requests.toList(), options)

/**
 * Returns the specified field for all matching documents.
 *
 * @param property the property to return
 * @param query the optional find query
 * @param options the optional [FindPublisher] modifiers
 * @return a property value FindPublisher
 */
inline fun <T, reified F> MongoCollection<T>.projection(
    property: KProperty<F>,
    query: Bson = EMPTY_BSON,
    options: (FindPublisher<SingleProjection<F>>) -> FindPublisher<SingleProjection<F>> = { it }
): FindPublisher<F> =
    withDocumentClass<SingleProjection<F>>()
        .withCodecRegistry(singleProjectionCodecRegistry(property.path(), F::class, codecRegistry))
        .find(query)
        .let { options(it) }
        .projection(fields(excludeId(), include(property)))
        .map { it?.field }

/**
 * Returns the specified two fields for all matching documents.
 *
 * @param property1 the first property to return
 * @param property2 the second property to return
 * @param query the optional find query
 * @param options the optional [FindPublisher] modifiers
 * @return a pair of property values FindPublisher
 */
inline fun <T, reified F1, reified F2> MongoCollection<T>.projection(
    property1: KProperty<F1>,
    property2: KProperty<F2>,
    query: Bson = EMPTY_BSON,
    options: (FindPublisher<PairProjection<F1, F2>>) -> FindPublisher<PairProjection<F1, F2>> = { it }
): FindPublisher<Pair<F1?, F2?>> =
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
        .map { it?.field1 to it?.field2 }

/**
 * Returns the specified three fields for all matching documents.
 *
 * @param property1 the first property to return
 * @param property2 the second property to return
 * @param property3 the third property to return
 * @param query the optional find query
 * @param options the optional [FindPublisher] modifiers
 * @return a triple of property values FindPublisher
 */
inline fun <T, reified F1, reified F2, reified F3> MongoCollection<T>.projection(
    property1: KProperty<F1>,
    property2: KProperty<F2>,
    property3: KProperty<F3>,
    query: Bson = EMPTY_BSON,
    options: (FindPublisher<TripleProjection<F1, F2, F3>>) -> FindPublisher<TripleProjection<F1, F2, F3>> = { it }
): FindPublisher<Triple<F1?, F2?, F3?>> =
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
        .map { Triple(it?.field1, it?.field2, it?.field3) }