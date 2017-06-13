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

import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import org.junit.Before
import org.junit.BeforeClass
import org.litote.kmongo.model.Friend
import org.litote.kmongo.util.KMongoUtil.defaultCollectionName
import kotlin.reflect.KClass

/**
 *
 */
abstract class KMongoAsyncBaseTest<T : Any> {

    companion object {

        val mongoClient = AsyncTestClient.instance

        lateinit var database: MongoDatabase

        @BeforeClass
        @JvmStatic
        fun startMongo() {
            database = mongoClient.getDatabase("test")
        }

        inline fun <reified T : Any> getCollection(): MongoCollection<T>
            = database.getCollection<T>()

        fun <T : Any> getCollection(clazz: KClass<T>): MongoCollection<T>
            = database.getCollection(defaultCollectionName(clazz), clazz.java)

        suspend inline fun <reified T : Any> dropCollection()
            = dropCollection(defaultCollectionName(T::class))

        suspend fun dropCollection(clazz: KClass<*>)
            = dropCollection(defaultCollectionName(clazz))

        suspend fun dropCollection(collectionName: String): Void? {
            return singleResult { database.getCollection(collectionName).drop(it) }
        }
    }

    lateinit var col: MongoCollection<T>

    @Before
    fun before() {
        col = getCollection(getDefaultCollectionClass())
    }

    @Suppress("UNCHECKED_CAST")
    open fun getDefaultCollectionClass(): KClass<T>
        = Friend::class as KClass<T>

}