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

package org.litote.kmongo.reactor

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.Binary
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.json
import org.litote.kmongo.reactor.ReactiveStreamsBinaryTest.BinaryFriend
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 *
 */
class ReactiveStreamsBinaryTest : KMongoReactiveStreamsReactorBaseTest<BinaryFriend>() {

    @Serializable
    data class BinaryFriend(@Contextual val _id: Binary, var name: String = "none")

    lateinit var friendId: Binary

    override fun getDefaultCollectionClass(): KClass<BinaryFriend> = BinaryFriend::class

    @Before
    fun setup() {
        friendId = Binary("kmongo".toByteArray())
    }

    @Test
    fun testUpdate() {
        val expectedId = Binary("friend2".toByteArray())
        val expected = BinaryFriend(expectedId, "friend2")

        col.insertOne(expected).block()
        expected.name = "new friend"

        col.updateOne("{_id:${expectedId.json}}", expected).block()
        val savedFriend = col.findOne("{_id:${expectedId.json}}").block()
        assertNotNull(savedFriend)
        assertEquals(expected, savedFriend)
    }

    @Test
    fun testInsert() {
        val expectedId = Binary("friend".toByteArray())
        val expected = BinaryFriend(expectedId, "friend")

        col.insertOne(expected).block()
        val savedFriend = col.findOne("{_id:${expectedId.json}}").block()
        assertNotNull(savedFriend)
        assertEquals(expected, savedFriend)
    }

    @Test
    fun testRemove() {
        col.deleteOne("{_id:${friendId.json}}").block()
        val shouldNull = col.findOne("{_id:${friendId.json}}").block()
        assertNull(shouldNull)
    }

    @Test
    fun canMarshallBinary() {
        val doc = BinaryFriend(Binary("abcde".toByteArray()))

        col.insertOne(doc).block()
        val count =
            col.countDocuments("{'_id' : { ${MongoOperator.binary} : 'YWJjZGU=' , ${MongoOperator.type} : '0'}}")
                .block()
        val savedDoc = col.findOne("{_id:${doc._id.json}}").block() ?: throw AssertionError("Must not NUll")

        assertEquals(1, count)
        assertEquals(doc._id.type, savedDoc._id.type)
        Assert.assertArrayEquals(doc._id.data, savedDoc._id.data)
    }

}

