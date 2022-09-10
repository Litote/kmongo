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

package org.litote.kmongo.session

import com.mongodb.client.ClientSession
import com.mongodb.client.MongoCollection
import kotlinx.serialization.Serializable
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.aggregate
import org.litote.kmongo.model.Friend
import org.litote.kmongo.session.AggregateWithSessionTest.Article
import kotlin.test.assertEquals

class AggregateWithSessionTest : AllCategoriesKMongoBaseTest<Article>() {

    @Serializable
    data class Article(val title: String, val author: String, val tags: List<String>) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    lateinit var friendCol: MongoCollection<Friend>
    lateinit var session: ClientSession

    @Before
    fun setup() {
        session = mongoClient.startSession()
        col.insertOne(session, Article("Zombie Panic", "Kirsty Mckay", "horror", "virus"))
        col.insertOne(session, Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead"))
        col.insertOne(session, Article("World War Z", "Max Brooks", "horror", "virus", "pandemic"))

        friendCol = getCollection()
        friendCol.insertOne(session, Friend("William"))
        friendCol.insertOne(session, Friend("John"))
        friendCol.insertOne(session, Friend("Richard"))
    }

    @After
    fun tearDown() {
        session.close()
        dropCollection<Friend>()
    }

    @Test
    fun canAggregate() {
        val l = col.aggregate<Article>(session, "{$match:{}}").toList()
        assertEquals(3, l.size)
    }

}
