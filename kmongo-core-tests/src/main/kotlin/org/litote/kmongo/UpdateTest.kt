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

import com.mongodb.client.model.UpdateOptions
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
class UpdateTest : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun canUpdate() {
        val friend = Friend("Paul")
        col.insertOne(friend)
        col.updateOne("{name:'Paul'}", "{$set:{name:'John'}}")
        val r = col.findOne("{name:'John'}")
        assertEquals("John", r!!.name)
    }

    @Test
    fun canUpdateTheSameDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend)
        friend.name = "Johnny"
        col.updateOne(friend)
        val r = col.findOne("{name:'Johnny'}")
        assertEquals("Johnny", r!!.name)
        assertEquals("123 Wall Street", r.address)
        assertEquals(friend._id, r._id)
    }

    @Test
    fun canUpdateMulti() {
        col.insertMany(listOf(Friend("John"), Friend("John")))
        col.updateMany("{name:'John'}", "{$unset:{name:1}}")
        val c = col.countDocuments("{name:{$exists:true}}")
        assertEquals(0, c)
    }

    @Test
    fun canUpdateByObjectId() {
        val friend = Friend("Paul")
        col.insertOne(friend)
        col.updateOneById(friend._id!!, "{$set:{name:'John'}}")
        val r = col.findOne("{name:'John'}")
        assertEquals("John", r!!.name)
        assertEquals(friend._id, r._id)
    }

    @Test
    fun canUpsert() {
        col.updateOne("{}", "{$set:{name:'John'}}", UpdateOptions().upsert(true))
        val r = col.findOne("{name:'John'}")
        assertEquals("John", r!!.name)
    }

    @Test
    fun canPartiallyUpdateWithAnOtherDocumentWithSameId() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend)
        val preexistingDocument = Friend(friend._id!!, "Johnny")
        col.updateOne("{name:'John'}", preexistingDocument)
        val r = col.findOne("{name:'Johnny'}")
        assertEquals("Johnny", r!!.name)
        assertNull(r.address)
        assertEquals(friend._id, r._id)
    }

    @Test
    fun canPartiallyUpdateWithANewDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend)
        val newDocument = Friend("Johnny")
        col.updateOne("{name:'John'}", newDocument)
        val r = col.findOne("{name:'Johnny'}")
        assertEquals("Johnny", r!!.name)
        assertNull(r.address)
    }

}