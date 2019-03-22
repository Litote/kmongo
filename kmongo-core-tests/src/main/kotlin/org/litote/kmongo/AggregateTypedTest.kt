/*
 * Copyright (C) 2017 Litote
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
import org.bson.codecs.pojo.annotations.BsonId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.AggregateTypedTest.Article
import org.litote.kmongo.model.Friend
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class AggregateTypedTest : AllCategoriesKMongoBaseTest<Article>() {

    data class Article(
        val title: String,
        val author: String,
        val tags: List<String>,
        val date: Instant = Instant.now(),
        val count: Int = 1,
        val ok: Boolean = true
    ) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    private data class Result(
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
        val l = col.aggregate<Article>(
            match(Article::author eq "Maberry Jonathan")
        ).toList()
        assertEquals(1, l.size)
    }


    @Test
    fun canAggregateWithMultipleDocuments() {
        val l = col.aggregate<Article>(match(Article::tags contains "virus")).toList()
        assertEquals(2, l.size)
        assertTrue(l.all { it.tags.contains("virus") })
    }

    @Test
    fun canAggregateParameters() {
        val tag = "pandemic"
        val l = col.aggregate<Article>(match(Article::tags contains tag)).toList()
        assertEquals(1, l.size)
        assertEquals("World War Z", l.first().title)
    }

    @Test
    fun canAggregateWithManyMatch() {
        val l =
            col.aggregate<Article>(match(Article::tags contains "virus", Article::tags contains "pandemic")).toList()
        assertEquals(1, l.size)
        assertEquals("World War Z", l.first().title)
    }

    @Test
    fun `can aggregate complex queries and deserialize in object`() {

        val r = col.aggregate<Result>(
            match(
                Article::tags contains "virus"
            ),
            project(
                Article::title from Article::title,
                Article::ok from cond(Article::ok, 1, 0),
                Result::averageYear from year(Article::date)
            ),
            group(
                Article::title,
                Result::count sum Article::ok,
                Result::averageYear avg Result::averageYear
            ),
            sort(
                ascending(
                    Result::title
                )
            )
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
    fun canAggregateWithManyOperators() {
        val l = col.aggregate<Article>(match(Article::tags contains "virus"), limit(1)).toList()
        assertEquals(1, l.size)
    }

    @Test
    fun shouldPopulateIds() {
        val l0 = friendCol.aggregate<Friend>(
            project(
                Friend::_id from "\$_id",
                Friend::name from Friend::name
            )
        ).toList()
        assertEquals(3, l0.size)
        assertTrue(l0.all { it._id != null })
        assertTrue(l0.all { it.name != null })
        assertTrue(l0.all { it.address == null })

        val l = friendCol.aggregate<Friend>(
            project(
                Friend::_id to "\$_id",
                Friend::name to Friend::name
            )
        ).toList()
        assertEquals(3, l.size)
        assertTrue(l.all { it._id != null })
        assertTrue(l.all { it.name != null })
        assertTrue(l.all { it.address == null })

        val l2 = friendCol.aggregate<Friend>(project(Friend::_id, Friend::name)).toList()
        assertEquals(3, l2.size)
        assertTrue(l2.all { it._id != null })
        assertTrue(l2.all { it.name != null })
        assertTrue(l2.all { it.address == null })

        val l3 = friendCol.aggregate<Friend>(project(Friend::name)).toList()
        assertEquals(3, l3.size)
        assertTrue(l3.all { it._id != null })
        assertTrue(l3.all { it.name != null })
        assertTrue(l3.all { it.address == null })

        val l4 = friendCol.aggregate<Friend>(project(Friend::_id to false, Friend::name to true)).toList()
        assertEquals(3, l4.size)
        assertTrue(l4.all { it._id == null })
        assertTrue(l4.all { it.name != null })
        assertTrue(l4.all { it.address == null })
    }

    @Test
    fun `use push in group`() {
        val r = col.aggregate<Result>(
            match(
                Article::tags contains "virus"
            ),
            group(
                Article::title, Result::friends.push(Friend::name from Article::author)
            ),
            sort(
                ascending(
                    Result::title
                )
            )
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
}