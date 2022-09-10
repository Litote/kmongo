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

package org.litote.kmongo.reactor

import com.mongodb.reactor.client.ReactorMongoClient
import com.mongodb.reactor.client.ReactorMongoCollection
import com.mongodb.reactor.client.ReactorMongoDatabase
import com.mongodb.reactor.client.toReactor
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion
import org.bson.types.ObjectId
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.litote.kmongo.defaultMongoTestVersion
import org.litote.kmongo.reactivestreams.KFlapdoodleReactiveStreamsConfiguration
import org.litote.kmongo.util.KMongoUtil
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * A [org.junit.Rule] to help writing tests for KMongo using [Flapdoodle](http://flapdoodle-oss.github.io/de.flapdoodle.embed.mongo/).
 */
class ReactorFlapdoodleRule<T : Any>(
    val defaultDocumentClass: KClass<T>,
    val generateRandomCollectionName: Boolean = false,
    val dbName: String = "test",
    val version: IFeatureAwareVersion = defaultMongoTestVersion
) : TestRule {

    companion object {

        inline fun <reified T : Any> rule(generateRandomCollectionName: Boolean = false): ReactorFlapdoodleRule<T> =
            ReactorFlapdoodleRule(T::class, generateRandomCollectionName)

        private val versionsMap = ConcurrentHashMap<IFeatureAwareVersion, KFlapdoodleReactiveStreamsConfiguration>()

    }

    private val configuration =
        versionsMap.getOrPut(version) { KFlapdoodleReactiveStreamsConfiguration(version) }
    val mongoClient: ReactorMongoClient = configuration.mongoClient.toReactor()
    val database: ReactorMongoDatabase by lazy {
        mongoClient.getDatabase(dbName)
    }

    inline fun <reified T : Any> getCollection(): ReactorMongoCollection<T> =
        database.getCollection(KMongoUtil.defaultCollectionName(T::class), T::class.java)

    fun <T : Any> getCollection(clazz: KClass<T>): ReactorMongoCollection<T> =
        database.getCollection(KMongoUtil.defaultCollectionName(clazz), clazz.java)

    fun <T : Any> getCollection(name: String, clazz: KClass<T>): ReactorMongoCollection<T> =
        database.getCollection(name, clazz.java)

    inline fun <reified T : Any> dropCollection() = dropCollection(KMongoUtil.defaultCollectionName(T::class))

    fun dropCollection(clazz: KClass<*>) = dropCollection(KMongoUtil.defaultCollectionName(clazz))

    fun dropCollection(collectionName: String) = database.getCollection(collectionName).drop()

    fun <T> ReactorMongoCollection<T>.drop(): Mono<Void> {
        return drop().toMono()
    }

    val col: ReactorMongoCollection<T> by lazy {
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
                    col.drop().toMono().block()
                }
            }
        }
    }

}
