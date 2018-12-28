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
package org.litote.kmongo.coroutine

import com.mongodb.ReadPreference
import com.mongodb.client.model.Filters
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class FindOneTest : KMongoCoroutineBaseTest<Friend>() {

    @Test
    fun canFindOne() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        val friend = col.findOne("{name:'John'}") ?: throw AssertionError("Value must not null!")
        assertEquals("John", friend.name)
    }

    @Test
    fun `can find one by string filter in ClientSession`() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        rule.mongoClient.startSession().use {
            val friend = col.findOne(it, "{name:'John'}") ?: throw AssertionError("Value must not null!")
            assertEquals("John", friend.name)
        }
    }

    @Test
    fun canFindOneBson() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        val friend = col.findOne(Filters.eq("name", "John")) ?: throw AssertionError("Value must not null!")
        assertEquals("John", friend.name)
    }

    @Test
    fun `can find one by bson filter in ClientSession`() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        rule.mongoClient.startSession().use {
            val friend = col.findOne(it, Filters.eq("name", "John")) ?: throw AssertionError("Value must not null!")
            assertEquals("John", friend.name)
        }
    }

    @Test
    fun canFindOneWithEmptyQuery() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        val friend = col.findOne() ?: throw AssertionError("Value must not null!")
        assertEquals("John", friend.name)
    }

    @Test
    fun canFindOneWithObjectId() = runBlocking {
        val john = Friend(ObjectId(), "John")
        col.insertOne(john)
        val friend = col.findOneById(john._id ?: Any()) ?: throw AssertionError("Value must not null!")
        assertEquals(john._id, friend._id)
    }

    @Test
    fun `can find one by Object Id in clientSession`() = runBlocking {
        val john = Friend(ObjectId(), "John")
        rule.mongoClient.startSession().use {
            col.insertOne(john)
            val friend = col.findOneById(john._id ?: Any(), it) ?: throw AssertionError("Value must not null!")
            assertEquals(john._id, friend._id)
        }
    }


    @Test
    fun canFindOneWithObjectIdInQuery() = runBlocking {
        val id = ObjectId()
        val john = Friend(id, "John")
        col.insertOne(john)
        val friend = col.findOne("{_id:${id.json}}") ?: throw AssertionError("Value must not null!")
        assertEquals(id, friend._id)
    }

    @Test
    fun canFindOneWithObjectIdAsString() = runBlocking {
        val id = ObjectId()
        val john = Friend(id, "John")
        col.insertOne(john)
        val friend = col.findOne("{_id:{$oid:'$id'}}") ?: throw AssertionError("Value must not null!")
        assertEquals(id, friend._id)
    }

    @Test
    fun whenNoResultShouldReturnNull() = runBlocking {
        val friend = col.findOne("{_id:'invalid-id'}")
        assertNull(friend)
    }

    @Test
    fun canFindOneWithReadPreference() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        val friend = col.withReadPreference(ReadPreference.primaryPreferred()).findOne("{name:'John'}") ?: throw AssertionError("Value must not null!")
        assertEquals("John", friend.name)
    }
}