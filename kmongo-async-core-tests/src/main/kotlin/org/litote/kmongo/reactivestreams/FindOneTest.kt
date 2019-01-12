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
package org.litote.kmongo.reactivestreams

import com.mongodb.client.model.Filters
import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class FindOneTest : KMongoReactiveStreamsBaseTest<Friend>() {

    @Test
    fun canFindOne() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
            .forEach { _, _ ->
                col.findOne("{name:'John'}").forEach { friend, _ ->
                    asyncTest { assertEquals("John", friend!!.name) }
                }
            }
    }

    @Test
    fun canFindOneBson() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
            .forEach { _, _ ->
                col.findOne(Filters.eq("name", "John")).forEach { friend, _ ->
                    asyncTest { assertEquals("John", friend!!.name) }
                }
            }
    }
}