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

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument.AFTER
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.MongoOperator.setOnInsert
import org.litote.kmongo.MongoOperator.inc
import org.litote.kmongo.model.Coordinate
import org.litote.kmongo.model.ExposableFriend
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class FindOneAndModifyTest : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun canFindAndUpdateOne() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col.findOneAndUpdate("{name:'John'}", "{$set: {address: 'A better place'}}")
        val friend = col.findOne("{name:'John'}")
        assertEquals("John", friend!!.name)
        assertEquals("A better place", friend.address)
    }

    @Test
    fun canFindAndUpdateWithNullValue() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col.findOneAndUpdate("{name:'John'}", "{$set: {address: null}}")
        val friend = col.findOne("{name:'John'}")
        assertEquals("John", friend!!.name)
        assertNull(friend.address)
    }

    @Test
    fun canFindAndIncrement() {
        col.insertOne(Friend("John", coordinate = Coordinate(1,1)))
        col.findOneAndUpdate("{name:'John'}", "{$inc: {'coordinate.lat': 1}}")
        val friend = col.findOne("{name:'John'}")
        assertEquals("John", friend!!.name)
        assertEquals(2, friend.coordinate?.lat)
    }

    @Test
    fun canFindAndUpdateWithDocument() {
        val col2 = col.withDocumentClass<Document>()
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col2.findOneAndUpdate("{name:'John'}", "{$set: {address: 'A better place'}}")
        val friend = col2.findOne("{name:'John'}")
        assertEquals("John", friend!!.get("name"))
        assertEquals("A better place", friend.get("address"))
    }

    @Test
    fun canUpsertByObjectId() {
        val expected = Friend(ObjectId(), "John")
        val r = col.findOneAndUpdate(
                "{_id:${expected._id!!.json}}",
                "{$setOnInsert: {name: 'John'}}",
                FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER))
        assertEquals(expected, r)
    }

    @Test
    fun canUpsertByStringId() {
        val expected = ExposableFriend(ObjectId().toString(), "John")
        val r = col.withDocumentClass<ExposableFriend>().findOneAndUpdate(
                "{_id:${expected._id.json}}",
                "{$setOnInsert: {name: 'John'}}",
                FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER))

        assertEquals(expected, r)
    }
}