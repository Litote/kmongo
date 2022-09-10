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

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.find
import org.litote.kmongo.issues.Issue73IdNotDeserializedWhenUsingInterfaceAsCollectionClass.Intermediate
import kotlin.test.assertEquals

/**
 *
 */
class Issue73IdNotDeserializedWhenUsingInterfaceAsCollectionClass : AllCategoriesKMongoBaseTest<Intermediate>() {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    @JsonSubTypes(
        JsonSubTypes.Type(Implementation::class)
    )
    interface Intermediate {
        var _id: ObjectId?
    }

    data class Implementation(
        override var _id: ObjectId? = null,
        var field: String = ""
    ) : Intermediate

    @Test
    fun `GIVEN interface that defines _id THEN it is populated with the database _id`() {
        val obj = Implementation(field = "value")
        col.insertOne(obj)

        val firstResult = col.find("{}").toList().first()
        val secondResult = col.find("{}").toList().first()

        assertEquals(obj._id, firstResult._id)
        assertEquals(firstResult._id, secondResult._id)
    }
}