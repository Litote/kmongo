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
package org.litote.kmongo.async

import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.litote.kmongo.async.model.Friend
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.reflect.KClass

/**
 *
 */
abstract class KMongoAsyncBaseTest<T : Any> {

    companion object {

        val mongoClient = EmbeddedMongo.instance

        lateinit var database: MongoDatabase

        @BeforeClass @JvmStatic fun startMongo() {
            database = mongoClient.getDatabase("test")
        }

        inline fun <reified T : Any> getCollection(): MongoCollection<T>
                = database.getCollection<T>(toCollectionName<T>())

        fun <T : Any> getCollection(clazz: KClass<T>): MongoCollection<T>
                = database.getCollection(toCollectionName(clazz), clazz.java)

        inline fun <reified T : Any> dropCollection()
                = dropCollection { toCollectionName<T>() }

        fun dropCollection(clazz: KClass<*>)
                = dropCollection { -> toCollectionName(clazz) }

        fun dropCollection(nameFunction: () -> String) {
            val count = CountDownLatch(1)
            dropCollection (nameFunction.invoke(), { r, t -> count.countDown() })
            count.await(1, SECONDS)
        }

        fun dropCollection(collectionName: String, callback: (Void?, Throwable?) -> Unit)
                = database.getCollection(collectionName).drop(callback)

        inline fun <reified T : Any> toCollectionName()
                = toCollectionName(T::class)

        fun toCollectionName(clazz: KClass<*>)
                = clazz.simpleName!!.toLowerCase()

    }

    lateinit var testContext: TestContext
    lateinit var col: MongoCollection<T>

    @Before
    fun before() {
        testContext = TestContext()
        col = getCollection(getDefaultCollectionClass())
    }

    @After
    fun after() {
        try {
            waitToComplete()
        } finally {
            dropCollection(getDefaultCollectionClass())
        }
    }

    @Suppress("UNCHECKED_CAST")
    open fun getDefaultCollectionClass(): KClass<T>
            = Friend::class as KClass<T>

    fun waitToComplete() = testContext.waitToComplete()

    fun asyncTest(testToRun: () -> Unit) = testContext.test(testToRun)


}