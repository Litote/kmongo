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

import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.ReturnDocument.AFTER
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.MongoOperator.setOnInsert
import org.litote.kmongo.async.model.ExposableFriend
import org.litote.kmongo.async.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class FindOneAndModifyTest : KMongoAsyncBaseTest() {

    lateinit var col: MongoCollection<Friend>

    @Before
    fun before() {
        col = getCollection<Friend>()
    }

    @After
    fun tearDown() = dropCollection<Friend>()

    @Test
    fun canFindAndUpdateOne() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"),
                { r, t ->
                    col.findOneAndUpdate("{name:'John'}", "{$set: {address: 'A better place'}}", { r, t ->
                        col.findOne("{name:'John'}", {
                            friend, throwable ->
                            asyncTest {
                                assertEquals("John", friend!!.name)
                                assertEquals("A better place", friend.address)
                            }
                        })
                    })
                })
        waitToComplete()
    }

    @Test
    fun canFindAndUpdateWithNullValue() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"),
                { r, t ->
                    col.findOneAndUpdate("{name:'John'}", "{$set: {address: null}}", { r, t ->
                        col.findOne("{name:'John'}", {
                            friend, throwable ->
                            asyncTest {
                                assertEquals("John", friend!!.name)
                                assertNull(friend.address)
                            }
                        })
                    })
                })
        waitToComplete()
    }

    @Test
    fun canFindAndUpdateWithDocument() {
        val col2 = col.withDocumentClass<Document>()
        col.insertOne(Friend("John", "22 Wall Street Avenue"),
                { r, t ->
                    col2.findOneAndUpdate("{name:'John'}", "{$set: {address: 'A better place'}}", { r, t ->
                        col2.findOne("{name:'John'}", {
                            friend, throwable ->
                            asyncTest {
                                assertEquals("John", friend!!.get("name"))
                                assertEquals("A better place", friend.get("address"))
                            }
                        })
                    })
                })
        waitToComplete()
    }

    @Test
    fun canUpsertByObjectId() {
        val expected = Friend(ObjectId(), "John")
        col.findOneAndUpdate(
                "{_id:${expected._id!!.json}}",
                "{$setOnInsert: {name: 'John'}}",
                FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER), { r, t ->
            asyncTest {
                assertEquals(expected, r)
            }

        })
        waitToComplete()
    }

    @Test
    fun canUpsertByStringId() {
        val expected = ExposableFriend(ObjectId().toString(), "John")
        col.withDocumentClass<ExposableFriend>().findOneAndUpdate(
                "{_id:${expected._id.json}}",
                "{$setOnInsert: {name: 'John'}}",
                FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER), { r, t ->
            asyncTest {
                assertEquals(expected, r)
            }

        })
        waitToComplete()
    }
}