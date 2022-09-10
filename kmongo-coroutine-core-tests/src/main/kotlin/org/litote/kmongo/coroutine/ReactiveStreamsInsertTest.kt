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

package org.litote.kmongo.coroutine

import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.model.Friend
import org.litote.kmongo.util.ObjectMappingConfiguration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 */
class ReactiveStreamsInsertTest : KMongoReactiveStreamsCoroutineBaseTest<Friend>() {

    fun newFriend(): Friend {
        return Friend("John", "22 Wall Street Avenue")
    }

    @Test
    fun `can insert one`() = runBlocking {
        col.insertOne(newFriend())
        assertEquals(1, col.countDocuments())
    }

    @Test
    fun `can insert one in ClientSession`() = runBlocking {
        mongoClient.startSession().use {
            col.insertOne(it, newFriend())
            assertEquals(1, col.countDocuments(it))
        }
    }

    @Test
    fun `can insert one in ClientSession and persists null`() = runBlocking {
        mongoClient.startSession().use {
            col.insertOne(it, newFriend())
            assertEquals(1, col.countDocuments(it))
            val doc = database.getCollection<Document>("friend").findOne()!!

            assertTrue(doc.containsKey("name"))
            if (ObjectMappingConfiguration.serializeNull) {
                assertTrue(doc.containsKey("coordinate"))
            } else {
                assertFalse(doc.containsKey("coordinate"))
            }
        }
    }

    @Test
    fun `can insert many`() = runBlocking {
        col.insertMany(listOf(newFriend(), newFriend()))
        assertEquals(2, col.countDocuments())
    }

    @Test
    fun `can insert many in ClientSession`() = runBlocking {
        mongoClient.startSession().use {
            col.insertMany(it, listOf(newFriend(), newFriend()))
            assertEquals(2, col.countDocuments(it))
        }
    }
}