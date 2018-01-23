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

package org.litote.kmongo.issues

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import kotlin.reflect.KClass
import kotlin.test.assertEquals

/**
 *
 */
class Issue51ObjectNodeClassSerializedInArray : AllCategoriesKMongoBaseTest<Document>() {

    override fun getDefaultCollectionClass(): KClass<Document> = Document::class

    @Test
    fun testFindAndUpdate() {
        val mapper = jacksonObjectMapper()
        val objectNode: ObjectNode = mapper.createObjectNode()
        objectNode.put("test", 123)

        col.updateOne(
                Filters.eq("_id", "test"),
                Updates.set("objectField", mapper.convertValue(objectNode, Map::class.java)),
                UpdateOptions().upsert(true)
        )
        assertEquals(
                Document(
                        mapOf(
                                "_id" to "test",
                                "objectField" to Document(mapOf("test" to 123))
                        )
                ),
                col.findOne())
    }
}