/*
 * Copyright (C) 2017/2018 Litote
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

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.litote.kmongo.json
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class ReactiveStreamsBulkWriteTest : KMongoReactiveStreamsCoroutineBaseTest<Friend>() {

    fun newFriend(): Friend {
        return Friend("John", "22 Wall Street Avenue")
    }

    @Test
    fun `can do bulk write`() = runBlocking {
        col.bulkWrite(
            "{ insertOne : { \"document\" : ${newFriend().json} } }",
            "{ insertOne : { \"document\" : ${newFriend().json} } }"
        )
        assertEquals(2, col.countDocumentsAndAwait())
    }

    @Test
    fun `can do bulk write in ClientSession`() = runBlocking {
        rule.mongoClient.startSessionAndAwait().use {
            col.bulkWrite(
                it,
                "{ insertOne : { \"document\" : ${newFriend().json} } }",
                "{ insertOne : { \"document\" : ${newFriend().json} } }"
            )
            assertEquals(2, col.countDocumentsAndAwait(it))
        }
    }
}