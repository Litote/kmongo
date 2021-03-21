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

import com.mongodb.CursorType
import com.mongodb.ExplainVerbosity
import com.mongodb.client.model.Collation
import com.mongodb.reactivestreams.client.FindPublisher
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.include
import org.litote.kmongo.util.KMongoUtil
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty

/**
 * Sets the query filter to apply to the query.
 *
 * @param filter the filter, which may be null
 * @return this
 */
fun <T> FindPublisher<T>.filter(filter: String): FindPublisher<T> = filter(KMongoUtil.toBson(filter))

/**
 * Sets a document describing the fields to return for all matching documents.
 *
 * @param projection the project document
 * @return this
 */
fun <T> FindPublisher<T>.projection(projection: String): FindPublisher<T> = projection(KMongoUtil.toBson(projection))

/**
 * Sets a document describing the fields to return for all matching documents.
 *
 * @param projections the properties of the returned fields
 * @return this
 */
fun <T> FindPublisher<T>.projection(vararg projections: KProperty<*>): FindPublisher<T> =
    projection(include(*projections))


/**
 * Sets the sort criteria to apply to the query.
 *
 * @param sort the sort criteria
 * @return this
 */
fun <T> FindPublisher<T>.sort(sort: String): FindPublisher<T> = sort(KMongoUtil.toBson(sort))

/**
 * Maps a value and returns the new FindPublisher.
 */
@Suppress("UNCHECKED_CAST")
fun <I, O> FindPublisher<I>.map(mapper: (I?) -> O?): FindPublisher<O> =
    object : FindPublisher<O> {

        override fun first(): Publisher<O> = this@map.first().map(mapper)

        override fun filter(filter: Bson): FindPublisher<O> = this@map.filter(filter).map(mapper)

        override fun limit(limit: Int): FindPublisher<O> = this@map.limit(limit).map(mapper)

        override fun skip(skip: Int): FindPublisher<O> = this@map.skip(skip).map(mapper)

        override fun maxTime(maxTime: Long, timeUnit: TimeUnit): FindPublisher<O> =
            this@map.maxTime(maxTime, timeUnit).map(mapper)

        override fun maxAwaitTime(maxAwaitTime: Long, timeUnit: TimeUnit): FindPublisher<O> =
            this@map.maxAwaitTime(maxAwaitTime, timeUnit).map(mapper)

        override fun projection(projection: Bson): FindPublisher<O> = this@map.projection(projection).map(mapper)

        override fun sort(sort: Bson): FindPublisher<O> = this@map.sort(sort).map(mapper)

        override fun noCursorTimeout(noCursorTimeout: Boolean): FindPublisher<O> =
            this@map.noCursorTimeout(noCursorTimeout).map(mapper)

        override fun oplogReplay(oplogReplay: Boolean): FindPublisher<O> = this@map.oplogReplay(oplogReplay).map(mapper)

        override fun partial(partial: Boolean): FindPublisher<O> = this@map.partial(partial).map(mapper)

        override fun cursorType(cursorType: CursorType): FindPublisher<O> = this@map.cursorType(cursorType).map(mapper)

        override fun collation(collation: Collation): FindPublisher<O> = this@map.collation(collation).map(mapper)

        override fun comment(comment: String): FindPublisher<O> = this@map.comment(comment).map(mapper)

        override fun hint(hint: Bson): FindPublisher<O> = this@map.hint(hint).map(mapper)

        override fun hintString(hint: String): FindPublisher<O> = this@map.hintString(hint).map(mapper)

        override fun max(max: Bson): FindPublisher<O> = this@map.max(max).map(mapper)

        override fun min(min: Bson): FindPublisher<O> = this@map.min(min).map(mapper)

        override fun returnKey(returnKey: Boolean): FindPublisher<O> = this@map.returnKey(returnKey).map(mapper)

        override fun showRecordId(showRecordId: Boolean): FindPublisher<O> =
            this@map.showRecordId(showRecordId).map(mapper)

        override fun batchSize(batchSize: Int): FindPublisher<O> = this@map.batchSize(batchSize).map(mapper)

        override fun allowDiskUse(allowDiskUse: Boolean?): FindPublisher<O> =
            this@map.allowDiskUse(allowDiskUse).map(mapper)

        override fun explain(): Publisher<Document> = this@map.explain()

        override fun explain(verbosity: ExplainVerbosity): Publisher<Document> = this@map.explain(verbosity)

        override fun <E : Any?> explain(explainResultClass: Class<E>): Publisher<E> =
            this@map.explain(explainResultClass)

        override fun <E : Any?> explain(explainResultClass: Class<E>, verbosity: ExplainVerbosity): Publisher<E> =
            this@map.explain(explainResultClass, verbosity)

        override fun subscribe(subscriber: Subscriber<in O>) {
            this@map.subscribe(mapper, subscriber)
        }
    }

fun <I, O> Publisher<I>.map(mapper: (I?) -> O?): Publisher<O> =
    Publisher<O> { subscriber -> subscribe(mapper, subscriber) }

private fun <I, O> Publisher<I>.subscribe(mapper: (I?) -> O?, subscriber: Subscriber<in O>) =
    subscribe(object : Subscriber<I> {
        override fun onComplete() {
            subscriber.onComplete()
        }

        override fun onSubscribe(s: Subscription) {
            subscriber.onSubscribe(s)
        }

        override fun onNext(t: I?) {
            subscriber.onNext(mapper(t))
        }

        override fun onError(t: Throwable) {
            subscriber.onError(t)
        }
    })