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
import org.bson.BsonObjectId
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.RawBsonDocument
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import java.io.IOException
import java.io.UncheckedIOException
import java.lang.reflect.Field
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 *
 */
internal class JacksonCodec<T>(val bsonObjectMapper: ObjectMapper,
                      val codecRegistry: CodecRegistry,
                      val type: Class<T>) : Codec<T>, CollectibleCodec<T> {

    private val rawBsonDocumentCodec: Codec<RawBsonDocument>
    private val idField: Field? by lazy(PUBLICATION) {
        val f = try {
            type.getDeclaredField("_id")
        } catch(e: NoSuchFieldException) {
            //type.declaredFields.firstOrNull { it.isAnnotationPresent(MongoId::class.java) }
            null
        }
        if (f == null) {
            null
        } else {
            f.isAccessible = true
            f
        }
    }

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

    override fun getDocumentId(document: T): BsonValue? {
        if (idField == null) {
            throw IllegalStateException("$type has no id field")
        } else {
            val idValue = (idField as Field).get(document)
            if (idValue == null) {
                throw IllegalStateException("$type has null id")
            }
            return when (idValue) {
                is ObjectId -> BsonObjectId(idValue)
                is String -> BsonString(idValue)
                else -> throw IllegalArgumentException("id field type not supported : ${idField}")
            }
        }
    }

    override fun documentHasId(document: T): Boolean = idField != null &&  (idField as Field).get(document) != null

    override fun generateIdIfAbsentFromDocument(document: T): T {
        if (idField != null) {
            val id = (idField as Field)
            val idValue = id.get(document)
            if (idValue == null) {
                val fieldType = id.getType()
                if (fieldType == String::class.java) {
                    id.set(document, ObjectId.get().toString())
                } else if (fieldType == ObjectId::class.java) {
                    id.set(document, ObjectId.get())
                } else {
                    throw IllegalArgumentException("generation for id field type not supported : ${idField}")
                }
            }
        }

        return document
    }
}