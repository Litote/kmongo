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

import org.junit.Test
import org.litote.kmongo.MongoOperator.exists
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class CountTest : KMongoBaseTest<Friend>() {

    fun newFriend(): Friend {
        return Friend("John", "22 Wall Street Avenue")
    }

    @Test
    fun canCount() {
        col.insertMany(listOf(newFriend(), newFriend()))
        val c = col.count()
        assertEquals(2, c)
    }

    @Test
    fun canCountWithQuery() {
        col.insertMany(listOf(newFriend(), newFriend()))
        val c = col.count("{name:{$exists:true}}")
        assertEquals(2, c)
    }

    @Test
    fun canCountWithParameters() {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue")))
        val c = col.count("{name:'Peter'}")
        assertEquals(1, c)
    }

}