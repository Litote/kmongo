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

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument.AFTER
import kotlinx.coroutines.experimental.runBlocking
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.MongoOperator.setOnInsert
import org.litote.kmongo.model.ExposableFriend
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class FindOneAndModifyTest : KMongoAsyncBaseTest<Friend>() {

    @Test
    fun canFindAndUpdateOne() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col.findOneAndUpdate("{name:'John'}", "{$set: {address: 'A better place'}}") ?: throw AssertionError("Value cannot null!")
        val savedFriend = col.findOne("{name:'John'}") ?: throw AssertionError("Value cannot null!")
        assertEquals("John", savedFriend.name)
        assertEquals("A better place", savedFriend.address)
    }

    @Test
    fun canFindAndUpdateWithNullValue() = runBlocking {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col.findOneAndUpdate("{name:'John'}", "{$set: {address: null}}") ?: throw AssertionError("Value cannot null!")
        val friend = col.findOne("{name:'John'}") ?: throw AssertionError("Value cannot null!")
        assertEquals("John", friend.name)
        assertNull(friend.address)
    }

    @Test
    fun canFindAndUpdateWithDocument() = runBlocking {
        val col2 = col.withDocumentClass<Document>()
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col2.findOneAndUpdate("{name:'John'}", "{$set: {address: 'A better place'}}") ?: throw AssertionError("Value cannot null!")
        val friend = col2.findOne("{name:'John'}") ?: throw AssertionError("Value cannot null!")
        assertEquals("John", friend["name"])
        assertEquals("A better place", friend["address"])
    }

    @Test
    fun canUpsertByObjectId() = runBlocking {
        val expected = Friend(ObjectId(), "John")
        val friend = col.findOneAndUpdate(
                "{_id:${expected._id!!.json}}",
                "{$setOnInsert: {name: 'John'}}",
                FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER)) ?: throw AssertionError("Value cannot null!")

        assertEquals(expected, friend)
    }

    @Test
    fun canUpsertByStringId() = runBlocking {
        val expected = ExposableFriend(ObjectId().toString(), "John")

        val friend = col.withDocumentClass<ExposableFriend>().findOneAndUpdate(
                "{_id:${expected._id.json}}",
                "{$setOnInsert: {name: 'John'}}",
                FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER))
        assertEquals(expected, friend)
    }
}