/*
 * Copyright (C) 2016/2020 Litote
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
import org.litote.kmongo.MongoOperator.exists
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class CountTest : KMongoAsyncBaseTest<Friend>() {

    fun newFriend(): Friend {
        return Friend("John", "22 Wall Street Avenue")
    }

    @Test
    fun canCount() {
        col.insertMany(listOf(newFriend(), newFriend())) {
                _, _ ->
            col.count { r, _ ->
                asyncTest {
                    assertEquals(2, r)
                }
            }
        }
    }

    @Test
    fun canCountWithQuery() {
        col.insertMany(listOf(newFriend(), newFriend())) {
                _, _ ->
            col.count("{name:{$exists:true}}") { r, _ ->
                asyncTest {
                    assertEquals(2, r)
                }
            }
        }
    }

    @Test
    fun canCountWithParameters() {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue"))) {
                _, _ ->
            col.count("{name:'Peter'}}") { r, _ ->
                asyncTest {
                    assertEquals(1, r)
                }
            }
        }
    }

    @Test
    fun canCountDocuments() {
        col.insertMany(listOf(newFriend(), newFriend())) {
                _, _ ->
            col.countDocuments { r, _ ->
                asyncTest {
                    assertEquals(2, r)
                }
            }
        }
    }

    @Test
    fun canCountDocumentsWithQuery() {
        col.insertMany(listOf(newFriend(), newFriend())) {
                _, _ ->
            col.countDocuments("{name:{$exists:true}}") { r, _ ->
                asyncTest {
                    assertEquals(2, r)
                }
            }
        }
    }

    @Test
    fun canCountDocumentsWithParameters() {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue"))) {
                _, _ ->
            col.countDocuments("{name:'Peter'}}") { r, _ ->
                asyncTest {
                    assertEquals(1, r)
                }
            }
        }
    }

}