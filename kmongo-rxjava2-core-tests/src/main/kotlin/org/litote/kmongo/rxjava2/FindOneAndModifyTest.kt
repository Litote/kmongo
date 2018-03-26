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

package org.litote.kmongo.rxjava2

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument.AFTER
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
class FindOneAndModifyTest : KMongoRxBaseTest<Friend>() {

    @Test
    fun canFindAndUpdateOne() {
        col.insertOne(Friend("John", "22 Wall Street Avenue")).blockingAwait()
        col.findOneAndUpdate("{name:'John'}", "{$set: {address: 'A better place'}}").blockingGet() ?: throw AssertionError("Value cannot null!")
        val savedFriend = col.findOne("{name:'John'}").blockingGet() ?: throw AssertionError("Value cannot null!")
        assertEquals("John", savedFriend.name)
        assertEquals("A better place", savedFriend.address)
    }

    @Test
    fun canFindAndUpdateWithNullValue() {
        col.insertOne(Friend("John", "22 Wall Street Avenue")).blockingAwait()
        col.findOneAndUpdate("{name:'John'}", "{$set: {address: null}}").blockingGet() ?: throw AssertionError("Value cannot null!")
        val friend = col.findOne("{name:'John'}").blockingGet() ?: throw AssertionError("Value cannot null!")
        assertEquals("John", friend.name)
        assertNull(friend.address)
    }

    @Test
    fun canFindAndUpdateWithDocument() {
        val col2 = col.withDocumentClass<Document>()
        col.insertOne(Friend("John", "22 Wall Street Avenue")).blockingAwait()
        col2.findOneAndUpdate("{name:'John'}", "{$set: {address: 'A better place'}}").blockingGet() ?: throw AssertionError("Value cannot null!")
        val friend = col2.findOne("{name:'John'}").blockingGet() ?: throw AssertionError("Value cannot null!")
        assertEquals("John", friend["name"])
        assertEquals("A better place", friend["address"])
    }

    @Test
    fun canUpsertByObjectId() {
        val expected = Friend(ObjectId(), "John")
        val friend = col.findOneAndUpdate(
                "{_id:${expected._id!!.json}}",
                "{$setOnInsert: {name: 'John'}}",
                FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER)).blockingGet() ?: throw AssertionError("Value cannot null!")

        assertEquals(expected, friend)
    }

    @Test
    fun canUpsertByStringId() {
        val expected = ExposableFriend(ObjectId().toString(), "John")

        val friend = col.withDocumentClass<ExposableFriend>().findOneAndUpdate(
                "{_id:${expected._id.json}}",
                "{$setOnInsert: {name: 'John'}}",
                FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER)).blockingGet()
        assertEquals(expected, friend)
    }
}