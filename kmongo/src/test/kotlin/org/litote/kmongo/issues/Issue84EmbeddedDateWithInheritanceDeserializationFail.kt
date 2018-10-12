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

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import java.util.Date
import kotlin.test.assertEquals

/**
 *
 */
class Issue84EmbeddedDateWithInheritanceDeserializationFail :
    AllCategoriesKMongoBaseTest<Issue84EmbeddedDateWithInheritanceDeserializationFail.I>() {

    data class MainData(
        val e: EmbeddedWithDate?
    )   : I()

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes(
        JsonSubTypes.Type(value = MainData::class, name = "test")
    )
    abstract class I

    data class EmbeddedWithDate(val date: Date)

    @Test
    fun `serialization and deserialization is ok`() {
        val d = MainData(
            EmbeddedWithDate(Date())
        )
        col.insertOne(d)
        assertEquals(d, col.findOne())
    }
}