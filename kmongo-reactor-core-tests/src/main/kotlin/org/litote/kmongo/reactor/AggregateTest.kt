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

import com.mongodb.MongoCommandException
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.serialization.Serializable
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.MongoOperator.*
import org.litote.kmongo.model.Friend
import reactor.kotlin.core.publisher.toFlux
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AggregateTest : KMongoReactorBaseTest<AggregateTest.Article>() {

    @Serializable
    data class Article(val title: String, val author: String, val tags: List<String>) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    lateinit var friendCol: MongoCollection<Friend>

    @Before
    fun setup() {

        col.insertOne(Article("Zombie Panic", "Kirsty Mckay", "horror", "virus")).blockLast()
        col.insertOne(Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead")).blockLast()
        col.insertOne(Article("World War Z", "Max Brooks", "horror", "virus", "pandemic")).blockLast()

        friendCol = getCollection()
        friendCol.insertOne(Friend("William")).blockLast()
        friendCol.insertOne(Friend("John")).blockLast()
        friendCol.insertOne(Friend("Richard")).blockLast()
    }

    @After
    fun tearDown() {
        rule.dropCollection<Friend>().blockLast()
    }

    override fun getDefaultCollectionClass(): KClass<Article> = Article::class

    @Test
    fun canAggregate() {
        col.aggregate<Article>("{$match:{}}").toFlux().toIterable()
    }

    @Test
    fun canAggregateWithMultipleDocuments() {
        val data = col.aggregate<Article>("{$match:{tags:'virus'}}").toFlux().toIterable().toList()
        assertEquals(2, data.size)
        assertTrue(data.all { it.tags.contains("virus") })
    }

    @Test
    fun canAggregateParameters() {
        val tag = "pandemic"
        val data = col.aggregate<Article>("{$match:{tags:'$tag'}}").toFlux().toIterable().toList()
        assertEquals(1, data.size)
        assertEquals("World War Z", data.first().title)
    }

    @Test
    fun canAggregateWithManyMatch() {
        val data = col.aggregate<Article>("{$match:{$and:[{tags:'virus'}, {tags:'pandemic'}]}}").toFlux().toIterable().toList()
        assertEquals(1, data.size)
        assertEquals("World War Z", data.first().title)
    }

    @Test
    fun canAggregateWithManyOperators() {
        val data = col.aggregate<Article>("[{$match:{tags:'virus'}},{$limit:1}]").toFlux().toIterable().toList()
        assertEquals(1, data.size)
    }

    @Test
    fun shouldCheckIfCommandHasErrors() {
        assertFailsWith(MongoCommandException::class) {
            col.aggregate<Article>("{\$invalid:{}}")
                .toFlux().toIterable().toList()
        }
    }

    @Test
    fun shouldPopulateIds() {
        val data = friendCol.aggregate<Friend>("{$project: {_id: '\$_id', name: '\$name'}}")
                .toFlux().toIterable().toList()
        assertEquals(3, data.size)
        assertTrue(data.all { it._id != null })
    }

}
