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

package org.litote.kmongo.async

import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class ReplaceTest : KMongoAsyncBaseTest<Friend>() {

    @Test
    fun canReplaceWithId() {
        val friend = Friend("Peter", "31 rue des Lilas")
        col.insertOne(friend, { r, t ->
            col.replaceOne(friend._id!!, Friend("John"), { r, t ->
                col.findOne("{name:'John'}}", { r, t ->
                    asyncTest {
                        assertEquals("John", r!!.name)
                        assertNull(r.address)
                    }
                })
            })
        })
    }

    @Test
    fun canReplaceTheSameDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend, { r, t ->
            friend.name = "Johnny"
            col.replaceOne(friend, { r, t ->
                col.findOne("{name:'Johnny'}", { r, t ->
                    asyncTest {
                        assertEquals("Johnny", r!!.name)
                        assertEquals("123 Wall Street", r.address)
                        assertEquals(friend._id, r._id)
                    }
                })
            })
        })
    }
}