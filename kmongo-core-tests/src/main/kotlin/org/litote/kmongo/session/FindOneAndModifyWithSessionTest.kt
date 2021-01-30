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
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneAndUpdate
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class FindOneAndModifySessionTest : AllCategoriesKMongoBaseTest<Friend>() {

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
    fun canFindAndUpdateOne() {
        col.insertOne(session, Friend("John", "22 Wall Street Avenue"))
        col.findOneAndUpdate(session, "{name:'John'}", "{$set: {address: 'A better place'}}")
        val friend = col.findOne(session, "{name:'John'}")
        assertEquals("John", friend!!.name)
        assertEquals("A better place", friend.address)
    }

}
