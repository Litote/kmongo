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

package org.litote.kmongo.session

import com.mongodb.client.ClientSession
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.model.Friend
import org.litote.kmongo.replaceOne
import org.litote.kmongo.replaceOneById
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class ReplaceWithSessionTest : AllCategoriesKMongoBaseTest<Friend>() {

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
    fun canReplaceWithId() {
        val friend = Friend("Peter", "31 rue des Lilas")
        col.insertOne(session, friend)
        col.replaceOneById(session, friend._id!!, Friend("John"))
        val r = col.findOne(session, "{name:'John'}}")
        assertEquals("John", r!!.name)
        assertNull(r.address)
    }

    @Test
    fun canReplaceWithFilterQuery() {
        val friend = Friend("Peter", "31 rue des Lilas")
        col.insertOne(session, friend)
        col.replaceOne(session, "{name:'Peter'}}", Friend(ObjectId(), "John"))
        val r = col.findOne(session, "{name:'John'}}")
        assertEquals("John", r!!.name)
        assertNull(r.address)
    }

}