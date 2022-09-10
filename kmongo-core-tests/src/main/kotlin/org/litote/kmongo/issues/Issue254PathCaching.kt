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

import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoRootTest
import org.litote.kmongo.MongoOperator.lookup
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.SerializationMappingCategory
import org.litote.kmongo.json
import org.litote.kmongo.lookup
import kotlin.test.assertEquals

private data class Instance(val p1: String, val p2: String)

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class, SerializationMappingCategory::class)
class Issue254PathCaching : KMongoRootTest() {

    @Test
    fun `test json generation lookup`() {
        val json1 = lookup("myColl", resultProperty = Instance::p1).json

        assertEquals("""{"$lookup": {"from": "myColl", "pipeline": [], "as": "p1"}}""", json1)

        val json2 = lookup("myColl", resultProperty = Instance::p2).json

        assertEquals("""{"$lookup": {"from": "myColl", "pipeline": [], "as": "p2"}}""", json2)
    }
}