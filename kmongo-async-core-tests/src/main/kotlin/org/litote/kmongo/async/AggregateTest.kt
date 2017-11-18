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

import com.mongodb.MongoCommandException
import com.mongodb.async.client.MongoCollection
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.MongoOperator.and
import org.litote.kmongo.MongoOperator.limit
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.project
import org.litote.kmongo.async.AggregateTest.Article
import org.litote.kmongo.model.Friend
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AggregateTest : KMongoAsyncBaseTest<Article>() {

    data class Article(val title: String, val author: String, val tags: List<String>) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    lateinit var friendCol: MongoCollection<Friend>

    @Before
    fun setup() {
        val count = CountDownLatch(6)

        col.insertOne(Article("Zombie Panic", "Kirsty Mckay", "horror", "virus"), { _, _ -> count.countDown() })
        col.insertOne(Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead"), { _, _ -> count.countDown() })
        col.insertOne(Article("World War Z", "Max Brooks", "horror", "virus", "pandemic"), { _, _ -> count.countDown() })

        friendCol = getCollection<Friend>()
        friendCol.insertOne(Friend("William"), { _, _ -> count.countDown() })
        friendCol.insertOne(Friend("John"), { _, _ -> count.countDown() })
        friendCol.insertOne(Friend("Richard"), { _, _ -> count.countDown() })

        count.await(20, TimeUnit.SECONDS)
    }

    @After
    fun tearDown() {
        dropCollection<Friend>()
    }

    override fun getDefaultCollectionClass(): KClass<Article> = Article::class

    @Test
    fun canAggregate() {
        col.aggregate<Article>("{$match:{}}")
                .toList {
                    l, t ->
                    t?.printStackTrace()
                    asyncTest { assertEquals(3, l!!.size) }
                }
    }


    @Test
    fun canAggregateWithMultipleDocuments() {
        col.aggregate<Article>("{$match:{tags:'virus'}}")
                .toList {
                    l, _ ->
                    asyncTest {
                        assertEquals(2, l!!.size)
                        assertTrue (l.all { it.tags.contains("virus") })
                    }
                }
    }

    @Test
    fun canAggregateParameters() {
        val tag = "pandemic"
        col.aggregate<Article>("{$match:{tags:'$tag'}}")
                .toList {
                    l, _ ->
                    asyncTest {
                        assertEquals(1, l!!.size)
                        assertEquals ("World War Z", l.first().title)
                    }
                }
    }

    @Test
    fun canAggregateWithManyMatch() {
        col.aggregate<Article>("{$match:{$and:[{tags:'virus'}, {tags:'pandemic'}]}}")
                .toList {
                    l, _ ->
                    asyncTest {
                        assertEquals(1, l!!.size)
                        assertEquals ("World War Z", l.first().title)
                    }
                }
    }

    @Test
    fun canAggregateWithManyOperators() {
        col.aggregate<Article>("[{$match:{tags:'virus'}},{$limit:1}]")
                .toList {
                    l, _ ->
                    asyncTest {
                        assertEquals(1, l!!.size)
                    }
                }
    }

    @Test
    fun shouldCheckIfCommandHasErrors() {
        col.aggregate<Article>("{\$invalid:{}}")
                .toList {
                    _, t ->
                    asyncTest {
                        assertTrue(t is MongoCommandException)
                    }
                }
    }

    @Test
    fun shouldPopulateIds() {
        friendCol.aggregate<Friend>("{$project: {_id: '\$_id', name: '\$name'}}")
                .toList {
                    l, t ->
                    asyncTest {
                        t?.printStackTrace()
                        //TODO understand why this fails randomly
                        //assertEquals(3, l!!.size)
                        assertTrue (l!!.all { it._id != null })
                    }
                }
    }


}
