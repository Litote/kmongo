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
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.Binary
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.coroutine.ReactiveStreamsBinaryTest.BinaryFriend
import org.litote.kmongo.json
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 *
 */
class ReactiveStreamsBinaryTest : KMongoReactiveStreamsCoroutineBaseTest<BinaryFriend>() {

    @Serializable
    data class BinaryFriend(@Contextual val _id: Binary, var name: String = "none")

    lateinit var friendId: Binary

    override fun getDefaultCollectionClass(): KClass<BinaryFriend> = BinaryFriend::class

    @Before
    fun setup() {
        friendId = Binary("kmongo".toByteArray())
    }

    @Test
    fun testUpdate() = runBlocking {
        val expectedId = Binary("friend2".toByteArray())
        val expected = BinaryFriend(expectedId, "friend2")

        col.insertOne(expected)
        expected.name = "new friend"

        col.updateOne("{_id:${expectedId.json}}", expected)
        val savedFriend = col.findOne("{_id:${expectedId.json}}")
        assertNotNull(savedFriend)
        assertEquals(expected, savedFriend)
    }

    @Test
    fun testInsert() = runBlocking {
        val expectedId = Binary("friend".toByteArray())
        val expected = BinaryFriend(expectedId, "friend")

        col.insertOne(expected)
        val savedFriend = col.findOne("{_id:${expectedId.json}}")
        assertNotNull(savedFriend)
        assertEquals(expected, savedFriend)
    }

    @Test
    fun testRemove() = runBlocking {
        col.deleteOne("{_id:${friendId.json}}")
        val shouldNull = col.findOne("{_id:${friendId.json}}")
        assertNull(shouldNull)
    }

    @Test
    fun canMarshallBinary() = runBlocking {
        val doc = BinaryFriend(Binary("abcde".toByteArray()))

        col.insertOne(doc)
        val count =
            col.countDocuments("{'_id' : { ${MongoOperator.binary} : 'YWJjZGU=' , ${MongoOperator.type} : '0'}}")
        val savedDoc = col.findOne("{_id:${doc._id.json}}") ?: throw AssertionError("Must not NUll")

        assertEquals(1, count)
        assertEquals(doc._id.type, savedDoc._id.type)
        Assert.assertArrayEquals(doc._id.data, savedDoc._id.data)
    }

}

