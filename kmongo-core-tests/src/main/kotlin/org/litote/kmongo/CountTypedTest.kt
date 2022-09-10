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

package org.litote.kmongo

import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class CountTypedTest : AllCategoriesKMongoBaseTest<Friend>() {

    fun newFriend(): Friend {
        return Friend("John", "22 Wall Street Avenue")
    }

    @Test
    fun canCountWithQueryProperty() {
        col.insertMany(listOf(newFriend(), newFriend()))
        var c = col.countDocuments(Friend::name exists true)
        assertEquals(2, c)
        c = col.countDocuments(Friend::name.exists())
        assertEquals(2, c)
    }

    @Test
    fun canCountWithParametersProperty() {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue")))
        val c = col.countDocuments(Friend::name eq "Peter")
        assertEquals(1, c)
    }

}