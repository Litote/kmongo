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

package org.litote.kmongo

import com.mongodb.client.model.ReturnDocument
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.model.Coordinate
import org.litote.kmongo.model.ExposableFriend
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class FindOneAndModifyTypedTest : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun canFindAndUpdateOne() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col.findOneAndUpdate(Friend::name eq "John", setValue(Friend::address, "A better place"))
        val friend = col.findOne(Friend::name eq "John")
        assertEquals("John", friend!!.name)
        assertEquals("A better place", friend.address)
    }

    @Test
    fun canFindAndUpdateWithNullValue() {
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col.findOneAndUpdate(Friend::name eq "John", setValue(Friend::address, null))
        val friend = col.findOne(Friend::name eq "John")
        assertEquals("John", friend!!.name)
        assertNull(friend.address)
    }

    @Test
    fun canFindAndIncrement() {
        col.insertOne(Friend("John", coordinate = Coordinate(1,1)))
        col.findOneAndUpdate(Friend::name eq "John", inc(Friend::coordinate / Coordinate::lat, 1))
        val friend = col.findOne(Friend::name eq "John")
        assertEquals("John", friend!!.name)
        assertEquals(2, friend.coordinate?.lat)
    }

    @Test
    fun canFindAndUpdateWithDocument() {
        val col2 = col.withDocumentClass<Document>()
        col.insertOne(Friend("John", "22 Wall Street Avenue"))
        col2.findOneAndUpdate(Friend::name eq "John", setValue(Friend::address, "A better place"))
        val friend = col2.findOne(Friend::name eq "John")
        assertEquals("John", friend!!.get("name"))
        assertEquals("A better place", friend.get("address"))
    }

    @Test
    fun canUpsertByObjectId() {
        val expected = Friend(ObjectId(), "John")
        val r = col.findOneAndUpdate(
            Friend::_id eq expected._id,
            setOnInsert(Friend::name, "John"),
            findOneAndUpdateUpsert().returnDocument(ReturnDocument.AFTER)
        )
        assertEquals(expected, r)
    }

    @Test
    fun canUpsertByStringId() {
        val expected = ExposableFriend(ObjectId().toString(), "John")
        val r = col.withDocumentClass<ExposableFriend>().findOneAndUpdate(
            ExposableFriend::_id eq expected._id,
            setOnInsert(ExposableFriend::name, "John"),
            findOneAndUpdateUpsert().returnDocument(ReturnDocument.AFTER)
        )

        assertEquals(expected, r)
    }
}