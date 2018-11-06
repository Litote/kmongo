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

import com.mongodb.client.model.UpdateOptions
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.exists
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.MongoOperator.unset
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class UpdateTest : KMongoCoroutineBaseTest<Friend>() {

    @Test
    fun canUpdateMulti() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("John")))

        col.updateMany("{name:'John'}", "{$unset:{name:1}}")
        val count = col.count("{name:{$exists:true}}")
        assertEquals(0, count)
    }

    @Test
    fun canUpdateByObjectId() = runBlocking {
        val friend = Friend("Paul")
        col.insertOne(friend)
        col.updateOneById(friend._id!!, "{$set:{name:'John'}}")

        val updatedFriend = col.findOne("{name:'John'}") ?: throw AssertionError("Value must not null!")
        assertEquals("John", updatedFriend.name)
        assertEquals(friend._id, updatedFriend._id)
    }

    @Test
    fun canUpsert() = runBlocking {
        col.updateOne("{}", "{$set:{name:'John'}}", UpdateOptions().upsert(true))
        val friend = col.findOne("{name:'John'}") ?: throw AssertionError("Value must not null!")
        assertEquals("John", friend.name)
    }

    @Test
    fun canPartiallyUdpateWithAPreexistingDocument() = runBlocking {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend)
        val preexistingDocument = Friend(_id = friend._id ?: ObjectId(), name = "Johnny")
        col.updateOne("{name:'John'}", preexistingDocument)
        val updatedFriend = col.findOne("{name:'Johnny'}") ?: throw AssertionError("Value must not null!")
        assertEquals("Johnny", updatedFriend.name)
        assertNull(updatedFriend.address)
        assertEquals(friend._id, updatedFriend._id)
    }

    @Test
    fun canPartiallyUdpateWithANewDocument() = runBlocking {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend)
        val newDocument = Friend("Johnny")
        col.updateOne("{name:'John'}", newDocument)
        val updatedFriend = col.findOne("{name:'Johnny'}") ?: throw AssertionError("Value must not null!")
        assertEquals("Johnny", updatedFriend.name)
        assertNull(updatedFriend.address)
    }

    @Test
    fun canUpdateTheSameDocument() = runBlocking {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend)
        friend.name = "Johnny"
        col.updateOne(friend)
        val updatedFriend = col.findOne("{name:'Johnny'}") ?: throw AssertionError("Value must not null!")
        assertEquals("Johnny", updatedFriend.name)
        assertEquals("123 Wall Street", updatedFriend.address)
        assertEquals(friend._id, updatedFriend._id)
    }

}