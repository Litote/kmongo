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

package org.litote.kmongo.issues

import com.mongodb.DBRef
import org.bson.types.ObjectId
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.issues.Issue18SavingDBRefContainingAnObjectIdBreaks.TestData
import org.litote.kmongo.save
import kotlin.test.assertEquals

/**
 *
 */
class Issue18SavingDBRefContainingAnObjectIdBreaks : KMongoBaseTest<TestData>() {

    data class TestData(val title: String, val ref: DBRef?)

    @Category(JacksonMappingCategory::class)
    @Test
    fun testSerializeAndDeserializeDBRefWithId() {
        val data = TestData("I'm working", DBRef("magic", "id"))
        database.getCollection<TestData>().save(data)
        assertEquals(data, database.getCollection<TestData>().findOne()!!)
    }

    @Category(JacksonMappingCategory::class)
    @Test
    fun testSerializeAndDeserializeDBRefWithObjectId() {
        val data = TestData("I'm working", DBRef("magic", ObjectId()))
        database.getCollection<TestData>().save(data)
        assertEquals(data, database.getCollection<TestData>().findOne()!!)
    }

    @Category(JacksonMappingCategory::class, NativeMappingCategory::class)
    @Test
    fun testSerializeAndDeserializeNullDBRef() {
        val data = TestData("I'm working", null)
        database.getCollection<TestData>().save(data)
        assertEquals(data, database.getCollection<TestData>().findOne()!!)
    }

    @Category(JacksonMappingCategory::class)
    @Test
    fun testSerializeAndDeserializeWithDatabaseDBRef() {
        val data = TestData("I'm working", DBRef("db", "magic", ObjectId()))
        database.getCollection<TestData>().save(data)
        assertEquals(data, database.getCollection<TestData>().findOne()!!)
    }
}