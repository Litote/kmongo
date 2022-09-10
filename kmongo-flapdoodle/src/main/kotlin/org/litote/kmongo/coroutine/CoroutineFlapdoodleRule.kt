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

package org.litote.kmongo.coroutine

import com.mongodb.internal.async.SingleResultCallback
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.litote.kmongo.defaultMongoTestVersion
import org.litote.kmongo.reactivestreams.KFlapdoodleReactiveStreamsConfiguration
import org.litote.kmongo.util.KMongoUtil
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

/**
 * A [org.junit.Rule] to help writing tests for KMongo using [Flapdoodle](http://flapdoodle-oss.github.io/de.flapdoodle.embed.mongo/).
 */
class CoroutineFlapdoodleRule<T : Any>(
    val defaultDocumentClass: KClass<T>,
    val generateRandomCollectionName: Boolean = false,
    val dbName: String = "test",
    val version: IFeatureAwareVersion = defaultMongoTestVersion
) : TestRule {

    companion object {

        inline fun <reified T : Any> rule(generateRandomCollectionName: Boolean = false): CoroutineFlapdoodleRule<T> =
            CoroutineFlapdoodleRule(T::class, generateRandomCollectionName)

        private val versionsMap = ConcurrentHashMap<IFeatureAwareVersion, KFlapdoodleReactiveStreamsConfiguration>()
    }

    private val configuration =
        versionsMap.getOrPut(version) { KFlapdoodleReactiveStreamsConfiguration(version) }
    val mongoClient: MongoClient = configuration.mongoClient
    val database: MongoDatabase by lazy {
        mongoClient.getDatabase(dbName)
    }

    private suspend inline fun <T> singleResult(crossinline callback: (SingleResultCallback<T>) -> Unit): T? {
        return suspendCoroutine { continuation ->
            callback(SingleResultCallback { result: T?, throwable: Throwable? ->
                if (throwable != null) {
                    continuation.resumeWithException(throwable)
                } else {
                    continuation.resume(result)
                }
            })
        }
    }

    inline fun <reified T : Any> getCollection(): MongoCollection<T> =
        database.getCollection(KMongoUtil.defaultCollectionName(T::class), T::class.java)

    fun <T : Any> getCollection(clazz: KClass<T>): MongoCollection<T> =
        database.getCollection(KMongoUtil.defaultCollectionName(clazz), clazz.java)

    fun <T : Any> getCollection(name: String, clazz: KClass<T>): MongoCollection<T> =
        database.getCollection(name, clazz.java)

    suspend inline fun <reified T : Any> dropCollection() = dropCollection(KMongoUtil.defaultCollectionName(T::class))

    suspend fun dropCollection(clazz: KClass<*>) = dropCollection(KMongoUtil.defaultCollectionName(clazz))

    suspend fun dropCollection(collectionName: String) = database.getCollection(collectionName).drop()

    suspend fun <T> MongoCollection<T>.drop(): Void? {
        return singleResult { this.drop() }
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
                    runBlocking { col.drop() }
                }
            }
        }
    }

}