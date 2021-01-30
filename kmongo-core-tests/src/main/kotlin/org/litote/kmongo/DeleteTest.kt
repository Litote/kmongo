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

import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class DeleteTest : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun canDeleteASpecificDocument() {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteOne("{name:'John'}")
        val list = col.find().toList()
        assertEquals(1, list.size)
        assertEquals("Peter", list.first().name)
    }

    @Test
    fun canDeleteByObjectId() {
        col.insertOne("{ _id:{$oid:'47cc67093475061e3d95369d'}, name:'John'}")
        col.deleteOneById(ObjectId("47cc67093475061e3d95369d"))
        val c = col.countDocuments()
        assertEquals(0, c)
    }

    @Test
    fun canRemoveAll() {
        col.insertMany(listOf(Friend("John"), Friend("Peter")))
        col.deleteMany("{}")
        val c = col.countDocuments()
        assertEquals(0, c)
    }
}