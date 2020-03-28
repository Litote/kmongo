/*
 * Copyright (C) 2016/2020 Litote
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

import com.mongodb.MongoCommandException
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.serialization.Serializable
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.MongoOperator.*
import org.litote.kmongo.model.Friend
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AggregateTest : KMongoRxBaseTest<AggregateTest.Article>() {

    @Serializable
    data class Article(val title: String, val author: String, val tags: List<String>) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    lateinit var friendCol: MongoCollection<Friend>

    @Before
    fun setup() {

        col.insertOne(Article("Zombie Panic", "Kirsty Mckay", "horror", "virus")).blockingAwait()
        col.insertOne(Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead")).blockingAwait()
        col.insertOne(Article("World War Z", "Max Brooks", "horror", "virus", "pandemic")).blockingAwait()

        friendCol = getCollection<Friend>()
        friendCol.insertOne(Friend("William")).blockingAwait()
        friendCol.insertOne(Friend("John")).blockingAwait()
        friendCol.insertOne(Friend("Richard")).blockingAwait()
    }

    @After
    fun tearDown() {
        rule.dropCollection<Friend>().blockingAwait()
    }

    override fun getDefaultCollectionClass(): KClass<Article> = Article::class

    @Test
    fun canAggregate() {
        col.aggregate<Article>("{$match:{}}").toObservable().blockingIterable().toList()
    }

    @Test
    fun canAggregateWithMultipleDocuments() {
        val data = col.aggregate<Article>("{$match:{tags:'virus'}}").toObservable().blockingIterable().toList()
        assertEquals(2, data.size)
        assertTrue(data.all { it.tags.contains("virus") })
    }

    @Test
    fun canAggregateParameters() {
        val tag = "pandemic"
        val data = col.aggregate<Article>("{$match:{tags:'$tag'}}").toObservable().blockingIterable().toList()
        assertEquals(1, data.size)
        assertEquals("World War Z", data.first().title)
    }

    @Test
    fun canAggregateWithManyMatch() {
        val data = col.aggregate<Article>("{$match:{$and:[{tags:'virus'}, {tags:'pandemic'}]}}").toObservable().blockingIterable().toList()
        assertEquals(1, data.size)
        assertEquals("World War Z", data.first().title)
    }

    @Test
    fun canAggregateWithManyOperators() {
        val data = col.aggregate<Article>("[{$match:{tags:'virus'}},{$limit:1}]").toObservable().blockingIterable().toList()
        assertEquals(1, data.size)
    }

    @Test
    fun shouldCheckIfCommandHasErrors() {
        assertFailsWith(MongoCommandException::class) {
            col.aggregate<Article>("{\$invalid:{}}")
                    .toObservable().blockingIterable().toList()

        }
    }

    @Test
    fun shouldPopulateIds() {
        val data = friendCol.aggregate<Friend>("{$project: {_id: '\$_id', name: '\$name'}}")
                .toObservable().blockingIterable().toList()
        assertEquals(3, data.size)
        assertTrue(data.all { it._id != null })
    }

}
