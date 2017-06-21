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

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KClass

/**
 *
 */
abstract class KMongoAbstractTest<T : Any> {

    companion object {

        val mongoClient = TestClient.instance

        lateinit var database: MongoDatabase

        @BeforeClass @JvmStatic fun startMongo() {
            database = mongoClient.getDatabase("test")
        }

        inline fun <reified T : Any> getCollection(): MongoCollection<T>
                = database.getCollection(KMongoUtil.defaultCollectionName(T::class), T::class.java)

        fun <T : Any> getCollection(clazz: KClass<T>): MongoCollection<T>
                = database.getCollection(KMongoUtil.defaultCollectionName(clazz), clazz.java)

        inline fun <reified T : Any> dropCollection()
                = dropCollection(KMongoUtil.defaultCollectionName(T::class))

        fun dropCollection(clazz: KClass<*>)
                = dropCollection(KMongoUtil.defaultCollectionName(clazz))

        fun dropCollection(collectionName: String) {
            database.getCollection(collectionName).drop()
        }
    }

    lateinit var col: MongoCollection<T>

    @Before
    fun before() {
        col = getCollection(getDefaultCollectionClass())
    }

    @After
    fun after() {
        dropCollection(getDefaultCollectionClass())
    }

    abstract fun getDefaultCollectionClass(): KClass<T>
}