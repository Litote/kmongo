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

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.litote.kmongo.MongoOperator.exists
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class CountTest : KMongoCoroutineBaseTest<Friend>() {

    fun newFriend(): Friend {
        return Friend("John", "22 Wall Street Avenue")
    }

    @Test
    fun canCount() = runBlocking {
        col.insertMany(listOf(newFriend(), newFriend()))
        val count = col.count()
        assertEquals(2, count)
    }

    @Test
    fun canCountWithQuery() = runBlocking {
        col.insertMany(listOf(newFriend(), newFriend()))
        val count = col.count("{name:{$exists:true}}")
        assertEquals(2, count)
    }

    @Test
    fun canCountWithParameters() = runBlocking {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue")))
        val count = col.count("{name:'Peter'}}")
        assertEquals(1, count)
    }

    @Test
    fun `can count in ClientSession`() = runBlocking {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue")))
        rule.mongoClient.startSession().use {
            val count = col.count(it)
            assertEquals(2, count)
        }
    }

    @Test
    fun `can count with parameters in ClientSession`() = runBlocking {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue")))
        rule.mongoClient.startSession().use {
            val count = col.count(it, "{name:'Peter'}}")
            assertEquals(1, count)
        }
    }

    @Test
    fun canCountDocuments() = runBlocking {
        col.insertMany(listOf(newFriend(), newFriend()))
        val count = col.countDocuments()
        assertEquals(2, count)
    }

    @Test
    fun canCountDocumentsWithQuery() = runBlocking {
        col.insertMany(listOf(newFriend(), newFriend()))
        val count = col.countDocuments("{name:{$exists:true}}")
        assertEquals(2, count)
    }

    @Test
    fun canCountDocumentsWithParameters() = runBlocking {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue")))
        val count = col.countDocuments("{name:'Peter'}}")
        assertEquals(1, count)
    }

    @Test
    fun `can count documents in ClientSession`() = runBlocking {
        col.insertMany(listOf(newFriend(), newFriend()))
        rule.mongoClient.startSession().use {
            val count = col.countDocuments(it)
            assertEquals(2, count)
        }
    }

    @Test
    fun `can count documents with parameters in ClientSession`() = runBlocking {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue")))
        rule.mongoClient.startSession().use {
            val count = col.countDocuments(it, "{name:'Peter'}}")
            assertEquals(1, count)
        }
    }
}