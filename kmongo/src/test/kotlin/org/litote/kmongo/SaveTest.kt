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

package org.litote.kmongo

import org.bson.types.ObjectId
import org.junit.After
import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 */
class SaveTest : KMongoBaseTest<Friend>() {

    data class ClassWithoutId(var name: String)

    @After
    fun tearDown() {
        super.after()
        dropCollection<ClassWithoutId>()
    }

    @Test
    fun testSaveWithNullIdAndUpdate() {
        val friend = Friend("Yoda")
        col.save(friend)

        val id = friend._id
        assertNotNull(id)
        assertEquals("Yoda", col.findOne()!!.name)
        assertEquals(1, col.count())

        friend.name = "Luke"
        col.save(friend)

        assertEquals(1, col.count())
        assertEquals("Luke", col.findOne()!!.name)
        assertEquals(id, col.findOne()!!._id)
    }

    @Test
    fun testSaveWithNotNullIdAndUpdate() {
        val id = ObjectId()
        val friend = Friend(id, "Yoda")
        col.save(friend)

        assertEquals(id, col.findOne()!!._id)
        assertEquals("Yoda", col.findOne()!!.name)
        assertEquals(1, col.count())

        friend.name = "Luke"
        col.save(friend)

        assertEquals(1, col.count())
        assertEquals("Luke", col.findOne()!!.name)
        assertEquals(id, col.findOne()!!._id)
    }

    @Test
    fun testSaveWithNonExistentId() {
        val instance = ClassWithoutId("A")
        val colWithoutId = database.getCollection<ClassWithoutId>()

        colWithoutId.save(instance)
        assertEquals(1, colWithoutId.count())

        colWithoutId.save(instance)
        assertEquals(2, colWithoutId.count())
    }
}