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

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonBooleanFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNullFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor
import org.bson.BsonBinaryWriter
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.RawBsonDocument
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.io.BasicOutputBuffer
import org.bson.json.JsonReader
import org.litote.kmongo.Id
import org.litote.kmongo.id.StringId
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.array
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.boolean
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.date
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.integer
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.map
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.number
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.`object`
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.objectId
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.string
import org.litote.kmongo.json
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.generateNewIdForIdClass
import org.litote.kmongo.util.MongoIdUtil
import java.io.IOException
import java.io.UncheckedIOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.util.Calendar
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

/**
 *
 */
internal class JacksonCodec<T : Any>(
    private val bsonObjectMapper: ObjectMapper,
    private val notBsonObjectMapper: ObjectMapper,
    codecRegistry: CodecRegistry,
    val type: Class<T>
) : Codec<T>, CollectibleCodec<T> {

    class VisitorWrapper : JsonFormatVisitorWrapper.Base() {

        enum class JsonType {
            string, array, number, map, `object`, integer, boolean, objectId, date
        }

        var jsonType: JsonType? = null

        private val dateTimeClasses =
            setOf(
                Instant::class,
                ZonedDateTime::class,
                OffsetDateTime::class,
                LocalDate::class,
                LocalDateTime::class,
                LocalTime::class,
                OffsetTime::class
            )

        override fun expectNullFormat(type: JavaType?): JsonNullFormatVisitor? {
            return null
        }

        override fun expectAnyFormat(type: JavaType): JsonAnyFormatVisitor? {
            if (type.rawClass?.let { Id::class.java.isAssignableFrom(it) } == true) {
                if (type.rawClass == StringId::class.java) {
                    jsonType = string
                } else {
                    jsonType = objectId
                }
            } else if (dateTimeClasses.contains(type.rawClass.kotlin)
                || Calendar::class.java.isAssignableFrom(type.rawClass)
            ) {
                jsonType = date
            } else {
                jsonType = `object`
            }
            return null
        }

        override fun expectStringFormat(type: JavaType?): JsonStringFormatVisitor? {
            jsonType = string
            return null
        }

        override fun expectArrayFormat(type: JavaType?): JsonArrayFormatVisitor? {
            jsonType = array
            return null
        }

        override fun expectNumberFormat(type: JavaType?): JsonNumberFormatVisitor? {
            jsonType = number
            return null
        }

        override fun expectMapFormat(type: JavaType?): JsonMapFormatVisitor? {
            jsonType = map
            return null
        }

        override fun expectObjectFormat(type: JavaType?): JsonObjectFormatVisitor? {
            jsonType = `object`
            return null
        }

        override fun expectIntegerFormat(type: JavaType?): JsonIntegerFormatVisitor? {
            jsonType = integer
            return null
        }

        override fun expectBooleanFormat(type: JavaType?): JsonBooleanFormatVisitor? {
            jsonType = boolean
            return null
        }
    }

    private val rawBsonDocumentCodec: Codec<RawBsonDocument> = codecRegistry.get(RawBsonDocument::class.java)

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): T? {
        //string to object conversion
        if (reader.currentBsonType == BsonType.STRING) {
            val s = reader.readString()
            return notBsonObjectMapper.readValue(s.json, type)
        }
        val buffer = BasicOutputBuffer(128)
        val writer = BsonBinaryWriter(buffer)
        return try {
            writer.pipe(reader)
            bsonObjectMapper.readValue(buffer.internalBuffer, type)
        } finally {
            writer.close()
            buffer.close()
        }
    }

    override fun encode(writer: BsonWriter, value: T?, encoderContext: EncoderContext) {
        try {
            if (value == null) {
                writer.writeNull()
            } else {
                //need to know the serialized type, see https://github.com/Litote/kmongo/issues/12
                val visitor = VisitorWrapper()
                bsonObjectMapper.acceptJsonFormatVisitor(value::class.java, visitor)

                when (visitor.jsonType) {
                    `object`, map, array, null -> {
                        val bytes = bsonObjectMapper.writeValueAsBytes(value)
                        rawBsonDocumentCodec.encode(writer, RawBsonDocument(bytes), encoderContext)
                    }

                    string, integer, number, boolean, objectId, date -> {
                        val jsonReader = JsonReader(notBsonObjectMapper.writeValueAsString(value))
                        when (jsonReader.readBsonType()) {
                            BsonType.STRING -> writer.writeString(jsonReader.readString())
                            BsonType.DOUBLE -> writer.writeDouble(jsonReader.readDouble())
                            BsonType.INT64 -> writer.writeInt64(jsonReader.readInt64())
                            BsonType.INT32 -> writer.writeInt32(jsonReader.readInt32())
                            BsonType.BOOLEAN -> writer.writeBoolean(jsonReader.readBoolean())
                            BsonType.OBJECT_ID -> writer.writeObjectId(jsonReader.readObjectId())
                            BsonType.DATE_TIME -> writer.writeDateTime(jsonReader.readDateTime())
                            BsonType.BINARY -> writer.writeBinaryData(jsonReader.readBinaryData())
                            BsonType.DECIMAL128 -> writer.writeDecimal128(jsonReader.readDecimal128())
                            BsonType.TIMESTAMP -> writer.writeTimestamp(jsonReader.readTimestamp())
                            BsonType.NULL -> writer.writeNull().also { jsonReader.readNull() }
                            else -> when (visitor.jsonType) {
                                number -> writer.writeDouble(jsonReader.readDouble())
                                integer -> writer.writeInt64(jsonReader.readInt64())
                                boolean -> writer.writeBoolean(jsonReader.readBoolean())
                                objectId -> writer.writeObjectId(jsonReader.readObjectId())
                                date -> writer.writeDateTime(jsonReader.readDateTime())
                                else -> writer.writeString(jsonReader.readString())
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    override fun getEncoderClass(): Class<T> {
        return this.type
    }

    override fun getDocumentId(document: T): BsonValue {
        @Suppress("UNCHECKED_CAST")
        val idProperty = MongoIdUtil.findIdProperty(document.javaClass.kotlin) as? KProperty1<T, *>?
        if (idProperty == null) {
            throw IllegalStateException("$type has no id field")
        } else {
            val idValue = KMongoUtil.getIdBsonValue(idProperty.get(document))
            return idValue ?: throw IllegalStateException("$type has null id")
        }
    }

    override fun documentHasId(document: T): Boolean = MongoIdUtil.findIdProperty(document.javaClass.kotlin) != null

    override fun generateIdIfAbsentFromDocument(document: T): T {
        @Suppress("UNCHECKED_CAST")
        val idProperty = MongoIdUtil.findIdProperty(document.javaClass.kotlin) as? KProperty1<Any, *>?
        if (idProperty != null) {
            val idValue = MongoIdUtil.getIdValue(idProperty, document)
            if (idValue == null) {
                val javaField = idProperty.javaField!!
                val type = javaField.type
                javaField.isAccessible = true
                javaField.set(document, generateNewIdForIdClass(type.kotlin))
            }
        }

        return document
    }
}