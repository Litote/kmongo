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

package org.litote.kmongo.pojo

import com.mongodb.MongoClientSettings
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.KMongoConvention
import org.bson.codecs.pojo.KMongoPojoCodecService
import org.bson.codecs.pojo.KMongoPojoCodecService.codecRegistry
import org.bson.codecs.pojo.KMongoPojoCodecService.codecRegistryWithNullSerialization
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.json.JsonMode
import org.bson.json.JsonWriter
import org.bson.json.JsonWriterSettings
import org.litote.kmongo.service.ClassMappingType
import org.litote.kmongo.service.ClassMappingTypeService
import org.litote.kmongo.util.ObjectMappingConfiguration
import java.io.StringWriter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

/**
 *
 */
internal class PojoClassMappingTypeService : ClassMappingTypeService {

    override fun priority(): Int {
        return 0
    }

    override fun filterIdToBson(obj: Any): BsonDocument {
        val bsonDocument = BsonDocument()
        val bsonWriter = BsonDocumentWriter(bsonDocument)
        ClassMappingType.codecRegistry(MongoClientSettings.getDefaultCodecRegistry())
            .get(obj.javaClass)
            ?.encode(
                bsonWriter,
                obj,
                EncoderContext.builder().build()
            )
        bsonDocument.remove("_id")
        return bsonDocument
    }

    override fun toExtendedJson(obj: Any?): String {
        return when (obj) {
            null -> "null"
            is Number -> obj.toString()
            is Array<*> -> toExtendedJson(obj.toList())
            is Pair<*, *> -> "{\"first\":${toExtendedJson(obj.first)},\"second\":${toExtendedJson(obj.second)}}"
            else -> {
                val writer = StringWriter()
                val jsonWriter = JsonWriter(
                    writer,
                    JsonWriterSettings
                        .builder()
                        .indent(false)
                        .outputMode(JsonMode.EXTENDED)
                        .build()
                )
                //create a fake document to bypass bson writer built-in checks
                jsonWriter.writeStartDocument()
                jsonWriter.writeName("tmp")
                ClassMappingType.codecRegistry(MongoClientSettings.getDefaultCodecRegistry())
                    .get(obj.javaClass)
                    ?.encode(
                        jsonWriter,
                        obj,
                        EncoderContext.builder().build()
                    )
                jsonWriter.writeEndDocument()
                writer.toString().run {
                    substring("{ \"tmp\" :".length, length - "}".length).trim()
                }
            }
        }
    }

    override fun findIdProperty(type: KClass<*>): KProperty1<*, *>? {
        return KMongoPojoCodecService
            .codecProvider
            .getClassModel(type)
            .idPropertyModel
            ?.run {
                type.memberProperties.find { it.name == name }
            }
    }

    override fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R? {
        return idProperty.get(instance)
    }

    override fun coreCodecRegistry(): CodecRegistry {
        return if (ObjectMappingConfiguration.serializeNull) {
            codecRegistryWithNullSerialization
        } else {
            codecRegistry
        }
    }

    override fun <T> calculatePath(property: KProperty<T>): String {
        val owner = property.javaField?.declaringClass
                ?: try {
                    property.javaGetter?.declaringClass
                } catch (e: Exception) {
                    null
                }

        return if (owner?.kotlin?.let { findIdProperty(it) }?.name == property.name)
            "_id"
        else {
            owner?.let {
                KMongoConvention
                    .getDeclaredAnnotations(property, it.kotlin)
                    .filterIsInstance<BsonProperty>()
                    .firstOrNull()
                    ?.value
            } ?: property.name
        }
    }
}
