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

package org.bson.codecs.pojo

import org.bson.BsonDouble
import org.bson.BsonInt32
import org.bson.BsonInt64
import org.bson.BsonObjectId
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import org.litote.kmongo.util.KMongoUtil
import java.util.concurrent.ConcurrentHashMap

/**
 *
 */
internal class KMongoPojoCodec<T>(originalCodec: PojoCodec<T>, registry: CodecRegistry) : PojoCodec<T>(), CollectibleCodec<T> {

    val pojoCodec = if (originalCodec.classModel.type == Pair::class.java) {
        PojoCodecImpl<T>(
                originalCodec.classModel,
                registry,
                PropertyCodecRegistryImpl(this, registry, null),
                DiscriminatorLookup(emptyMap(), emptySet()),
                ConcurrentHashMap<ClassModel<*>, Codec<*>>(),
                true)
    } else {
        originalCodec
    }


    override fun getClassModel(): ClassModel<T> {
        return pojoCodec.classModel
    }

    override fun encode(writer: BsonWriter, value: T, encoderContext: EncoderContext) {
        pojoCodec.encode(writer, value, encoderContext)
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): T {
        return pojoCodec.decode(reader, decoderContext)
    }

    override fun getEncoderClass(): Class<T> {
        return pojoCodec.encoderClass
    }

    private fun retrieveDocumentId(document: T): Any? {
        return pojoCodec.classModel
                .idPropertyModel
                ?.propertyAccessor
                ?.get(document)
    }

    override fun getDocumentId(document: T): BsonValue {
        return retrieveDocumentId(document)
                ?.let { getIdBsonValue(it) }
                ?: error("unable to retrieve _id for $document")
    }

    private fun getIdBsonValue(idValue: Any): BsonValue {
        return when (idValue) {
            is ObjectId -> BsonObjectId(idValue)
            is String -> BsonString(idValue)
            is Double -> BsonDouble(idValue)
            is Int -> BsonInt32(idValue)
            is Long -> BsonInt64(idValue)
        //TODO direct mapping
            else -> KMongoUtil.toBson(KMongoUtil.toExtendedJson(idValue))
        }
    }

    override fun generateIdIfAbsentFromDocument(document: T): T {
        val idProperty = pojoCodec.classModel.idPropertyModel
        @Suppress("UNCHECKED_CAST")
        val propertyAccessor: PropertyAccessor<Any>? = idProperty?.propertyAccessor as PropertyAccessor<Any>?
        if (idProperty != null && propertyAccessor != null) {
            val idValue = propertyAccessor.get(document)
            if (idValue == null) {
                val type = idProperty.typeData.type
                if (ObjectId::class.java == type) {
                    setPropertyValue(propertyAccessor, document, ObjectId.get())
                } else if (String::class.java == type) {
                    setPropertyValue(propertyAccessor, document, ObjectId.get().toString())
                } else {
                    error("generation for id property type not supported : $idProperty")
                }
            }
        }

        return document
    }

    private fun setPropertyValue(property: PropertyAccessor<Any>?, document: Any?, value: Any?) {
        val p = property as PropertyAccessorImpl
        val metadataField = p.javaClass.getDeclaredField("propertyMetadata")
        metadataField.isAccessible = true
        val metadata = metadataField.get(p) as PropertyMetadata<*>
        if (metadata.isDeserializable) {
            property.set(document, value)
        } else {
            val field = metadata.field
            field.isAccessible = true
            field.set(document, value)
        }
    }

    override fun documentHasId(document: T): Boolean {
        return retrieveDocumentId(document) != null

    }
}