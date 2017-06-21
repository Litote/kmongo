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
import org.litote.kmongo.util.KMongoUtil
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 *
 */
abstract class KMongoAsyncAbstractTest<T : Any> {

    class TestContext {

        val lock = CountDownLatch(1)
        var error: Throwable? = null

        fun test(testToRun: () -> Unit) {
            try {
                testToRun()
            } catch(t: Throwable) {
                error = t
                throw t
            } finally {
                lock.countDown()
            }
        }

        fun waitToComplete() {
            assert(lock.await(10, TimeUnit.SECONDS))
            val err = error
            if (err != null) throw err
        }
    }

    companion object {

        val mongoClient = AsyncTestClient.instance

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
            val count = CountDownLatch(1)
            dropCollection(collectionName, { r, t -> count.countDown() })
            count.await(1, TimeUnit.SECONDS)
        }

        fun dropCollection(collectionName: String, callback: (Void?, Throwable?) -> Unit)
                = database.getCollection(collectionName).drop(callback)


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

    abstract fun getDefaultCollectionClass(): KClass<T>

    fun waitToComplete() = testContext.waitToComplete()

    fun asyncTest(testToRun: () -> Unit) = testContext.test(testToRun)

}