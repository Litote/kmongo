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

import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.MongoOperator.dateToString
import java.util.Date
import kotlin.test.assertEquals

/**
 *
 */

@Category(JacksonMappingCategory::class, NativeMappingCategory::class, SerializationMappingCategory::class)
class TypedOperationsTest : KMongoRootTest() {

    class Projection(val stringField: String, val dateField: Date)
    class ResultKey(val a: String, val date: String)
    class Result(val _id: ResultKey)

    @Test
    fun `test group with dateToString`() {
        assertEquals(
            """
            { "${'$'}group" :
                {
                    "_id" : {
                    "a" : "${'$'}stringField",
                    "date" : { "$dateToString" : { "format" : "%Y-%m-%d", "date" : "${'$'}dateField" } }
                }
                }
            } """.bson,
            group(
                fields(
                    ResultKey::a from Projection::stringField,
                    ResultKey::date from (
                            dateToString from
                                    fields(
                                        "format" from "%Y-%m-%d",
                                        "date" from Projection::dateField
                                    )
                            )
                )
            ).json.bson
        )
    }

}