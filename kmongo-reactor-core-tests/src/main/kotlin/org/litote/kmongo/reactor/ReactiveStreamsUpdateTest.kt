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

import com.mongodb.client.model.UpdateOptions
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class ReactiveStreamsUpdateTest : KMongoReactiveStreamsReactorBaseTest<Friend>() {

    @Test
    fun canUpdateMulti() {
        col.insertMany(listOf(Friend("John"), Friend("John"))).blockLast()

        col.updateMany("{name:'John'}", "{${MongoOperator.unset}:{name:1}}").block()
        val count = col.countDocuments("{name:{${MongoOperator.exists}:true}}").block()
        assertEquals(0, count)
    }

    @Test
    fun canUpdateByObjectId() {
        val friend = Friend("Paul")
        col.insertOne(friend).blockLast()
        col.updateOneById(friend._id!!, "{${MongoOperator.set}:{name:'John'}}").block()

        val updatedFriend = col.findOne("{name:'John'}").block() ?: throw AssertionError("Value must not null!")
        assertEquals("John", updatedFriend.name)
        assertEquals(friend._id, updatedFriend._id)
    }

    @Test
    fun canUpsert() {
        col.updateOne("{}", "{${MongoOperator.set}:{name:'John'}}", UpdateOptions().upsert(true)).block()
        val friend = col.findOne("{name:'John'}").block() ?: throw AssertionError("Value must not null!")
        assertEquals("John", friend.name)
    }

    @Test
    fun canPartiallyUdpateWithAPreexistingDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend).blockLast()
        val preexistingDocument = Friend(_id = friend._id ?: ObjectId(), name = "Johnny")
        col.updateOne("{name:'John'}", preexistingDocument).block()
        val updatedFriend = col.findOne("{name:'Johnny'}").block() ?: throw AssertionError("Value must not null!")
        assertEquals("Johnny", updatedFriend.name)
        assertNull(updatedFriend.address)
        assertEquals(friend._id, updatedFriend._id)
    }

    @Test
    fun canPartiallyUdpateWithANewDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend).blockLast()
        val newDocument = Friend("Johnny")
        col.updateOne("{name:'John'}", newDocument).block()
        val updatedFriend = col.findOne("{name:'Johnny'}").block() ?: throw AssertionError("Value must not null!")
        assertEquals("Johnny", updatedFriend.name)
        assertNull(updatedFriend.address)
    }

    @Test
    fun canUpdateTheSameDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend).blockLast()
        friend.name = "Johnny"
        col.updateOne(friend).block()
        val updatedFriend = col.findOne("{name:'Johnny'}").block() ?: throw AssertionError("Value must not null!")
        assertEquals("Johnny", updatedFriend.name)
        assertEquals("123 Wall Street", updatedFriend.address)
        assertEquals(friend._id, updatedFriend._id)
    }

}
