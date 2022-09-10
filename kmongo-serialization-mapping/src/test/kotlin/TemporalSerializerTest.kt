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

package org.litote.kmongo.serialization

import kotlinx.serialization.*
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.junit.Test
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@Serializable
private data class DummyTemporalWrapperEntity(
    @SerialName("date_int")
    @Contextual
    val dateInt: Instant,

    @SerialName("date_long")
    @Contextual
    val dateLong: Instant,

    @SerialName("date_double")
    @Contextual
    val dateDouble: Instant,

    @SerialName("date_decimal_128")
    @Contextual
    val dateDecimal128: Instant,

    @SerialName("date_timestamp")
    @Contextual
    val dateTimestamp: Instant,
)

class TemporalSerializerTest {
    @InternalSerializationApi
    @ExperimentalSerializationApi
    @Test
    fun `decode Instant from json`() {
        val stubSeconds = 1634339695L
        val stubMillis = TimeUnit.SECONDS.toMillis(stubSeconds)
        val jsonString = """
            { 
                "date_int": ${stubSeconds}, 
                "date_long": NumberLong(${stubMillis}), 
                "date_double": ${stubMillis}.0, 
                "date_decimal_128": NumberDecimal("${stubMillis}.0"),
                "date_timestamp": Timestamp(${stubSeconds}, 1)
            }
        """.trimIndent()

        BsonTimestampSerializer
        val codec = SerializationCodec(DummyTemporalWrapperEntity::class, configuration)

        val actualEntity = codec.decode(BsonDocumentReader(BsonDocument.parse(jsonString)), DecoderContext.builder().build())
        val expectedEntity = DummyTemporalWrapperEntity(
            dateInt = Instant.ofEpochMilli(stubSeconds),
            dateLong = Instant.ofEpochMilli(stubMillis),
            dateDouble = Instant.ofEpochMilli(stubMillis),
            dateDecimal128 = Instant.ofEpochMilli(stubMillis),
            dateTimestamp = Instant.ofEpochSecond(stubSeconds),
        )

        assertEquals(expectedEntity, actualEntity)
    }

    @InternalSerializationApi
    @ExperimentalSerializationApi
    @Test
    fun `encode and decode Instant`() {
        val stubSeconds = 1634339695L
        val stubMillis = TimeUnit.SECONDS.toMillis(stubSeconds)
        val expectedEntity = DummyTemporalWrapperEntity(
            dateInt = Instant.ofEpochMilli(stubSeconds),
            dateLong = Instant.ofEpochMilli(stubMillis),
            dateDouble = Instant.ofEpochMilli(stubMillis),
            dateDecimal128 = Instant.ofEpochMilli(stubMillis),
            dateTimestamp = Instant.ofEpochSecond(stubSeconds),
        )

        val codec = SerializationCodec(DummyTemporalWrapperEntity::class, configuration)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, expectedEntity, EncoderContext.builder().build())

        val actualEntity = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(expectedEntity, actualEntity)
    }
}