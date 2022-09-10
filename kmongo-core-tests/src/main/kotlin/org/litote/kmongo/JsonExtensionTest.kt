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

package org.litote.kmongo

import com.mongodb.client.model.Projections
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.MongoOperator.oid
import java.time.Instant
import kotlin.test.assertEquals

@Category(JacksonMappingCategory::class, NativeMappingCategory::class, SerializationMappingCategory::class)
class JsonExtensionTest : KMongoRootTest() {

    @Serializable
    class DataTest(var a: String)

    @Test
    fun booleanToBson() = assertEquals("true", true.json)

    @Test
    fun intToBson() = assertEquals("2", 2.json)

    @Test
    fun stringToBson() = assertEquals("\"test'and\\\"fire\"", "test'and\"fire".json)

    @Test
    fun pairToBson() = assert(
        "{\"first\":\"z\",\"second\":4}" == Pair("z", 4).json
                || "{\"first\": \"z\", \"second\": 4}" == Pair("z", 4).json
    )

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

    @Test
    fun `week aggregate`() {
        data class Base(val timestamp: Instant)
        data class Result(val week: Int, val documentCount: Int)

        val pipeline = """
            {
                "$ group": {
                "_id": {"$ week": "$ timestamp"},
                "documentCount": {"$ sum": 1}
            }
            }""".formatJson().replace(" ", "").replace("\n", "")

        val bson =
            group(
                Projections.computed("\$week", Base::timestamp.projection),
                Result::documentCount sum (1)
            )

        val bson2 =
            group(
                week(Base::timestamp),
                Result::documentCount sum (1)
            )

        val bson3 =
            group(
                MongoOperator.week from Base::timestamp,
                Result::documentCount sum (1)
            )

        assertEquals(pipeline.replace(" ", ""), bson.json.replace(" ", ""))
        assertEquals(pipeline.replace(" ", ""), bson2.json.replace(" ", ""))
        assertEquals(pipeline.replace(" ", ""), bson3.json.replace(" ", ""))
    }

}

