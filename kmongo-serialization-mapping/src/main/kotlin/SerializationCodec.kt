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

package org.litote.kmongo.serialization

import com.github.jershell.kbson.BsonDocumentDecoder
import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.Configuration
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.decode
import kotlinx.serialization.encode
import org.bson.BsonDouble
import org.bson.BsonInt32
import org.bson.BsonInt64
import org.bson.BsonObjectId
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.types.ObjectId
import org.litote.kmongo.serialization.KMongoSerializationRepository.module
import org.litote.kmongo.service.ClassMappingType
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

/**
 *
 */
internal class SerializationCodec<T : Any>(val clazz: KClass<T>) : CollectibleCodec<T> {

    private val idProperty: KProperty1<*, *>? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ClassMappingType.findIdProperty(clazz)
    }

    override fun getEncoderClass(): Class<T> = clazz.java

    @ImplicitReflectionSerializer
    override fun encode(writer: BsonWriter, value: T, encoderContext: EncoderContext) {
        val serializer = KMongoSerializationRepository.getSerializer(value)
        BsonEncoder(writer, module, Configuration()).encode(serializer, value)
    }

    @ImplicitReflectionSerializer
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): T {
        return BsonDocumentDecoder(reader, module, Configuration()).decode(
            KMongoSerializationRepository.getSerializer(clazz)
        )
    }

    override fun getDocumentId(document: T): BsonValue {
        if (idProperty == null) {
            throw IllegalStateException("$clazz has no id field")
        } else {
            @Suppress("UNCHECKED_CAST")
            val idValue = getIdBsonValue(idProperty!!, document)
            return idValue ?: throw IllegalStateException("$clazz has null id")
        }
    }

    private fun getIdBsonValue(idProperty: KProperty1<*, *>, instance: Any): BsonValue? {
        val idValue = (idProperty)(instance)
        return when (idValue) {
            null -> null
            is ObjectId -> BsonObjectId(idValue)
            is String -> BsonString(idValue)
            is Double -> BsonDouble(idValue)
            is Int -> BsonInt32(idValue)
            is Long -> BsonInt64(idValue)
            //TODO direct mapping
            else -> KMongoUtil.toBson(KMongoUtil.toExtendedJson(idValue))
        }
    }

    override fun documentHasId(document: T): Boolean = idProperty != null

    override fun generateIdIfAbsentFromDocument(document: T): T {
        if (idProperty != null) {
            @Suppress("UNCHECKED_CAST")
            val idValue = ClassMappingType.getIdValue(idProperty as KProperty1<Any, Any>, document)
            if (idValue == null) {
                val javaField = idProperty!!.javaField!!
                val type = javaField.type
                javaField.isAccessible = true
                if (ObjectId::class.java == type) {
                    javaField.set(document, ObjectId.get())
                } else if (String::class.java == type) {
                    javaField.set(document, ObjectId.get().toString())
                } else {
                    error("generation for id property type not supported : $idProperty")
                }
            }
        }

        return document
    }
}