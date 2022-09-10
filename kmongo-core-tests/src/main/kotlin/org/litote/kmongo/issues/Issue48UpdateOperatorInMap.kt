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

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.Document
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.MongoOperator.addToSet
import org.litote.kmongo.MongoOperator.push
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.findOneById
import org.litote.kmongo.issues.Issue48UpdateOperatorInMap.CollectionTest
import org.litote.kmongo.newId
import org.litote.kmongo.save
import org.litote.kmongo.updateOneById
import kotlin.test.assertEquals

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class)
class Issue48UpdateOperatorInMap : KMongoBaseTest<CollectionTest>() {

    @Serializable
    data class CollectionTest(
        val set: Set<String> = emptySet(),
        val list: List<String> = emptyList(),
        @Contextual
        val _id: Id<CollectionTest> = newId()
    )

    @Test
    fun testUpdateSet() {
        val t = CollectionTest(setOf("a"))
        col.save(t)
        col.updateOneById(t._id, """{$addToSet : {"set" : "b"}, $push: {"list": "a"}}""")
        assertEquals(t.copy(set = setOf("a", "b"), list = listOf("a")), col.findOneById(t._id))
    }

    @Test
    fun testUpdateSetWithDocument() {
        val t = CollectionTest(setOf("a"))
        col.save(t)
        val doc = Document(
            mapOf(
                "$addToSet" to Document(mapOf("set" to "b")),
                "$push" to Document(mapOf("list" to "a"))
            )
        )
        col.updateOneById(t._id, doc)
        assertEquals(t.copy(set = setOf("a", "b"), list = listOf("a")), col.findOneById(t._id))
    }

}