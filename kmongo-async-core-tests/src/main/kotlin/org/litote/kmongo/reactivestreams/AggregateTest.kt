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

import com.mongodb.reactivestreams.client.MongoCollection
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.model.Friend
import org.litote.kmongo.reactivestreams.AggregateTest.Article
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class AggregateTest : KMongoReactiveStreamsBaseTest<Article>() {

    data class Article(val title: String, val author: String, val tags: List<String>) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    lateinit var friendCol: MongoCollection<Friend>

    @Before
    fun setup() {
        val count = CountDownLatch(6)

        col.insertOne(Article("Zombie Panic", "Kirsty Mckay", "horror", "virus")).forEach { _, _ -> count.countDown() }
        col.insertOne(Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead"))
            .forEach { _, _ -> count.countDown() }
        col.insertOne(
            Article("World War Z", "Max Brooks", "horror", "virus", "pandemic")
        ).forEach { _, _ -> count.countDown() }

        friendCol = getCollection<Friend>()
        friendCol.insertOne(Friend("William")).forEach { _, _ -> count.countDown() }
        friendCol.insertOne(Friend("John")).forEach { _, _ -> count.countDown() }
        friendCol.insertOne(Friend("Richard")).forEach { _, _ -> count.countDown() }

        count.await(20, TimeUnit.SECONDS)
    }

    @After
    fun tearDown() {
        dropCollection<Friend>()
    }

    @Test
    fun canAggregate() {
        col.aggregate<Article>("{$match:{}}")
            .listenList { l, _ ->
                asyncTest { assertEquals(3, l!!.size) }
            }
    }

}
