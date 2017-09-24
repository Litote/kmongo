/*
 * Copyright (C) 2016 Litote
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
package org.litote.kmongo.async

import org.bson.types.ObjectId
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoRootTest
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.NativeMappingCategory
import kotlin.test.assertEquals

@Category(JacksonMappingCategory::class, NativeMappingCategory::class)
class JsonExtensionTest : KMongoRootTest() {

    class DataTest(var a: String)

    @Test
    fun booleanToBson() = assertEquals("true", true.json)

    @Test
    fun intToBson() = assertEquals("2", 2.json)

    @Test
    fun stringToBson() = assertEquals("\"test'and\\\"fire\"", "test'and\"fire".json)

    @Test
    fun pojoToBson() = assertEquals("{\"first\":\"z\",\"second\":4}", Pair("z", 4).json)

    @Test
    fun arrayToBson() = assertEquals("[\"z\\\"z\",\"aa\"]", arrayOf("z\"z", "aa").json.replace(" ", ""))

    @Test
    fun objectIdToBson() {
        val id = ObjectId()
        assertEquals("{\"$oid\":\"$id\"}", id.json.replace(" ", ""))
    }

    @Test
    fun objectMutation() {
        val data = DataTest("a")
        assertEquals("{\"a\":\"a\"}", data.json.replace(" ", ""))
        data.a = "b"
        assertEquals("{\"a\":\"b\"}", data.json.replace(" ", ""))
    }
}

