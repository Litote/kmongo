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

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import com.github.jershell.kbson.Configuration
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decode
import kotlinx.serialization.encode
import org.bson.AbstractBsonReader
import org.bson.BsonReader
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.litote.kmongo.serialization.KMongoSerializationRepository.module
import org.litote.kmongo.service.ClassMappingType
import org.litote.kmongo.util.KMongoUtil.getIdBsonValue
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 *
 */
internal class SerializationCodec<T : Any>(
    private val clazz: KClass<T>,
    private val configuration: Configuration
) : CollectibleCodec<T> {

    private val idProperty: KProperty1<*, *>? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ClassMappingType.findIdProperty(clazz)
    }
    @ImplicitReflectionSerializer
    private val decoderSerializer: KSerializer<T> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        KMongoSerializationRepository.getSerializer(clazz)
    }

    override fun getEncoderClass(): Class<T> = clazz.java

    @ImplicitReflectionSerializer
    override fun encode(writer: BsonWriter, value: T, encoderContext: EncoderContext) {
        val serializer = KMongoSerializationRepository.getSerializer(clazz, value)
        BsonEncoder(writer, module, configuration).encode(serializer, value)
    }

    @ImplicitReflectionSerializer
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): T {
        return BsonFlexibleDecoder(reader as AbstractBsonReader, module, configuration).decode(decoderSerializer)
    }

    override fun getDocumentId(document: T): BsonValue {
        if (idProperty == null) {
            throw IllegalStateException("$clazz has no id field")
        } else {
            @Suppress("UNCHECKED_CAST")
            val idValue = getIdBsonValue((idProperty!!)(document))
            return idValue ?: throw IllegalStateException("$clazz has null id")
        }
    }

    override fun documentHasId(document: T): Boolean = idProperty != null

    override fun generateIdIfAbsentFromDocument(document: T): T {
        if (idProperty != null) {
            @Suppress("UNCHECKED_CAST")
            val idValue = ClassMappingType.getIdValue(idProperty as KProperty1<Any, Any>, document)
            if (idValue == null) {
                @Suppress("UNCHECKED_CAST")
                idController.setIdValue(idProperty!! as KProperty1<T, Any>, document)
            }
        }

        return document
    }
}