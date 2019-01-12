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

import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.json
import org.litote.kmongo.model.ExposableFriend
import org.litote.kmongo.model.Friend
import org.litote.kmongo.reactivestreams.withDocumentClass
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class ReactiveStreamsFindOneAndModifyTest : KMongoReactiveStreamsCoroutineBaseTest<Friend>() {

    @Test
    fun canFindAndUpdateOne() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col.findOneAndUpdate("{name:'John'}", "{${MongoOperator.set}: {address: 'A better place'}}")
                ?: throw AssertionError("Value cannot null!")
        val savedFriend = col.findOne("{name:'John'}") ?: throw AssertionError("Value cannot null!")
        assertEquals("John", savedFriend.name)
        assertEquals("A better place", savedFriend.address)
    }

    @Test
    fun `can find and update one in ClientSession`() = runBlocking {
        mongoClient.startSession().use {
            col.insertOne(it, Friend("John", "22 Wall Street Avenue"))
            col.findOneAndUpdate(it, "{name:'John'}", "{${MongoOperator.set}: {address: 'A better place'}}")
                    ?: throw AssertionError("Value cannot null!")
            val savedFriend = col.findOne(it, "{name:'John'}") ?: throw AssertionError("Value cannot null!")
            assertEquals("John", savedFriend.name)
            assertEquals("A better place", savedFriend.address)
        }
    }

    @Test
    fun canFindAndUpdateWithNullValue() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col.findOneAndUpdate("{name:'John'}", "{${MongoOperator.set}: {address: null}}")
                ?: throw AssertionError("Value cannot null!")
        val friend = col.findOne("{name:'John'}") ?: throw AssertionError("Value cannot null!")
        assertEquals("John", friend.name)
        assertNull(friend.address)
    }

    @Test
    fun canFindAndUpdateWithDocument() = runBlocking {
        val col2 = col.withDocumentClass<Document>()
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col2.findOneAndUpdate("{name:'John'}", "{${MongoOperator.set}: {address: 'A better place'}}")
                ?: throw AssertionError("Value cannot null!")
        val friend = col2.findOne("{name:'John'}") ?: throw AssertionError("Value cannot null!")
        assertEquals("John", friend["name"])
        assertEquals("A better place", friend["address"])
    }

    @Test
    fun canUpsertByObjectId() = runBlocking {
        val expected = Friend(ObjectId(), "John")
        val friend = col.findOneAndUpdate(
            "{_id:${expected._id!!.json}}",
            "{${MongoOperator.setOnInsert}: {name: 'John'}}",
            FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        ) ?: throw AssertionError("Value cannot null!")

        assertEquals(expected, friend)
    }

    @Test
    fun canUpsertByStringId() = runBlocking {
        val expected = ExposableFriend(ObjectId().toString(), "John")

        val friend = col.withDocumentClass<ExposableFriend>().findOneAndUpdate(
            "{_id:${expected._id.json}}",
            "{${MongoOperator.setOnInsert}: {name: 'John'}}",
            FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        )
        assertEquals(expected, friend)
    }

    @Test
    fun `can find and delete one`() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col.findOneAndDelete("{name:'John'}")
        val count = col.countDocuments()
        assertEquals(0, count)
    }

    @Test
    fun `can find and delete one in ClientSesison`() = runBlocking {
        mongoClient.startSession().use {
            col.insertOne(it, Friend("John", "22 Wall Street Avenue"))
            col.findOneAndDelete(it, "{name:'John'}")
            val count = col.countDocuments(it)
            assertEquals(0, count)
        }
    }

    @Test
    fun `can find and replace one`() = runBlocking {
        val oldFriend = Friend("John", "22 Wall Street Avenue")
        col.insertOne(oldFriend)
        val newFriend = Friend("Bob", "22 Wall Street Avenue", _id = oldFriend._id)
        col.findOneAndReplace("{name:'John'}", newFriend, FindOneAndReplaceOptions().upsert(true))
        val count = col.countDocuments()
        assertEquals(1, count)
        val countBobs = col.countDocuments("{name:'Bob'}")
        assertEquals(1, countBobs)
    }

    @Test
    fun `can find and replace one in ClientSession`() = runBlocking {
        mongoClient.startSession().use {
            val oldFriend = Friend("John", "22 Wall Street Avenue")
            col.insertOne(it, oldFriend)
            val newFriend = Friend("Bob", "22 Wall Street Avenue", _id = oldFriend._id)
            col.findOneAndReplace(it, "{name:'John'}", newFriend, FindOneAndReplaceOptions().upsert(true))
            val count = col.countDocuments(it)
            assertEquals(1, count)
            val countBobs = col.countDocuments(it, "{name:'Bob'}")
            assertEquals(1, countBobs)
        }
    }
}