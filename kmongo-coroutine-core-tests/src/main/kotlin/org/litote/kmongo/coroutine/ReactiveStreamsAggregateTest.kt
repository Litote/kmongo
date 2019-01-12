/*
 * Copyright (C) 2017/2018 Litote
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

import com.mongodb.MongoCommandException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.MongoOperator.and
import org.litote.kmongo.MongoOperator.limit
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.project
import org.litote.kmongo.coroutine.ReactiveStreamsAggregateTest.Article
import org.litote.kmongo.model.Friend
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 *
 */
class ReactiveStreamsAggregateTest : KMongoReactiveStreamsCoroutineBaseTest<Article>() {

    data class Article(val title: String, val author: String, val tags: List<String>) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    lateinit var friendCol: CoroutineCollection<Friend>

    @Before
    fun setup() = runBlocking<Unit> {

        col.insertOne(Article("Zombie Panic", "Kirsty Mckay", "horror", "virus"))
        col.insertOne(Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead"))
        col.insertOne(Article("World War Z", "Max Brooks", "horror", "virus", "pandemic"))

        friendCol = getCollection()
        friendCol.insertOne(Friend("William"))
        friendCol.insertOne(Friend("John"))
        friendCol.insertOne(Friend("Richard"))
    }

    @After
    fun tearDown() {
        rule.dropCollection<Friend>()
    }

    override fun getDefaultCollectionClass(): KClass<Article> = Article::class

    @Test
    fun canAggregate() = runBlocking {
        assertEquals(3, col.aggregate<Article>("{$match:{}}").toList().size)
    }

    @Test
    fun canAggregateWithMultipleDocuments() = runBlocking {
        val data = col.aggregate<Article>("{$match:{tags:'virus'}}").toList()
        assertEquals(2, data.size)
        assertTrue(data.all { it.tags.contains("virus") })
    }

    @Test
    fun canAggregateParameters() = runBlocking {
        val tag = "pandemic"
        val data = col.aggregate<Article>("{$match:{tags:'$tag'}}").toList()
        assertEquals(1, data.size)
        assertEquals("World War Z", data.first().title)
    }

    @Test
    fun canAggregateWithManyMatch() = runBlocking {
        val data =
            col.aggregate<Article>("{$match:{$and:[{tags:'virus'}, {tags:'pandemic'}]}}")
                .toList()
        assertEquals(1, data.size)
        assertEquals("World War Z", data.first().title)
    }

    @Test
    fun canAggregateWithManyOperators() = runBlocking {
        val data =
            col.aggregate<Article>("[{$match:{tags:'virus'}},{$limit:1}]").toList()
        assertEquals(1, data.size)
    }

    @Test
    fun shouldCheckIfCommandHasErrors() {
        assertFailsWith(MongoCommandException::class) {
            runBlocking {
                col.aggregate<Article>("{\$invalid:{}}")
                    .toList()
            }
        }
    }

    @Test
    fun shouldPopulateIds() = runBlocking {
        val data = friendCol.aggregate<Friend>("{$project: {_id: '\$_id', name: '\$name'}}")
            .toList()
        assertEquals(3, data.size)
        assertTrue(data.all { it._id != null })
    }

}
