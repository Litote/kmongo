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

package org.litote.kmongo.coroutine.issues

import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson
import org.junit.Test
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.coroutine.KMongoReactiveStreamsCoroutineBaseTest
import org.litote.kmongo.model.Friend
import org.litote.kmongo.ne
import kotlin.test.assertEquals

/**
 *
 */
class Issue69NullOrEmptyBsonDeleteManyCrash : KMongoReactiveStreamsCoroutineBaseTest<Friend>() {

    @Test
    fun `deleteMany with empty bson does not crash`() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteMany()
        val count = col.countDocuments()
        assertEquals(0, count)
    }

    @Test
    fun `deleteMany with null filter does not crash`() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteMany(null as Bson?)
        val count = col.countDocuments()
        assertEquals(0, count)
    }

    @Test
    fun `deleteMany with no filter does not crash`() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteMany()
        val count = col.countDocuments()
        assertEquals(0, count)
    }

    @Test
    fun `deleteMany with list of no filter does not crash`() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteMany(EMPTY_BSON)
        var count = col.countDocuments()
        assertEquals(0, count)

        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteMany(EMPTY_BSON, EMPTY_BSON)
        count = col.countDocuments()
        assertEquals(0, count)

        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteMany(EMPTY_BSON, EMPTY_BSON, EMPTY_BSON)
        count = col.countDocuments()
        assertEquals(0, count)
    }

    @Test
    fun `deleteMany with list of filter and empty filter does not crash`() = runBlocking {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteMany(Friend::name ne "Paul", EMPTY_BSON)
        val count = col.countDocuments()
        assertEquals(0, count)
    }


}