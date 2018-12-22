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
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class DeleteTest : KMongoCoroutineBaseTest<Friend>() {

    @Test
    fun canDeleteASpecificDocument() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteOne("{name:'John'}")
        val list = col.find().toList()
        assertEquals(1, list.size)
        assertEquals("Peter", list.first().name)
    }

    @Test
    fun `can delete one in ClientSession`() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        rule.mongoClient.startSession().use {
            col.deleteOne(it, "{name:'John'}")
            val list = col.find(it).toList()
            assertEquals(1, list.size)
            assertEquals("Peter", list.first().name)
        }
    }

    @Test
    fun canDeleteByObjectId() = runBlocking {
        col.insertOne("{ _id:{$oid:'47cc67093475061e3d95369d'}, name:'John'}")
        col.deleteOneById(ObjectId("47cc67093475061e3d95369d"))
        val count = col.count()
        assertEquals(0, count)
    }

    @Test
    fun canRemoveAll() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteMany("{}")
        val count = col.count()
        assertEquals(0, count)
    }

    @Test
    fun `can remove all in ClientSession`() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        rule.mongoClient.startSession().use {
            col.deleteMany(it, "{}")
            val count = col.countDocuments(it)
            assertEquals(0, count)
        }
    }
}