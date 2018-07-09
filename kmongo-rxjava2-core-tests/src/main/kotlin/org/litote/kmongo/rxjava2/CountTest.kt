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
package org.litote.kmongo.rxjava2

import org.junit.Test
import org.litote.kmongo.MongoOperator.exists
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class CountTest : KMongoRxBaseTest<Friend>() {

    fun newFriend(): Friend {
        return Friend("John", "22 Wall Street Avenue")
    }

    @Test
    fun canCount() {
        col.insertMany(listOf(newFriend(), newFriend())).blockingAwait()
        val count = col.count().blockingGet()
        assertEquals(2, count)
    }

    @Test
    fun canCountWithQuery() {
        col.insertMany(listOf(newFriend(), newFriend())).blockingAwait()
        val count = col.count("{name:{$exists:true}}").blockingGet()
        assertEquals(2, count)
    }

    @Test
    fun canCountWithParameters() {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue"))).blockingAwait()
        val count = col.count("{name:'Peter'}}").blockingGet()
        assertEquals(1, count)
    }

    @Test
    fun canCountDocuments() {
        col.insertMany(listOf(newFriend(), newFriend())).blockingAwait()
        val count = col.countDocuments().blockingGet()
        assertEquals(2, count)
    }

    @Test
    fun canCountDocumentsWithQuery() {
        col.insertMany(listOf(newFriend(), newFriend())).blockingAwait()
        val count = col.countDocuments("{name:{$exists:true}}").blockingGet()
        assertEquals(2, count)
    }

    @Test
    fun canCountDocumentsWithParameters() {
        col.insertMany(listOf(newFriend(), Friend("Peter", "22 Wall Street Avenue"))).blockingAwait()
        val count = col.countDocuments("{name:'Peter'}}").blockingGet()
        assertEquals(1, count)
    }

}