/*
 * Copyright (C) 2016/2020 Litote
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

import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.MongoClient
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import io.reactivex.Completable
import io.reactivex.Maybe
import org.bson.types.ObjectId
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.litote.kmongo.async.KFlapdoodleAsync
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KClass

/**
 * A [org.junit.Rule] to help writing tests for KMongo using [Flapdoodle](http://flapdoodle-oss.github.io/de.flapdoodle.embed.mongo/).
 */
class RxFlapdoodleRule<T : Any>(val defaultDocumentClass: KClass<T>,
                                val generateRandomCollectionName: Boolean = false,
                                val dbName: String = "test") : TestRule {

    companion object {

        inline fun <reified T : Any> rule(generateRandomCollectionName: Boolean = false): RxFlapdoodleRule<T> = RxFlapdoodleRule(T::class, generateRandomCollectionName)

    }

    val mongoClient: MongoClient = KFlapdoodleAsync.mongoClient
    val database: MongoDatabase by lazy {
        mongoClient.getDatabase(dbName)
    }

    private inline fun <T> maybeResult(crossinline callback: (SingleResultCallback<T>) -> Unit): Maybe<T> {
        return Maybe.create { emitter ->
            callback(SingleResultCallback { result: T?, throwable: Throwable? ->
                when {
                    throwable != null -> emitter.onError(throwable)
                    result != null -> emitter.onSuccess(result)
                    else -> emitter.onComplete()
                }
            })
        }
    }

    private inline fun completableResult(crossinline callback: (SingleResultCallback<Void>) -> Unit): Completable {
        return Completable.create { emitter ->
            callback(SingleResultCallback { result: Void?, throwable: Throwable? ->
                when {
                    throwable != null -> emitter.onError(throwable)
                    else -> emitter.onComplete()
                }
            })
        }
    }

    inline fun <reified T : Any> getCollection(): MongoCollection<T> = database.getCollection(KMongoUtil.defaultCollectionName(T::class), T::class.java)

    fun <T : Any> getCollection(clazz: KClass<T>): MongoCollection<T> = database.getCollection(KMongoUtil.defaultCollectionName(clazz), clazz.java)

    fun <T : Any> getCollection(name: String, clazz: KClass<T>): MongoCollection<T> = database.getCollection(name, clazz.java)

    inline fun <reified T : Any> dropCollection() = dropCollection(KMongoUtil.defaultCollectionName(T::class))

    fun dropCollection(clazz: KClass<*>) = dropCollection(KMongoUtil.defaultCollectionName(clazz))

    fun dropCollection(collectionName: String) = database.getCollection(collectionName).drop()

    fun <T> MongoCollection<T>.drop(): Completable {
        return completableResult { this.drop(it) }
    }

    val col: MongoCollection<T> by lazy {
        val name = if (generateRandomCollectionName) {
            ObjectId().toString()
        } else {
            KMongoUtil.defaultCollectionName(defaultDocumentClass)
        }

        getCollection(name, defaultDocumentClass)
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            override fun evaluate() {
                try {
                    base.evaluate()
                } finally {
                    col.drop().blockingAwait()
                }
            }
        }
    }

}