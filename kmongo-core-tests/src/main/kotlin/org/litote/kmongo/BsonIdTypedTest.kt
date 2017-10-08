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

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class BsonIdTypedTest : AllCategoriesKMongoBaseTest<Friend>() {

    class StringId(val _id: String? = null)

    class WithMongoId(@BsonId val key: ObjectId? = null)

    class WithMongoStringId(@BsonId val key: String? = null)

    class CompositeId(val _id: Key?)

    class CompositeKey(@BsonId val key: Key?)

    data class Key(val category: String, val index: Int)

    @Test
    fun testEqualityWithObjectId() {
        val friend = Friend("Joe")
        col.insertOne(friend)
        assertEquals("Joe", col.findOne(friend::_id eq friend._id)!!.name)
    }

    @Test
    fun testEqualityWithStringId() {
        val stringId = StringId()
        val stringIdCol = col.withDocumentClass<StringId>()
        stringIdCol.insertOne(stringId)
        assertEquals(stringId._id, stringIdCol.findOne(stringId::_id eq stringId._id)!!._id)
    }

    /*
    @Test
    fun testEqualityWithMongoId() {
        val withMongoId = WithMongoId()
        val withMongoIdCol = col.withDocumentClass<WithMongoId>()
        withMongoIdCol.insertOne(withMongoId)
        assertEquals(withMongoId.key, withMongoIdCol.findOne(withMongoId::key eq withMongoId.key)!!.key)
    }

    @Test
    fun testEqualityWithMongoStringId() {
        val withMongoId = WithMongoStringId("keyValue")
        val withMongoIdCol = col.withDocumentClass<WithMongoStringId>()
        withMongoIdCol.insertOne(withMongoId.json)
        assertEquals(withMongoId.key, withMongoIdCol.findOne(withMongoId::key eq withMongoId.key)!!.key)
    } */

    @Test
    fun testEqualityWithCompositeId() {
        val compositeId = CompositeId(Key("alpha", 2))
        val compositeIdCol = col.withDocumentClass<CompositeId>()
        compositeIdCol.insertOne(compositeId)
        assertEquals(compositeId._id, compositeIdCol.findOne(compositeId::_id eq compositeId._id)!!._id)
    }

    /*
    @Test
    fun testEqualityWithCompositeKey() {
        val compositeKey = CompositeKey(Key("alpha", 2))
        val compositeKeyCol = col.withDocumentClass<CompositeKey>()
        compositeKeyCol.insertOne(compositeKey)
        assertEquals(compositeKey.key, compositeKeyCol.findOne(compositeKey::key eq compositeKey.key)!!.key)
    } */
}