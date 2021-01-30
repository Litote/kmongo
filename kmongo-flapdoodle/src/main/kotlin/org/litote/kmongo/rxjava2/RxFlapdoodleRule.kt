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

package org.litote.kmongo.rxjava2

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.reactivex.Completable
import org.bson.types.ObjectId
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.litote.kmongo.reactivestreams.KFlapdoodleReactiveStreams
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KClass

/**
 * A [org.junit.Rule] to help writing tests for KMongo using [Flapdoodle](http://flapdoodle-oss.github.io/de.flapdoodle.embed.mongo/).
 */
class RxFlapdoodleRule<T : Any>(
    val defaultDocumentClass: KClass<T>,
    val generateRandomCollectionName: Boolean = false,
    val dbName: String = "test"
) : TestRule {

    companion object {

        inline fun <reified T : Any> rule(generateRandomCollectionName: Boolean = false): RxFlapdoodleRule<T> =
            RxFlapdoodleRule(T::class, generateRandomCollectionName)

    }

    val mongoClient: MongoClient = KFlapdoodleReactiveStreams.mongoClient
    val database: MongoDatabase by lazy {
        mongoClient.getDatabase(dbName)
    }

    inline fun <reified T : Any> getCollection(): MongoCollection<T> =
        database.getCollection(KMongoUtil.defaultCollectionName(T::class), T::class.java)

    fun <T : Any> getCollection(clazz: KClass<T>): MongoCollection<T> =
        database.getCollection(KMongoUtil.defaultCollectionName(clazz), clazz.java)

    fun <T : Any> getCollection(name: String, clazz: KClass<T>): MongoCollection<T> =
        database.getCollection(name, clazz.java)

    inline fun <reified T : Any> dropCollection() = dropCollection(KMongoUtil.defaultCollectionName(T::class))

    fun dropCollection(clazz: KClass<*>) = dropCollection(KMongoUtil.defaultCollectionName(clazz))

    fun dropCollection(collectionName: String) = database.getCollection(collectionName).drop()

    fun <T> MongoCollection<T>.drop(): Completable {
        return Completable.fromPublisher(this.drop())
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
                    Completable.fromPublisher(col.drop()).blockingAwait()
                }
            }
        }
    }

}