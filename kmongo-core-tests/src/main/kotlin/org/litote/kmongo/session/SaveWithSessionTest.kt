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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.model.Friend
import org.litote.kmongo.save
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 */
class SaveWithSessionTest: AllCategoriesKMongoBaseTest<Friend>() {

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
    fun testSaveWithNullIdAndUpdate() {
        val friend = Friend("Yoda")
        col.save(session, friend)

        val id = friend._id
        assertNotNull(id)
        assertEquals("Yoda", col.findOne(session)!!.name)
        assertEquals(1, col.countDocuments(session))

        friend.name = "Luke"
        col.save(session, friend)

        assertEquals(1, col.countDocuments(session))
        assertEquals("Luke", col.findOne(session)!!.name)
        assertEquals(id, col.findOne(session)!!._id)
    }
}