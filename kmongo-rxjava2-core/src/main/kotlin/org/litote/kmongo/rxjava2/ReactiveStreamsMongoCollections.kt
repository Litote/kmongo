/*
 * Copyright (C) 2016/2022 Litote
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

package org.litote.kmongo.rxjava2

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
import com.mongodb.reactivestreams.client.AggregatePublisher
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.DistinctPublisher
import com.mongodb.reactivestreams.client.FindPublisher
import com.mongodb.reactivestreams.client.ListIndexesPublisher
import com.mongodb.reactivestreams.client.MapReducePublisher
import com.mongodb.reactivestreams.client.MongoCollection
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.Maybe
import io.reactivex.Single
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.SetTo
import org.litote.kmongo.and
import org.litote.kmongo.ascending
import org.litote.kmongo.path
import org.litote.kmongo.set
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.filterIdToBson
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import org.litote.kmongo.util.KMongoUtil.toBson
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Create a new MongoCollection instance with a different default class to cast any documents returned from the database into..
 *
 * @param <NewTDocument> the default class to cast any documents returned from the database into.
 * @return a new MongoCollection instance with the different default class
 */
inline fun <reified NewTDocument : Any> MongoCollection<*>.withDocumentClass(): MongoCollection<NewTDocument> =
    withDocumentClass(NewTDocument::class.java)

/**
 * Counts the number of documents
 *
 * @return count of all collection
 */
fun <T> MongoCollection<T>.countDocuments(): Single<Long> = countDocuments().single()

/**
 * Counts the number of documents
 *
 * @param clientSession the client session
 * @return count of all collection
 */
fun <T> MongoCollection<T>.countDocuments(clientSession: ClientSession): Single<Long> = countDocuments(clientSession).single()

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param filter   the query filter
 * @return count of filtered collection
 */
fun <T> MongoCollection<T>.countDocuments(filter: String, options: CountOptions = CountOptions()): Single<Long> =
    countDocuments(toBson(filter), options).single()

/**
 * Counts the number of documents in the collection according to the given options.
 *
 * @param clientSession the client session
 * @param filter   the query filter
 * @return count of filtered collection
 */
fun <T> MongoCollection<T>.countDocuments(clientSession: ClientSession, filter: String, options: CountOptions = CountOptions()): Single<Long> =
        countDocuments(clientSession, toBson(filter), options).single()

/**
 * Gets the distinct values of the specified field name.
 *
 * @param fieldName   the field name
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(fieldName: String): DistinctPublisher<TResult> {
    return distinct(fieldName, KMongoUtil.EMPTY_JSON)
}

/**
 * Gets the distinct values of the specified field name.
 *
 * @param clientSession the client session
 * @param fieldName   the field name
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(clientSession: ClientSession, fieldName: String): DistinctPublisher<TResult> {
    return distinct(clientSession, fieldName, KMongoUtil.EMPTY_JSON)
}

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
): DistinctPublisher<TResult> {
    return distinct(fieldName, toBson(filter), TResult::class.java)
}

/**
 * Gets the distinct values of the specified field name.
 *
 * @param clientSession the client session
 * @param fieldName   the field name
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified TResult : Any> MongoCollection<*>.distinct(
    clientSession: ClientSession,
    fieldName: String,
    filter: String
): DistinctPublisher<TResult> {
    return distinct(clientSession, fieldName, toBson(filter), TResult::class.java)
}

/**
 * Gets the distinct values of the specified field name.
 *
 * @param field   the field name
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified T : Any, reified TResult : Any> MongoCollection<*>.distinct(
    field: KProperty1<T, TResult>,
    filter: Bson = EMPTY_BSON
): DistinctPublisher<TResult> {
    return distinct(field.path(), filter, TResult::class.java)
}

/**
 * Gets the distinct values of the specified field name.
 *
 * @param clientSession the client session
 * @param field   the field name
 * @param filter      the query filter
 * @param <TResult>   the target type of the iterable
 * @return an iterable of distinct values
 */
inline fun <reified T : Any, reified TResult : Any> MongoCollection<*>.distinct(
    clientSession: ClientSession,
    field: KProperty1<T, TResult>,
    filter: Bson = EMPTY_BSON
): DistinctPublisher<TResult> {
    return distinct(clientSession, field.path(), filter, TResult::class.java)
}

/**
 * Finds all documents that match the filter in the collection.
 *
 * @param  filter the query filter
 * @return the find iterable interface
 */
fun <T : Any> MongoCollection<T>.find(filter: String): FindPublisher<T> = find(toBson(filter))

/**
 * Finds all documents that match the filter in the collection.
 *
 * @param clientSession the client session
 * @param  filter the query filter
 * @return the find iterable interface
 */
fun <T : Any> MongoCollection<T>.find(clientSession: ClientSession, filter: String): FindPublisher<T> = find(clientSession, toBson(filter))

/**
 * Finds all documents that match the filters in the collection.
 *
 * @param  filters the query filter
 * @return the find iterable interface
 */
fun <T : Any> MongoCollection<T>.find(vararg filters: Bson?): FindPublisher<T> = find(and(*filters))

/**
 * Finds all documents that match the filters in the collection.
 *
 * @param clientSession the client session
 * @param  filters the query filter
 * @return the find iterable interface
 */
fun <T : Any> MongoCollection<T>.find(clientSession: ClientSession, vararg filters: Bson?): FindPublisher<T> = find(clientSession, and(*filters))

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 */
fun <T : Any> MongoCollection<T>.findOne(filter: String = KMongoUtil.EMPTY_JSON): Maybe<T> = find(filter).maybe()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param clientSession the client session
 * @param filter the query filter
 */
fun <T : Any> MongoCollection<T>.findOne(clientSession: ClientSession, filter: String = KMongoUtil.EMPTY_JSON): Maybe<T> = find(clientSession, filter).maybe()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param filter the query filter
 */
fun <T : Any> MongoCollection<T>.findOne(filter: Bson): Maybe<T> = find(filter).first().maybe()

/**
 * Finds the first document that match the filter in the collection.
 *
 * @param clientSession the client session
 * @param filter the query filter
 */
fun <T : Any> MongoCollection<T>.findOne(clientSession: ClientSession, filter: Bson): Maybe<T> = find(clientSession, filter).first().maybe()

/**
 * Finds the first document that match the filters in the collection.
 *
 * @param filters the query filters
 */
fun <T : Any> MongoCollection<T>.findOne(vararg filters: Bson?): Maybe<T> = find(*filters).first().maybe()

/**
 * Finds the first document that match the filters in the collection.
 *
 * @param clientSession the client session
 * @param filters the query filters
 */
fun <T : Any> MongoCollection<T>.findOne(clientSession: ClientSession, vararg filters: Bson?): Maybe<T> = find(clientSession, *filters).first().maybe()

/**
 * Finds the document that match the id parameter.
 *
 * @param id       the object id
 */
fun <T : Any> MongoCollection<T>.findOneById(id: Any): Maybe<T> {
    return findOne(idFilterQuery(id))
}

/**
 * Finds the document that match the id parameter.
 *
 * @param clientSession the client session
 * @param id       the object id
 */
fun <T : Any> MongoCollection<T>.findOneById(clientSession: ClientSession, id: Any): Maybe<T> {
    return findOne(clientSession, idFilterQuery(id))
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
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: String): AggregatePublisher<TResult> {
    return aggregate(KMongoUtil.toBsonList(pipeline, codecRegistry), TResult::class.java)
}

/**
 * Aggregates documents according to the specified aggregation pipeline.  If the pipeline ends with a $out stage, the returned
 * iterable will be a query of the collection that the aggregation was written to.  Note that in this case the pipeline will be
 * executed even if the iterable is never iterated.
 *
 * @param clientSession the client session
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(clientSession: ClientSession, vararg pipeline: String): AggregatePublisher<TResult> {
    return aggregate(clientSession, KMongoUtil.toBsonList(pipeline, codecRegistry), TResult::class.java)
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
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(vararg pipeline: Bson): AggregatePublisher<TResult> {
    return aggregate(pipeline.toList(), TResult::class.java)
}

/**
 * Aggregates documents according to the specified aggregation pipeline.  If the pipeline ends with a $out stage, the returned
 * iterable will be a query of the collection that the aggregation was written to.  Note that in this case the pipeline will be
 * executed even if the iterable is never iterated.
 *
 * @param clientSession the client session
 * @param pipeline    the aggregate pipeline
 * @param <TResult>   the target document type of the iterable
 * @return an iterable containing the result of the aggregation operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.aggregate(clientSession: ClientSession, vararg pipeline: Bson): AggregatePublisher<TResult> {
    return aggregate(clientSession, pipeline.toList(), TResult::class.java)
}

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
): MapReducePublisher<TResult> {
    return mapReduce(mapFunction, reduceFunction, TResult::class.java)
}

/**
 * Aggregates documents according to the specified map-reduce function.
 *
 * @param clientSession the client session
 * @param mapFunction    a JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
 * @param reduceFunction a JavaScript function that "reduces" to a single object all the values associated with a particular key.
 * @param <TResult>      the target document type of the iterable.
 * *
 * @return an iterable containing the result of the map-reduce operation
 */
inline fun <reified TResult : Any> MongoCollection<*>.mapReduceTyped(
    clientSession: ClientSession,
    mapFunction: String,
    reduceFunction: String
): MapReducePublisher<TResult> {
    return mapReduce(clientSession, mapFunction, reduceFunction, TResult::class.java)
}

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 *
 * @param document the document to insert
 * @param options  the options to apply to the operation
 */
inline fun <reified T : Any> MongoCollection<T>.insertOne(
    document: String,
    options: InsertOneOptions = InsertOneOptions()
): Completable =
    withDocumentClass<BsonDocument>().insertOne(
        toBson(document, T::class),
        options
    )
        .completable()

/**
 * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
 * @param clientSession the client session
 * @param document the document to insert
 * @param options  the options to apply to the operation
 */
inline fun <reified T : Any> MongoCollection<T>.insertOne(
    clientSession: ClientSession,
    document: String,
    options: InsertOneOptions = InsertOneOptions()
): Completable =
    withDocumentClass<BsonDocument>().insertOne(
        clientSession,
        toBson(document, T::class),
        options
    )
        .completable()


/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filter   the query filter to apply the the delete operation
 *
 * @return the result of the remove one operation
 */
fun <T> MongoCollection<T>.deleteOne(filter: String): Maybe<DeleteResult> = deleteOne(toBson(filter)).maybe()

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param clientSession the client session
 * @param filter   the query filter to apply the the delete operation
 *
 * @return the result of the remove one operation
 */
fun <T> MongoCollection<T>.deleteOne(clientSession: ClientSession, filter: String): Maybe<DeleteResult> = deleteOne(clientSession, toBson(filter)).maybe()

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param filters   the query filter to apply the the delete operation
 *
 * @return the result of the remove one operation
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoException
 */
fun <T> MongoCollection<T>.deleteOne(vararg filters: Bson?): Maybe<DeleteResult> = deleteOne(and(*filters)).maybe()

/**
 * Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
 * modified.
 *
 * @param clientSession the client session
 * @param filters   the query filter to apply the the delete operation
 *
 * @return the result of the remove one operation
 *
 * @throws com.mongodb.MongoWriteException
 * @throws com.mongodb.MongoWriteConcernException
 * @throws com.mongodb.MongoException
 */
fun <T> MongoCollection<T>.deleteOne(clientSession: ClientSession, vararg filters: Bson?): Maybe<DeleteResult> = deleteOne(clientSession, and(*filters)).maybe()

/**
 * Removes at most one document from the id parameter.  If no documents match, the collection is not
 * modified.
 *
 * @param id   the object id
 */
fun <T> MongoCollection<T>.deleteOneById(id: Any): Maybe<DeleteResult> = deleteOne(idFilterQuery(id)).maybe()

/**
 * Removes at most one document from the id parameter.  If no documents match, the collection is not
 * modified.
 *
 * @param clientSession the client session
 * @param id   the object id
 */
fun <T> MongoCollection<T>.deleteOneById(clientSession: ClientSession, id: Any): Maybe<DeleteResult> = deleteOne(clientSession, idFilterQuery(id)).maybe()

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filter   the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 */
fun <T> MongoCollection<T>.deleteMany(
    filter: String,
    options: DeleteOptions = DeleteOptions()
): Maybe<DeleteResult> = deleteMany(toBson(filter), options).maybe()

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param clientSession the client session
 * @param filter   the query filter to apply the the delete operation
 * @param options  the options to apply to the delete operation
 */
fun <T> MongoCollection<T>.deleteMany(
    clientSession: ClientSession,
    filter: String,
    options: DeleteOptions = DeleteOptions()
): Maybe<DeleteResult> = deleteMany(clientSession, toBson(filter), options).maybe()

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param filters   the query filters to apply the the delete operation
 * @param options  the options to apply to the delete operation
 */
fun <T> MongoCollection<T>.deleteMany(
    vararg filters: Bson?,
    options: DeleteOptions = DeleteOptions()
): Maybe<DeleteResult> = deleteMany(and(*filters), options).maybe()

/**
 * Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
 *
 * @param clientSession the client session
 * @param filters   the query filters to apply the the delete operation
 * @param options  the options to apply to the delete operation
 */
fun <T> MongoCollection<T>.deleteMany(
    clientSession: ClientSession,
    vararg filters: Bson?,
    options: DeleteOptions = DeleteOptions()
): Maybe<DeleteResult> = deleteMany(clientSession, and(*filters), options).maybe()

/**
 * Save the document.
 * If the document has no id field, or if the document has a null id value, insert the document.
 * Otherwise, call [replaceOneById] with upsert true.
 *
 * @param document the document to save
 */
fun <T : Any> MongoCollection<T>.save(document: T): Completable {
    val id = KMongoUtil.getIdValue(document)
    return if (id != null) {
        replaceOneById(
            id,
            document,
            ReplaceOptions().upsert(true)
        ).flatMapCompletable { _ -> CompletableSource { cs -> cs.onComplete() } }
    } else {
        insertOne(document).completable()
    }
}

/**
 * Save the document.
 * If the document has no id field, or if the document has a null id value, insert the document.
 * Otherwise, call [replaceOneById] with upsert true.
 *
 * @param clientSession the client session
 * @param document the document to save
 */
fun <T : Any> MongoCollection<T>.save(clientSession: ClientSession, document: T): Completable {
    val id = KMongoUtil.getIdValue(document)
    return if (id != null) {
        replaceOneById(
            clientSession,
            id,
            document,
            ReplaceOptions().upsert(true)
        ).flatMapCompletable { _ -> CompletableSource { cs -> cs.onComplete() } }
    } else {
        insertOne(clientSession, document).completable()
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
fun <T : Any> MongoCollection<T>.replaceOneById(
    id: Any,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Maybe<UpdateResult> =
    withDocumentClass<BsonDocument>().replaceOne(idFilterQuery(id), filterIdToBson(replacement), options).maybe()

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param id          the object id
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the replace one operation
 */
fun <T : Any> MongoCollection<T>.replaceOneById(
    clientSession: ClientSession,
    id: Any,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Maybe<UpdateResult> =
    withDocumentClass<BsonDocument>().replaceOne(clientSession, idFilterQuery(id), filterIdToBson(replacement), options).maybe()

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param replacement the document to replace - must have an non null id
 * @param options     the options to apply to the replace operation
 */
inline fun <reified T : Any> MongoCollection<T>.replaceOne(
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Maybe<UpdateResult> = replaceOneById(KMongoUtil.extractId(replacement, T::class), replacement, options)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param replacement the document to replace - must have an non null id
 * @param options     the options to apply to the replace operation
 */
inline fun <reified T : Any> MongoCollection<T>.replaceOne(
    clientSession: ClientSession,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Maybe<UpdateResult> = replaceOneById(clientSession, KMongoUtil.extractId(replacement, T::class), replacement, options)

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the update one operation
 */
fun <T : Any> MongoCollection<T>.replaceOne(
    filter: String,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Maybe<UpdateResult> = replaceOne(toBson(filter), replacement, options).maybe()

/**
 * Replace a document in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the update one operation
 */
fun <T : Any> MongoCollection<T>.replaceOne(
    clientSession: ClientSession,
    filter: String,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Maybe<UpdateResult> = replaceOne(clientSession, toBson(filter), replacement, options).maybe()


/**
 * Replace a document in the collection according to the specified arguments.
 * The id of the provided document is not used, in order to avoid updated id error.
 * You may have to use [UpdateResult.getUpsertedId] in order to retrieve the generated id.
 *
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the update one operation
 */
fun <T : Any> MongoCollection<T>.replaceOneWithoutId(
    filter: Bson,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Maybe<UpdateResult> =
    withDocumentClass<BsonDocument>().replaceOne(
        filter,
        KMongoUtil.filterIdToBson(replacement),
        options
    ).maybe()

/**
 * Replace a document in the collection according to the specified arguments.
 * The id of the provided document is not used, in order to avoid updated id error.
 * You may have to use [UpdateResult.getUpsertedId] in order to retrieve the generated id.
 *
 * @param clientSession the client session
 * @param filter      the query filter to apply to the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the replace operation
 *
 * @return the result of the update one operation
 */
fun <T : Any> MongoCollection<T>.replaceOneWithoutId(
    clientSession: ClientSession,
    filter: Bson,
    replacement: T,
    options: ReplaceOptions = ReplaceOptions()
): Maybe<UpdateResult> =
    withDocumentClass<BsonDocument>().replaceOne(
            clientSession,
            filter,
            KMongoUtil.filterIdToBson(replacement),
            options
    ).maybe()


/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
fun <T> MongoCollection<T>.updateOne(
    filter: String,
    update: String,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateOne(toBson(filter), toBson(update), options).maybe()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
fun <T> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: String,
    update: String,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateOne(clientSession, toBson(filter), toBson(update), options).maybe()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
fun <T : Any> MongoCollection<T>.updateOne(
    filter: String,
    target: T,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateOne(toBson(filter), KMongoUtil.toBsonModifier(target), options).maybe()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param filter   a document describing the query filter
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
fun <T : Any> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: String,
    target: T,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateOne(clientSession, toBson(filter), KMongoUtil.toBsonModifier(target), options).maybe()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
fun <T : Any> MongoCollection<T>.updateOne(
    filter: Bson,
    target: T,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateOne(filter, KMongoUtil.toBsonModifier(target), options).maybe()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param filter   a document describing the query filter
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
fun <T : Any> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    filter: Bson,
    target: T,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateOne(clientSession, filter, KMongoUtil.toBsonModifier(target), options).maybe()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
inline fun <reified T : Any> MongoCollection<T>.updateOne(
    target: T,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateOneById(KMongoUtil.extractId(target, T::class), target, options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param target  the update object - must have an non null id
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
inline fun <reified T : Any> MongoCollection<T>.updateOne(
    clientSession: ClientSession,
    target: T,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateOneById(clientSession, KMongoUtil.extractId(target, T::class), target, options)

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param id        the object id
 * @param update    the update object
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
fun <T> MongoCollection<T>.updateOneById(
    id: Any,
    update: Any,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> =
    updateOne(
        idFilterQuery(id),
        KMongoUtil.toBsonModifier(update),
        options
    ).maybe()

/**
 * Update a single document in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param id        the object id
 * @param update    the update object
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update one operation
 */
fun <T> MongoCollection<T>.updateOneById(
    clientSession: ClientSession,
    id: Any,
    update: Any,
    options: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> =
    updateOne(
            clientSession,
            idFilterQuery(id),
            KMongoUtil.toBsonModifier(update),
            options
    ).maybe()

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update many operation
 */
fun <T> MongoCollection<T>.updateMany(
    filter: String,
    update: String,
    updateOptions: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateMany(toBson(filter), toBson(update), updateOptions).maybe()

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the update operation
 *
 * @return the result of the update many operation
 */
fun <T> MongoCollection<T>.updateMany(
    clientSession: ClientSession,
    filter: String,
    update: String,
    updateOptions: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateMany(clientSession, toBson(filter), toBson(update), updateOptions).maybe()

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param filter   a document describing the query filter
 * @param updates   a document describing the update. The update to apply must include only update operators.
 * @param updateOptions  the options to apply to the update operation
 *
 * @return the result of the update many operation
 */
fun <T> MongoCollection<T>.updateMany(
    filter: Bson,
    vararg updates: SetTo<*>,
    updateOptions: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateMany(filter, set(*updates), updateOptions).maybe()

/**
 * Update all documents in the collection according to the specified arguments.
 *
 * @param clientSession the client session
 * @param filter   a document describing the query filter
 * @param updates   a document describing the update. The update to apply must include only update operators.
 * @param updateOptions  the options to apply to the update operation
 *
 * @return the result of the update many operation
 */
fun <T> MongoCollection<T>.updateMany(
    clientSession: ClientSession,
    filter: Bson,
    vararg updates: SetTo<*>,
    updateOptions: UpdateOptions = UpdateOptions()
): Maybe<UpdateResult> = updateMany(clientSession, filter, set(*updates), updateOptions).maybe()


/**
 * Atomically find a document and remove it.
 *
 * @param filter   the query filter to find the document with
 * @param options  the options to apply to the operation
 *
 * @return the document that was removed.  If no documents matched the query filter, then null will be returned
 */
fun <T : Any> MongoCollection<T>.findOneAndDelete(
    filter: String,
    options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
): Maybe<T> = findOneAndDelete(toBson(filter), options).maybe()

/**
 * Atomically find a document and remove it.
 *
 * @param clientSession the client session
 * @param filter   the query filter to find the document with
 * @param options  the options to apply to the operation
 *
 * @return the document that was removed.  If no documents matched the query filter, then null will be returned
 */
fun <T : Any> MongoCollection<T>.findOneAndDelete(
    clientSession: ClientSession,
    filter: String,
    options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
): Maybe<T> = findOneAndDelete(clientSession, toBson(filter), options).maybe()

/**
 * Atomically find a document and replace it.
 *
 * @param filter      the query filter to apply the the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the operation
 *
 * @return the document that was updated.  Depending on the value of the `returnOriginal` property, this will either be the
 * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
 * returned
 */
fun <T> MongoCollection<T>.findOneAndReplace(
    filter: String,
    replacement: T,
    options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
): Maybe<T> = findOneAndReplace(toBson(filter), replacement, options).maybe()

/**
 * Atomically find a document and replace it.
 *
 * @param clientSession the client session
 * @param filter      the query filter to apply the the replace operation
 * @param replacement the replacement document
 * @param options     the options to apply to the operation
 *
 * @return the document that was updated.  Depending on the value of the `returnOriginal` property, this will either be the
 * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
 * returned
 */
fun <T> MongoCollection<T>.findOneAndReplace(
    clientSession: ClientSession,
    filter: String,
    replacement: T,
    options: FindOneAndReplaceOptions = FindOneAndReplaceOptions()
): Maybe<T> = findOneAndReplace(clientSession, toBson(filter), replacement, options).maybe()


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
fun <T : Any> MongoCollection<T>.findOneAndUpdate(
    filter: String,
    update: String,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Maybe<T> = findOneAndUpdate(toBson(filter), toBson(update), options).maybe()

/**
 * Atomically find a document and update it.
 *
 * @param clientSession the client session
 * @param filter   a document describing the query filter
 * @param update   a document describing the update. The update to apply must include only update operators.
 * @param options  the options to apply to the operation
 *
 * @return the document that was updated.  Depending on the value of the `returnOriginal` property, this will either be the
 * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
 * returned
 */
fun <T : Any> MongoCollection<T>.findOneAndUpdate(
    clientSession: ClientSession,
    filter: String,
    update: String,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Maybe<T> = findOneAndUpdate(clientSession, toBson(filter), toBson(update), options).maybe()

/**
 * Creates an index.  If successful, the callback will be executed with the name of the created index as the result.

 * @param key      an object describing the index key(s)
 * @param options  the options for the index
 *
 * @return the index name
 */
fun <T> MongoCollection<T>.createIndex(key: String, options: IndexOptions = IndexOptions()): Maybe<String> =
    createIndex(toBson(key), options).maybe()

/**
 * Creates an index.  If successful, the callback will be executed with the name of the created index as the result.
 *
 * @param clientSession the client session
 * @param key      an object describing the index key(s)
 * @param options  the options for the index
 *
 * @return the index name
 */
fun <T> MongoCollection<T>.createIndex(clientSession: ClientSession, key: String, options: IndexOptions = IndexOptions()): Maybe<String> =
    createIndex(clientSession, toBson(key), options).maybe()


/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param key      an object describing the index key(s)
 * @param indexOptions  the options for the index
 *
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureIndex(keys: String, indexOptions: IndexOptions = IndexOptions()):
        Completable {
    return createIndex(keys, indexOptions)
        .onErrorResumeNext(
            dropIndex(keys)
                .completable()
                .onErrorComplete()
                .andThen(createIndex(keys, indexOptions))
        )
        .flatMapCompletable { _ ->
            CompletableSource { cs -> cs.onComplete() }
        }
}

/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param clientSession the client session
 * @param key      an object describing the index key(s)
 * @param indexOptions  the options for the index
 *
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureIndex(clientSession: ClientSession, keys: String, indexOptions: IndexOptions = IndexOptions()):
        Completable {
    return createIndex(clientSession, keys, indexOptions)
        .onErrorResumeNext(
                dropIndex(clientSession, keys)
                        .completable()
                        .onErrorComplete()
                        .andThen(createIndex(clientSession, keys, indexOptions))
        )
        .flatMapCompletable { _ ->
            CompletableSource { cs -> cs.onComplete() }
        }
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
fun <T> MongoCollection<T>.ensureIndex(
    keys: Bson,
    indexOptions: IndexOptions = IndexOptions()
): Completable {
    return createIndex(keys, indexOptions).maybe()
        .onErrorResumeNext(
            dropIndex(keys)
                .completable()
                .onErrorComplete()
                .andThen(createIndex(keys, indexOptions).maybe())
        )
        .flatMapCompletable { _ ->
            CompletableSource { cs -> cs.onComplete() }
        }
}
/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param clientSession the client session
 * @param keys      an object describing the index key(s)
 * @param indexOptions  the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureIndex(
        clientSession: ClientSession,
        keys: Bson,
        indexOptions: IndexOptions = IndexOptions()
): Completable {
    return createIndex(clientSession, keys, indexOptions).maybe()
        .onErrorResumeNext(
                dropIndex(clientSession, keys)
                        .completable()
                        .onErrorComplete()
                        .andThen(createIndex(clientSession, keys, indexOptions).maybe())
        )
        .flatMapCompletable { _ ->
            CompletableSource { cs -> cs.onComplete() }
        }
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
fun <T> MongoCollection<T>.ensureIndex(
    vararg properties: KProperty<*>,
    indexOptions: IndexOptions = IndexOptions()
): Completable = ensureIndex(ascending(*properties), indexOptions)

/**
 * Create an index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param clientSession the client session
 * @param properties    the properties, which must contain at least one
 * @param indexOptions  the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureIndex(
    clientSession: ClientSession,
    vararg properties: KProperty<*>,
    indexOptions: IndexOptions = IndexOptions()
): Completable = ensureIndex(clientSession, ascending(*properties), indexOptions)

/**
 * Create an [IndexOptions.unique] index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param properties    the properties, which must contain at least one
 * @param indexOptions  the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureUniqueIndex(
    vararg properties: KProperty<*>,
    indexOptions: IndexOptions = IndexOptions()
): Completable = ensureIndex(ascending(*properties), indexOptions.unique(true))

/**
 * Create an [IndexOptions.unique] index with the given keys and options.
 * If the creation of the index is not doable because an index with the same keys but with different [IndexOptions]
 * already exists, then drop the existing index and create a new one.
 *
 * @param clientSession the client session
 * @param properties    the properties, which must contain at least one
 * @param indexOptions  the options for the index
 * @return the index name
 */
fun <T> MongoCollection<T>.ensureUniqueIndex(
    clientSession: ClientSession,
    vararg properties: KProperty<*>,
    indexOptions: IndexOptions = IndexOptions()
): Completable = ensureIndex(clientSession, ascending(*properties), indexOptions.unique(true))

/**
 * Get all the indexes in this collection.
 *
 * @param <TResult>   the target document type of the iterable.
 *
 * @return the list indexes iterable interface
 */
inline fun <reified TResult : Any> MongoCollection<*>.listTypedIndexes(): ListIndexesPublisher<TResult> =
    listIndexes(TResult::class.java)

/**
 * Get all the indexes in this collection.
 *
 * @param clientSession the client session
 * @param <TResult>   the target document type of the iterable.
 *
 * @return the list indexes iterable interface
 */
inline fun <reified TResult : Any> MongoCollection<*>.listTypedIndexes(clientSession: ClientSession): ListIndexesPublisher<TResult> =
        listIndexes(clientSession, TResult::class.java)

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    vararg requests: String,
    options: BulkWriteOptions = BulkWriteOptions()
): Maybe<BulkWriteResult> =
    withDocumentClass<BsonDocument>().bulkWrite(
        KMongoUtil.toWriteModel(
            requests,
            codecRegistry,
            T::class
        ),
        options
    ).maybe()

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param clientSession the client session
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 *
 * @return the result of the bulk write
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    clientSession: ClientSession,
    vararg requests: String,
    options: BulkWriteOptions = BulkWriteOptions()
): Maybe<BulkWriteResult> =
    withDocumentClass<BsonDocument>().bulkWrite(
            clientSession,
            KMongoUtil.toWriteModel(
                    requests,
                    codecRegistry,
                    T::class
            ),
            options
    ).maybe()

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    vararg requests: WriteModel<T>,
    options: BulkWriteOptions = BulkWriteOptions()
): Maybe<BulkWriteResult> = bulkWrite(requests.toList(), options).maybe()

/**
 * Executes a mix of inserts, updates, replaces, and deletes.
 *
 * @param clientSession the client session
 * @param requests the writes to execute
 * @param options  the options to apply to the bulk write operation
 */
inline fun <reified T : Any> MongoCollection<T>.bulkWrite(
    clientSession: ClientSession,
    vararg requests: WriteModel<T>,
    options: BulkWriteOptions = BulkWriteOptions()
): Maybe<BulkWriteResult> = bulkWrite(clientSession, requests.toList(), options).maybe()