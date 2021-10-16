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