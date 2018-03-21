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

import com.mongodb.client.model.Filters.and
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class FindOneTypedTest : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun canFindOneWithOneProperty() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        val friend = col.findOne(Friend::name eq "John")
        assertEquals("John", friend!!.name)
    }

    @Test
    fun canFindOneWithTwoProperties() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        val friend = col.findOne(Friend::name eq "John", Friend::address eq "22 Wall Street Avenue")
        assertEquals("John", friend!!.name)
    }

    @Test
    fun canFindOneWithLambda() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        val friend = col.findOne {
            and(
                Friend::name eq "John",
                Friend::address eq "22 Wall Street Avenue"
            )
        }
        assertEquals("John", friend!!.name)
    }

    @Test
    fun canFindOneWithObjectIdProperty() {
        val john = Friend(ObjectId(), "John")
        col.insertOne(john)
        val friend = col.findOne(john::_id eq john._id)
        assertEquals(john._id, friend!!._id)
    }

}