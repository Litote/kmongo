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

package org.litote.kmongo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.Test
import org.litote.kmongo.model.TestData
import org.litote.kmongo.model.TestData_Deserializer
import org.litote.kmongo.model.TestData_Serializer
import org.litote.kmongo.model.other.SimpleReferencedData
import java.util.Date
import java.util.Locale

/**
 *
 */
class SerializationTest {

    @Test
    fun `serialize and deserialize is ok`() {
        val mapper = ObjectMapper()
        mapper.registerModule(
            SimpleModule().apply {
                addSerializer(TestData::class.java, TestData_Serializer())
                addDeserializer(TestData::class.java, TestData_Deserializer())
            }
        )
        val data = TestData(
            setOf(SimpleReferencedData()),
            listOf(listOf(true)),
            "name",
            Date(),
            SimpleReferencedData(),
            mapOf("A".toId<Locale>() to setOf("B"))
        )
        data.set.first().apply {
            version = 2
        }
        val json = mapper.writeValueAsString(data)
        println(json)
        //val r: TestData = mapper.readValue(json, TestData::class.java)
        //println(r)
    }
}