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

import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.BsonReader
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.RawBsonDocument
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import org.litote.kmongo.util.MongoIdUtil
import java.io.IOException
import java.io.UncheckedIOException
import kotlin.reflect.defaultType
import kotlin.reflect.jvm.javaField

/**
 *
 */
internal class JacksonCodec<T : Any>(val bsonObjectMapper: ObjectMapper,
                                     val codecRegistry: CodecRegistry,
                                     val type: Class<T>) : Codec<T>, CollectibleCodec<T> {

    private val rawBsonDocumentCodec: Codec<RawBsonDocument>

    init {
        this.rawBsonDocumentCodec = codecRegistry.get(RawBsonDocument::class.java)
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): T {
        try {
            return bsonObjectMapper.readValue(rawBsonDocumentCodec.decode(reader, decoderContext).byteBuffer.array(), type)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }

    override fun encode(writer: BsonWriter, value: T, encoderContext: EncoderContext) {
        try {
            val e = bsonObjectMapper.writeValueAsBytes(value)
            rawBsonDocumentCodec.encode(writer, RawBsonDocument(e), encoderContext)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }

    override fun getEncoderClass(): Class<T> {
        return this.type
    }

    override fun getDocumentId(document: T): BsonValue {
        val idProperty = MongoIdUtil.findIdProperty(type.kotlin)
        if (idProperty == null) {
            throw IllegalStateException("$type has no id field")
        } else {
            val idValue = MongoIdUtil.getIdBsonValue(idProperty, document)
            return idValue ?: throw IllegalStateException("$type has null id")
        }
    }

    override fun documentHasId(document: T): Boolean
            = MongoIdUtil.findIdProperty(type.kotlin) != null

    override fun generateIdIfAbsentFromDocument(document: T): T {
        val idProperty = MongoIdUtil.findIdProperty(type.kotlin)
        if (idProperty != null) {
            val idValue = MongoIdUtil.getIdValue(idProperty, document)
            if (idValue == null) {
                val toString = idProperty.returnType.toString()
                val javaField = idProperty.javaField
                javaField!!.isAccessible = true
                if (toString.startsWith(ObjectId::class.defaultType.toString())) {
                    javaField.set(document, ObjectId.get())
                } else if (toString.startsWith(String::class.defaultType.toString())) {
                    javaField.set(document, ObjectId.get().toString())
                } else {
                    throw IllegalArgumentException("generation for id property type not supported : $idProperty")
                }
            }
        }

        return document
    }
}