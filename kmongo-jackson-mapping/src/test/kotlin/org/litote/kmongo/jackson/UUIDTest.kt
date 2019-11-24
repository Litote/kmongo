/*
 * Copyright (C) 2017/2019 Litote
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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.kmongo.jackson.ObjectMapperFactory.createBsonObjectMapper
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class UUIDTest {

    companion object {
        fun getBytesFromUUID(uuid: UUID): ByteArray {
            val bb: ByteBuffer = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(uuid.mostSignificantBits)
            bb.putLong(uuid.leastSignificantBits)
            return bb.array()
        }

        fun getUUIDFromBytes(bytes: ByteArray): UUID {
            val byteBuffer: ByteBuffer = ByteBuffer.wrap(bytes)
            val high: Long = byteBuffer.getLong()
            val low: Long = byteBuffer.getLong()
            return UUID(high, low)
        }
    }

    object UUIDSerializer : StdSerializer<UUID>(UUID::class.java) {
        override fun serialize(uuid: UUID, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeBinary(getBytesFromUUID(uuid))
        }
    }

    object UUIDDeserializer : StdDeserializer<UUID>(UUID::class.java) {

        override fun deserialize(parser: JsonParser, deserializer: DeserializationContext): UUID {
            val binary: ByteArray = parser.binaryValue
            val uuid = getUUIDFromBytes(binary);

            return uuid;
        }
    }

    data class UUIDContainer(
        @JsonSerialize(using = UUIDSerializer::class)
        @JsonDeserialize(using = UUIDDeserializer::class)
        val uuid: UUID
    )

    @Test
    fun testSerializationAndDeserialization() {
        val c = UUIDContainer(UUID.randomUUID())

        val mapper = createBsonObjectMapper()

        val bson = mapper.writeValueAsBytes(c)

        val data: UUIDContainer = mapper.readValue(bson, UUIDContainer::class.java)

        assertEquals(c, data)
    }
}