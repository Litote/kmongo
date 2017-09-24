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
package org.litote.kmongo.jackson

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonBooleanFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor
import org.bson.BsonReader
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.RawBsonDocument
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.json.JsonReader
import org.bson.types.ObjectId
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.`object`
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.array
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.boolean
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.integer
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.map
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.number
import org.litote.kmongo.jackson.JacksonCodec.VisitorWrapper.JsonType.string
import org.litote.kmongo.util.MongoIdUtil
import java.io.IOException
import java.io.UncheckedIOException
import kotlin.reflect.jvm.javaField

/**
 *
 */
internal class JacksonCodec<T : Any>(val bsonObjectMapper: ObjectMapper,
                                     val notBsonObjectMapper: ObjectMapper,
                                     val codecRegistry: CodecRegistry,
                                     val type: Class<T>) : Codec<T>, CollectibleCodec<T> {

    class VisitorWrapper : JsonFormatVisitorWrapper.Base() {

        enum class JsonType {
            string, array, number, map, `object`, integer, boolean
        }

        var jsonType: JsonType? = null

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

    private val rawBsonDocumentCodec: Codec<RawBsonDocument>

    init {
        this.rawBsonDocumentCodec = codecRegistry.get(RawBsonDocument::class.java)
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): T? {
        try {
            val document = rawBsonDocumentCodec.decode(reader, decoderContext)
            return bsonObjectMapper.readValue(document.byteBuffer.array(), type)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
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

                    string, integer, number, boolean -> {
                        val jsonReader = JsonReader(notBsonObjectMapper.writeValueAsString(value))
                        when (visitor.jsonType) {
                            number -> writer.writeDouble(jsonReader.readDouble())
                            integer -> writer.writeInt64(jsonReader.readInt64())
                            boolean -> writer.writeBoolean(jsonReader.readBoolean())
                            else -> writer.writeString(jsonReader.readString())
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
        val idProperty = MongoIdUtil.findIdProperty(document.javaClass.kotlin)
        if (idProperty == null) {
            throw IllegalStateException("$type has no id field")
        } else {
            val idValue = MongoIdUtil.getIdBsonValue(idProperty, document)
            return idValue ?: throw IllegalStateException("$type has null id")
        }
    }

    override fun documentHasId(document: T): Boolean
            = MongoIdUtil.findIdProperty(document.javaClass.kotlin) != null

    override fun generateIdIfAbsentFromDocument(document: T): T {
        val idProperty = MongoIdUtil.findIdProperty(document.javaClass.kotlin)
        if (idProperty != null) {
            val idValue = MongoIdUtil.getIdValue(idProperty, document)
            if (idValue == null) {
                val toString = idProperty.returnType.toString()
                val javaField = idProperty.javaField
                javaField!!.isAccessible = true
                if (toString.startsWith(ObjectId::class.qualifiedName!!)) {
                    javaField.set(document, ObjectId.get())
                } else if (toString.startsWith(String::class.qualifiedName!!)) {
                    javaField.set(document, ObjectId.get().toString())
                } else {
                    error("generation for id property type not supported : $idProperty")
                }
            }
        }

        return document
    }
}