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
package org.litote.kmongo.serialization

import kotlinx.serialization.SerialName
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.json.JsonMode
import org.bson.json.JsonWriter
import org.bson.json.JsonWriterSettings
import org.litote.kmongo.id.MongoId
import org.litote.kmongo.id.MongoProperty
import org.litote.kmongo.service.ClassMappingTypeService
import java.io.StringWriter
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * kotlinx serialization ClassMapping.
 */
class SerializationClassMappingTypeService : ClassMappingTypeService {

    override fun priority(): Int = 200

    private val coreCodecRegistry: CodecRegistry by lazy(PUBLICATION) {
        SerializationCodecRegistry(configuration)
    }

    @Volatile
    private lateinit var codecRegistry: CodecRegistry

    @Volatile
    private lateinit var codecRegistryWithNonEncodeNull: CodecRegistry

    override fun filterIdToBson(obj: Any, filterNullProperties: Boolean): BsonDocument {
        val doc = BsonDocument()
        val writer = BsonDocumentWriter(doc)

        (if (filterNullProperties) codecRegistryWithNonEncodeNull else codecRegistry)
            .get(obj.javaClass).encode(writer, obj, EncoderContext.builder().build())

        writer.flush()

        doc.remove("_id")
        return doc
    }

    override fun toExtendedJson(obj: Any?): String {
        return if (obj == null) {
            "null"
        } else {
            val writer = StringWriter()
            val jsonWriter = JsonWriter(
                writer,
                JsonWriterSettings
                    .builder()
                    .indent(false)
                    .outputMode(JsonMode.RELAXED)
                    .build()
            )
            //create a fake document to bypass bson writer built-in checks
            jsonWriter.writeStartDocument()
            jsonWriter.writeName("tmp")
            codecRegistryWithNonEncodeNull.get(obj.javaClass).encode(jsonWriter, obj, EncoderContext.builder().build())
            jsonWriter.writeEndDocument()
            writer.toString().run {
                substring("{ \"tmp\":".length, length - "}".length).trim()
            }
        }
    }

    override fun findIdProperty(type: KClass<*>): KProperty1<*, *>? = idController.findIdProperty(type)

    override fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R? =
        idController.getIdValue(idProperty, instance)

    override fun coreCodecRegistry(baseCodecRegistry: CodecRegistry): CodecRegistry {
        codecRegistry = codecRegistry(
            baseCodecRegistry,
            SerializationCodecRegistry(configuration)
        )
        codecRegistryWithNonEncodeNull =
            codecRegistry(
                baseCodecRegistry,
                SerializationCodecRegistry(configuration.copy(nonEncodeNull = true))
            )
        return coreCodecRegistry
    }

    override fun <T> calculatePath(property: KProperty<T>): String =
        property.findAnnotation<SerialName>()?.value
                ?: (if (property.hasAnnotation<MongoId>()) "_id" else property.findAnnotation<MongoProperty>()?.value)
                ?: property.name
}