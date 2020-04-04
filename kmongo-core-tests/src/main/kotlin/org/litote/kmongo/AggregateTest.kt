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

package org.litote.kmongo

import com.mongodb.MongoCommandException
import com.mongodb.client.MongoCollection
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.AggregateTest.Article
import org.litote.kmongo.MongoOperator.and
import org.litote.kmongo.MongoOperator.avg
import org.litote.kmongo.MongoOperator.cond
import org.litote.kmongo.MongoOperator.group
import org.litote.kmongo.MongoOperator.limit
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.project
import org.litote.kmongo.MongoOperator.push
import org.litote.kmongo.MongoOperator.sort
import org.litote.kmongo.MongoOperator.sum
import org.litote.kmongo.MongoOperator.year
import org.litote.kmongo.model.Friend
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class AggregateTest : AllCategoriesKMongoBaseTest<Article>() {

    @Serializable
    data class Article(
        val title: String,
        val author: String,
        val tags: List<String>,
        @ContextualSerialization
        val date: Instant = Instant.now(),
        val count: Int = 1,
        val ok: Boolean = true
    ) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    @Serializable
    private data class Result(
        @SerialName("_id")
        @BsonId val title: String,
        val averageYear: Double = 0.0,
        val count: Int = 0,
        val friends: List<Friend> = emptyList()
    )

    lateinit var friendCol: MongoCollection<Friend>

    @Before
    fun setup() {
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
        dropCollection<Friend>()
    }

    @Test
    fun canAggregate() {
        val l = col.aggregate<Article>("{$match:{}}").toList()
        assertEquals(3, l.size)
    }


    @Test
    fun canAggregateWithMultipleDocuments() {
        val l = col.aggregate<Article>("{$match:{tags:'virus'}}").toList()
        assertEquals(2, l.size)
        assertTrue(l.all { it.tags.contains("virus") })
    }

    @Test
    fun canAggregateParameters() {
        val tag = "pandemic"
        val l = col.aggregate<Article>("{$match:{tags:'$tag'}}").toList()
        assertEquals(1, l.size)
        assertEquals("World War Z", l.first().title)
    }

    @Test
    fun canAggregateWithManyMatch() {
        val l = col.aggregate<Article>("{$match:{$and:[{tags:'virus'}, {tags:'pandemic'}]}}").toList()
        assertEquals(1, l.size)
        assertEquals("World War Z", l.first().title)
    }

    @Test
    fun canAggregateWithManyOperators() {
        val l = col.aggregate<Article>("[{$match:{tags:'virus'}},{$limit:1}]").toList()
        assertEquals(1, l.size)
    }

    @Test
    fun shouldCheckIfCommandHasErrors() {
        try {
            col.aggregate<Article>("{\$invalid:{}}").toList()
        } catch (e: Exception) {
            assertTrue(e is MongoCommandException)
            return
        }

        fail("should have exception")
    }

    @Test
    fun shouldPopulateIds() {
        val l = friendCol.aggregate<Friend>("{$project: {_id: '\$_id', name: '\$name'}}").toList()
        assertEquals(3, l.size)
        assertTrue(l.all { it._id != null })
        assertTrue(l.all { it.name != null })
        assertTrue(l.all { it.address == null })

        val l2 = friendCol.aggregate<Friend>("{$project: {_id: 1, name: 1}}").toList()
        assertEquals(3, l2.size)
        assertTrue(l2.all { it._id != null })
        assertTrue(l2.all { it.name != null })
        assertTrue(l2.all { it.address == null })

        val l3 = friendCol.aggregate<Friend>("{$project: { name: 1}}").toList()
        assertEquals(3, l3.size)
        assertTrue(l3.all { it._id != null })
        assertTrue(l3.all { it.name != null })
        assertTrue(l3.all { it.address == null })

        val l4 = friendCol.aggregate<Friend>("{$project: {_id: 0, name: 1}}").toList()
        assertEquals(3, l4.size)
        assertTrue(l4.all { it._id == null })
        assertTrue(l4.all { it.name != null })
        assertTrue(l4.all { it.address == null })
    }

    @Test
    fun `use push in group`() {
        val r = col.aggregate<Result>(
            "{$match:{tags:'virus'}}",
            "{$group:{_id:\"\$title\",friends:{$push:{name:\"\$author\"}}}}",
            "{$sort:{title:-1}}"
        )
            .toList()
        assertEquals(
            2, r.size
        )
        assertEquals("Max Brooks", r[0].friends[0].name)
        assertEquals("Kirsty Mckay", r[1].friends[0].name)
        assertEquals("World War Z", r[0].title)
        assertEquals("Zombie Panic", r[1].title)
    }

    @Test
    fun `can aggregate complex queries and deserialize in object`() {
        val r = col.aggregate<Result>(
            """{"$match": {"tags": "virus"}}""",
            """{"$project": {"title": "$ title", "ok": {"$cond": ["$ ok", 1, 0]}, "averageYear": {"$year": "$ date"}}}""".formatJson(),
            """{"$group": {"_id": "$ title", "count": {"$sum": "$ ok"}, "averageYear": {"$avg": "$ averageYear"}}}""".formatJson(),
            """{"$sort": {"_id": 1}}"""
        )
            .toList()

        assertEquals(2, r.size)
        assertEquals(
            Result("World War Z", LocalDate.now().year.toDouble(), 1),
            r.first()
        )
        assertEquals(
            Result("Zombie Panic", LocalDate.now().year.toDouble(), 1),
            r.last()
        )
    }

    @Test
    fun `can aggregate complex queries and deserialize in object2`() {
        val r = col.aggregate<Result>(
            """[{"$match": {"tags": "virus"}},
            {"$project": {"title": "$ title", "ok": {"$cond": ["$ ok", 1, 0]}, "averageYear": {"$year": "$ date"}}},
            {"$group": {"_id": "$ title", "count": {"$sum": "$ ok"}, "averageYear": {"$avg": "$ averageYear"}}},
            {"$sort": {"_id": 1}}]""".formatJson()
        )
            .toList()

        assertEquals(2, r.size)
        assertEquals(
            Result("World War Z", LocalDate.now().year.toDouble(), 1),
            r.first()
        )
        assertEquals(
            Result("Zombie Panic", LocalDate.now().year.toDouble(), 1),
            r.last()
        )
    }

}
