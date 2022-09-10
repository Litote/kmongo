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
class BulkWriteTypedTest : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun testBulkWrite() {
        val friend = Friend("John", "22 Wall Street Avenue")
        with(friend) {
            val result =
                col.bulkWrite(
                    insertOne(friend),
                    updateOne(
                        ::name eq "Fred",
                        setValue(::address, "221B Baker Street"),
                        upsert()
                    ),
                    updateMany(
                        EMPTY_BSON,
                        setValue(::address, "nowhere")
                    ),
                    replaceOne(
                        ::name eq "Max",
                        Friend("Joe"),
                        replaceUpsert()
                    ),
                    deleteOne(::name eq "Max"),
                    deleteMany(EMPTY_BSON)
                )
            assertEquals(1, result.insertedCount)
            assertEquals(2, result.matchedCount)
            assertEquals(3, result.deletedCount)
            assertEquals(2, result.modifiedCount)
            assertEquals(2, result.upserts.size)
            assertEquals(0, col.countDocuments())
        }
    }

}