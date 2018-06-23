/*
 * Copyright (C) 2017 Litote
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

import com.mongodb.Block
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoIterable
import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertTrue

/**
 *
 */
class MongoIterableTest : AllCategoriesKMongoBaseTest<Friend>() {

    class MongoIterableWrapper<T>(val mongoIterable: MongoIterable<T>) : MongoIterable<T> by mongoIterable {

        var called = false

        override fun forEach(block: Block<in T>) {
            called = true
            mongoIterable.forEach(block)
        }
    }

    @Test
    fun `forEach calls MongoIterable#forEach`() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        val iterable = MongoIterableWrapper(col.find())
        iterable.forEach { }
        assertTrue(iterable.called)
    }
}