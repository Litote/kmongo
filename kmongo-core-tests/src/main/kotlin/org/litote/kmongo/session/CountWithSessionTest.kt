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
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.countDocuments
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class CountWithSessionTest : AllCategoriesKMongoBaseTest<Friend>() {

    lateinit var session: ClientSession

    @Before
    fun setup() {
        session = mongoClient.startSession()
    }

    @After
    fun tearDown() {
        session.close()
    }

    fun newFriend(): Friend {
        return Friend("John", "22 Wall Street Avenue")
    }

    @Test
    fun canCountWithQuery() {
        col.insertMany(session, listOf(newFriend(), newFriend()))
        val c = col.countDocuments(session, "{name:{${MongoOperator.exists}:true}}")
        assertEquals(2, c)
    }
}