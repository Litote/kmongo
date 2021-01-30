/*
 * Copyright (C) 2016/2021 Litote
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.MongoOperator.exists
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.MongoOperator.unset
import org.litote.kmongo.countDocuments
import org.litote.kmongo.findOne
import org.litote.kmongo.model.Friend
import org.litote.kmongo.updateMany
import org.litote.kmongo.updateOne
import org.litote.kmongo.updateOneById
import kotlin.test.assertEquals

/**
 *
 */
class UpdateWithSessionTest : AllCategoriesKMongoBaseTest<Friend>() {

    lateinit var session: ClientSession

    @Before
    fun setup() {
        session = mongoClient.startSession()
    }

    @After
    fun tearDown() {
        session.close()
    }

    @Test
    fun canUpdate() {
        val friend = Friend("Paul")
        col.insertOne(session, friend)
        col.updateOne(session, "{name:'Paul'}", "{$set:{name:'John'}}")
        val r = col.findOne(session, "{name:'John'}")
        assertEquals("John", r!!.name)
    }

    @Test
    fun canUpdateTheSameDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(session, friend)
        friend.name = "Johnny"
        col.updateOne(session, friend)
        val r = col.findOne(session, "{name:'Johnny'}")
        assertEquals("Johnny", r!!.name)
        assertEquals("123 Wall Street", r.address)
        assertEquals(friend._id, r._id)
    }

    @Test
    fun canUpdateMulti() {
        col.insertMany(session, listOf(Friend("John"), Friend("John")))
        col.updateMany(session, "{name:'John'}", "{$unset:{name:1}}")
        val c = col.countDocuments(session, "{name:{$exists:true}}")
        assertEquals(0, c)
    }

    @Test
    fun canUpdateByObjectId() {
        val friend = Friend("Paul")
        col.insertOne(session, friend)
        col.updateOneById(session, friend._id!!, "{$set:{name:'John'}}")
        val r = col.findOne(session, "{name:'John'}")
        assertEquals("John", r!!.name)
        assertEquals(friend._id, r._id)
    }
}