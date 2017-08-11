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
package org.litote.kmongo.async

import com.mongodb.ReadPreference
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class FindOneTest : KMongoAsyncBaseTest<Friend>() {

    @Test
    fun canFindOne() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"),
                { _, _ ->
                    col.findOne("{name:'John'}", {
                        friend, _ ->
                        asyncTest { assertEquals("John", friend!!.name) }
                    })
                })
    }

    @Test
    fun canFindOneBson() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"),
                { _, _ ->
                    col.findOne(Filters.eq("name", "John"), {
                        friend, _ ->
                        asyncTest { assertEquals("John", friend!!.name) }
                    })
                })
    }

    @Test
    fun canFindOneWithEmptyQuery() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"),
                { _, _ ->
                    col.findOne {
                        friend, _ ->
                        asyncTest { assertEquals("John", friend!!.name) }
                    }
                })
    }

    @Test
    fun canFindOneWithObjectId() {
        val john = Friend(ObjectId(), "John")
        col.insertOne(john,
                { _, _ ->
                    col.findOneById(john._id!!, {
                        friend, _ ->
                        asyncTest { assertEquals(john._id, friend!!._id) }
                    })
                })
    }

    @Test
    fun canFindOneWithObjectIdInQuery() {
        val id = ObjectId()
        val john = Friend(id, "John")
        col.insertOne(john,
                { _, _ ->
                    col.findOne("{_id:${id.json}}", {
                        friend, _ ->
                        asyncTest { assertEquals(id, friend!!._id) }
                    })
                })
    }

    @Test
    fun canFindOneWithObjectIdAsString() {
        val id = ObjectId()
        val john = Friend(id, "John")
        col.insertOne(john,
                { _, _ ->
                    col.findOne("{_id:{$oid:'$id'}}", {
                        friend, _ ->
                        asyncTest { assertEquals(id, friend!!._id) }
                    })
                })
    }

    @Test
    fun whenNoResultShouldReturnNull() {
        col.findOne("{_id:'invalid-id'}", {
            r, _ ->
            asyncTest { assertNull(r) }
        })
    }

    @Test
    fun canFindOneWithReadPreference() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"),
                { _, _ ->
                    col.withReadPreference(ReadPreference.primaryPreferred()).findOne("{name:'John'}", {
                        friend, _ ->
                        asyncTest { assertEquals("John", friend!!.name) }
                    })
                })
    }
}