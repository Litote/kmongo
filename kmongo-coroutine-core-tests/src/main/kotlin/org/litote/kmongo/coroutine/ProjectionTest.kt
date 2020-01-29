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

package org.litote.kmongo.coroutine

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.async.withDocumentClass
import org.litote.kmongo.insertOne
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class ProjectionTest : KMongoCoroutineBaseTest<Friend>() {

    @Serializable
    data class FriendWithNameOnly(val name: String)

    @Test
    fun `projection works as expected`() = runBlocking {
        col.bulkWrite(
            insertOne(Friend("Joe")),
            insertOne(Friend("Bob"))
        )
        val result: Iterable<String> =
            col.withDocumentClass<FriendWithNameOnly>()
                .find()
                .descendingSort(FriendWithNameOnly::name)
                .projection(FriendWithNameOnly::name)
                .map { it.name }
                .toList()

        assertEquals(
            listOf("Joe", "Bob"),
            result
        )
    }
}