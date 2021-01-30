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

package org.litote.kmongo.jackson

import com.fasterxml.jackson.module.kotlin.readValue
import org.bson.UuidRepresentation
import org.junit.Assert.assertArrayEquals
import org.litote.kmongo.jackson.ObjectMapperFactory.createBsonObjectMapper
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class UUIDTest
{
    private val testCode = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d0e0f")

    @Test
    fun testGeneratedBytesForUUID() {
        // First 4 bytes are the length, in our case always 16 bytes
        // The 5th byte is the binary type
        // The other 16 bytes is the actual data

        // This is the new standard, with type 4 binary data to represent the UUID (bytes are in order)
        assertArrayEquals(byteArrayOf(16, 0, 0, 0, 4, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                     createBsonObjectMapper(UuidRepresentation.STANDARD).writeValueAsBytes(testCode))

        // This is the legacy representation, with type 3 binary data and bytes are switched
        assertArrayEquals(byteArrayOf(16, 0, 0, 0, 3, 7, 6, 5, 4, 3, 2, 1, 0, 15, 14, 13, 12, 11, 10, 9, 8),
                          createBsonObjectMapper(UuidRepresentation.JAVA_LEGACY).writeValueAsBytes(testCode))

        // If null, it falls back to the old implementation from bson4jackson, which should be the same as JAVA_LEGACY
        assertArrayEquals(byteArrayOf(16, 0, 0, 0, 3, 7, 6, 5, 4, 3, 2, 1, 0, 15, 14, 13, 12, 11, 10, 9, 8),
                          createBsonObjectMapper().writeValueAsBytes(testCode))
    }

    data class TestingObject(
            val id: UUID,
            val other: String
    )

    private fun testSerializationAndDeserializationOfType(uuidRepresentation: UuidRepresentation?) {
        val mapper = createBsonObjectMapper(uuidRepresentation)

        val testObject = TestingObject(testCode, "Testing String")

        val bytes = mapper.writeValueAsBytes(testObject)

        // Check the type of the binary representation of the UUID
        assertEquals(if(uuidRepresentation == UuidRepresentation.STANDARD) 4 else 3, bytes[12].toInt())

        val decodedObject: TestingObject = mapper.readValue(bytes)

        assertEquals(testObject, decodedObject)
    }

    @Test
    fun testSerializationAndDeserializationOfStandardType() = testSerializationAndDeserializationOfType(UuidRepresentation.STANDARD)

    @Test
    fun testSerializationAndDeserializationOfLegacyType() = testSerializationAndDeserializationOfType(UuidRepresentation.JAVA_LEGACY)

    @Test
    fun testSerializationAndDeserializationOfNullType() = testSerializationAndDeserializationOfType(null)
}
