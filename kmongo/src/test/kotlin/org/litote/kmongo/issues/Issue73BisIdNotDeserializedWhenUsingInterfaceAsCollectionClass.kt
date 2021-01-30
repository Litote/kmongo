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

package org.litote.kmongo.issues

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOneById
import org.litote.kmongo.issues.Issue73BisIdNotDeserializedWhenUsingInterfaceAsCollectionClass.Intermediate
import kotlin.test.assertEquals

/**
 *
 */
class Issue73BisIdNotDeserializedWhenUsingInterfaceAsCollectionClass : AllCategoriesKMongoBaseTest<Intermediate>() {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    @JsonSubTypes(
        JsonSubTypes.Type(Implementation::class)
    )
    interface Intermediate {
        var _id: ObjectId?
    }

    data class Implementation(
        override var _id: ObjectId? = null,
        var fk: ObjectId
    ) : Intermediate

    @Test
    fun `GIVEN interface that defines _id with fk ObjectId THEN it is populated with the database _id and the fk ObjectId`() {
        val obj = Implementation(fk = ObjectId())
        col.insertOne(obj)

        val result = col.findOneById(obj._id!!) as Implementation

        assertEquals(obj, result)
    }
}