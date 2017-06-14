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

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class ReplaceTest : KMongoAsyncBaseTest<Friend>() {

    @Test
    fun canReplaceWithId() = runBlocking {
        val friend = Friend("Peter", "31 rue des Lilas")
        col.insertOne(friend)
        col.replaceOneById(friend._id ?: Any(), Friend("John"))
        val replacedFriend = col.findOne("{name:'John'}}") ?: throw AssertionError("Value must not null!")
        assertEquals("John", replacedFriend.name)
    }

    @Test
    fun canReplaceTheSameDocument() = runBlocking {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend)
        friend.name = "Johnny"

        col.replaceOne(friend)

        val replacedFriend = col.findOne("{name:'Johnny'}") ?: throw AssertionError("Value must not null!")
        assertEquals("Johnny", replacedFriend.name)
        assertEquals("123 Wall Street", replacedFriend.address)
        assertEquals(friend._id, replacedFriend._id)
    }
}