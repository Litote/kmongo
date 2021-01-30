/*
 * Copyright (C) 2016/2021 Litote
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

package org.litote.kmongo.reactivestreams

import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class DistinctTest : KMongoReactiveStreamsBaseTest<Friend>() {

    val wallStreetAvenue = "22 Wall Street Avenue"

    @Test
    fun distinctOnStringEntities() {
        col.insertMany(
            listOf(
                Friend("John", wallStreetAvenue),
                Friend("Smith", wallStreetAvenue),
                Friend("Peter", "24 Wall Street Avenue")
            )
        ).forEach { _, _ ->
            col.distinct<String>("address").listenList { r, _ ->
                asyncTest {
                    assertEquals(2, r!!.size)
                    assertTrue(r.contains(wallStreetAvenue) && r.contains("24 Wall Street Avenue"))
                }
            }
        }
    }

}