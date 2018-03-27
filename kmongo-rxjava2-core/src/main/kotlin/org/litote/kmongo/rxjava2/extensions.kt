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

package org.litote.kmongo.rxjava2

import com.mongodb.Block
import com.mongodb.async.AsyncBatchCursor
import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.*
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import io.reactivex.*
import io.reactivex.Observable
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.toBson


/**
 * RX wrapper for SingleResultCallback<T>
 *
 * @param callback lambda that will be supplied to wrapped SingleResultCallback<T>
 * @param <T>            the default target type of the collection to return
 */
inline fun <T> maybeResult(crossinline callback: (SingleResultCallback<T>) -> Unit): Maybe<T> {
    return Maybe.create<T> { emitter ->
        callback(SingleResultCallback { result: T?, throwable: Throwable? ->
            when {
                throwable != null -> emitter.onError(throwable)
                result != null -> emitter.onSuccess(result)
                else -> emitter.onComplete()
            }
        })
    }
}

/**
 * RX wrapper for SingleResultCallback<T>
 *
 * @param callback lambda that will be supplied to wrapped SingleResultCallback<T>
 * @param <T>            the default target type of the collection to return
 */
inline fun <T> singleResult(crossinline callback: (SingleResultCallback<T>) -> Unit): Single<T> {
    return Single.create<T> { emitter ->
        callback(SingleResultCallback { result: T?, throwable: Throwable? ->
            when {
                throwable != null -> emitter.onError(throwable)
                result != null -> emitter.onSuccess(result)
                else -> emitter.onError(NullPointerException("Null value passed"))
            }
        })
    }
}

/**
 * RX wrapper for SingleResultCallback<T>
 *
 * @param callback lambda that will be supplied to wrapped SingleResultCallback<T>
 */
inline fun completableResult(crossinline callback: (SingleResultCallback<Void>) -> Unit): Completable {
    return Completable.create { emitter ->
        callback(SingleResultCallback { result: Void?, throwable: Throwable? ->
            when {
                throwable != null -> emitter.onError(throwable)
                else -> emitter.onComplete()
            }
        })
    }
}

//*******
//IndexModel extension methods
//*******

/**
 * Construct an instance with the given keys and options.
 *
 * @param keys the index keys
 * @param options the index options
 */
fun IndexModel.IndexModel(keys: String, options: IndexOptions = IndexOptions()): IndexModel = IndexModel(toBson(keys), options)

//*******
//DistinctIterable extension methods
//*******

/**
 * Sets the query filter to apply to the query.
 *
 * @param filter the filter, which may be null
 * @return this
 */
fun <T> DistinctIterable<T>.filter(filter: String): DistinctIterable<T> = filter(toBson(filter))

//*******
//FindIterable extension methods
//*******

/**
 * Sets the query filter to apply to the query.
 *
 * @param filter the filter, which may be null
 * @return this
 */
fun <T> FindIterable<T>.filter(filter: String): FindIterable<T> = filter(toBson(filter))

/**
 * Sets the query modifiers to apply to this operation.
 *
 * @param modifiers the query modifiers to apply
 * @return this
 */
fun <T> FindIterable<T>.modifiers(modifiers: String): FindIterable<T> = modifiers(toBson(modifiers))

/**
 * Sets a document describing the fields to return for all matching documents.
 *
 * @param projection the project document
 * @return this
 */
fun <T> FindIterable<T>.projection(projection: String): FindIterable<T> = projection(toBson(projection))

/**
 * Sets the sort criteria to apply to the query.
 *
 * @param sort the sort criteria
 * @return this
 */
fun <T> FindIterable<T>.sort(sort: String): FindIterable<T> = sort(toBson(sort))

//*******
//MapReduceIterable extension methods
//*******

/**
 * Sets the global variables that are accessible in the map, reduce and finalize functions.
 *
 * @param scope the global variables that are accessible in the map, reduce and finalize functions.
 * @return this
 */
fun <T> MapReduceIterable<T>.scope(scope: String): MapReduceIterable<T> = scope(toBson(scope))

/**
 * Sets the sort criteria to apply to the query.
 *
 * @param sort the sort criteria, which may be null
 * @return this
 */
fun <T> MapReduceIterable<T>.sort(sort: String): MapReduceIterable<T> = sort(toBson(sort))

/**
 * Sets the query filter to apply to the query.
 *
 * @param filter the filter to apply to the query
 * @return this
 */
fun <T> MapReduceIterable<T>.filter(filter: String): MapReduceIterable<T> = filter(toBson(filter))

//*******
//MongoIterable extension methods
//*******

/**
 * Iterates over all the documents, adding each to an observable.
 *
 */
fun <TResult> MongoIterable<TResult>.toObservable(): Observable<TResult> {
    return Observable.create { source ->
        batchCursor { cursor, t1 ->
            if (t1 != null) {
                source.onError(t1)
            } else {
                loopCursor(cursor,
                        Block<TResult> { res ->
                            source.onNext(res)
                        },
                        SingleResultCallback { _, t2 ->
                            if (t2 != null) {
                                source.onError(t2)
                            } else {
                                source.onComplete()
                            }
                        })
            }
        }
    }
}

fun <TResult> MongoIterable<TResult>.toFlowable(mode: BackpressureStrategy = BackpressureStrategy.MISSING): Flowable<TResult> {
    return Flowable.create({ source ->
        batchCursor { cursor, t1 ->
            if (t1 != null) {
                source.onError(t1)
            } else {
                loopCursor(cursor,
                        Block<TResult> { res ->
                            source.onNext(res)
                        },
                        SingleResultCallback { _, t2 ->
                            if (t2 != null) {
                                source.onError(t2)
                            } else {
                                source.onComplete()
                            }
                        })
            }
        }
    }, mode)
}

private fun <TResult> MongoIterable<TResult>.loopCursor(batchCursor: AsyncBatchCursor<TResult>,
                                                        block: Block<in TResult>,
                                                        callback: SingleResultCallback<Void>) {
    batchCursor.next { results, t ->
        if (t != null || results == null) {
            batchCursor.close()
            callback.onResult(null, t)
        } else {
            try {
                for (result in results) {
                    block.apply(result)
                }
                loopCursor(batchCursor, block, callback)
            } catch (tr: Throwable) {
                batchCursor.close()
                callback.onResult(null, tr)
            }

        }
    }
}

//*******
//json extension property
//*******

/**
 * Get the extended json representation of this object
 *
 * See [Mongo extended json format](https://docs.mongodb.com/manual/reference/mongodb-extended-json) for details
 */
val Any.json: String
    get() = KMongoUtil.toExtendedJson(this)


/**
 * Format this string to remove space(s) between $ and next char
 */
fun String.formatJson(): String = KMongoUtil.formatJson(this)

