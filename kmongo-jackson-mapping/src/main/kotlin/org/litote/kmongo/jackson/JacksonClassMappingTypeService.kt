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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.PropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter.SerializeExceptFilter
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBRef
import org.bson.BsonDocument
import org.bson.RawBsonDocument
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import org.litote.kmongo.service.ClassMappingTypeService
import org.litote.kmongo.util.KMongoConfiguration
import org.litote.kmongo.util.KMongoConfiguration.registerBsonModule
import org.litote.kmongo.util.MongoIdUtil
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

@Volatile
internal var customModuleInitialized: Boolean = false

private object CustomJacksonModule : SimpleModule() {

    init {
        addSerializer(DBRef::class.java, object : JsonSerializer<DBRef>() {
            override fun serialize(value: DBRef?, gen: JsonGenerator, serializers: SerializerProvider) {
                if (value == null) {
                    gen.writeNull()
                } else {
                    gen.writeStartObject()
                    gen.writeStringField("\$ref", value.collectionName)
                    gen.writeFieldName("\$id")
                    val id = value.id
                    when (id) {
                        is String -> gen.writeString(id)
                        is Long -> gen.writeNumber(id)
                        is Int -> gen.writeNumber(id)
                        is Float -> gen.writeNumber(id)
                        is Double -> gen.writeNumber(id)
                        is BigInteger -> gen.writeNumber(id)
                        is BigDecimal -> gen.writeNumber(id)
                        is ObjectId -> gen.writeObjectId(id)
                        else -> error("dbRef with id $id of type ${id.javaClass} is not supported")
                    }
                    if (value.databaseName != null) {
                        gen.writeStringField("\$db", value.databaseName)
                    }
                    gen.writeEndObject()
                }
            }
        })
        addDeserializer(DBRef::class.java, object : JsonDeserializer<DBRef>() {
            override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): DBRef? {
                return if (jp.isExpectedStartObjectToken) {
                    jp.nextValue()
                    val ref = jp.valueAsString
                    jp.nextValue()
                    val id = when (jp.currentToken) {
                        JsonToken.VALUE_EMBEDDED_OBJECT -> jp.embeddedObject
                        JsonToken.VALUE_STRING -> jp.valueAsString
                        else -> jp.decimalValue
                    }
                    var db: String? = null
                    while (jp.currentToken != JsonToken.END_OBJECT) {
                        if (jp.currentName == "\$db") {
                            db = jp.valueAsString
                        }
                        jp.nextToken()
                    }
                    DBRef(db, ref, id)
                } else {
                    null
                }
            }
        })

        //check DBObject exists
        try {
            addSerializer(DBObject::class.java, object : JsonSerializer<DBObject>() {
                override fun serialize(
                    value: DBObject,
                    gen: JsonGenerator,
                    serializers: SerializerProvider
                ) {
                    val map = value.toMap()
                    serializers
                        .findTypedValueSerializer(map::class.java, true, null)
                        .serialize(map, gen, serializers)
                }
            })
                .addDeserializer(DBObject::class.java, object : JsonDeserializer<DBObject>() {
                    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): DBObject? {
                        val map = jp.readValueAs(Map::class.java)
                        return BasicDBObject(map)
                    }
                })
        } catch (exception: Throwable) {
            //ignore - this is a sync driver class only
        }
    }

}

/**
 *
 */
internal class JacksonClassMappingTypeService : ClassMappingTypeService {

    override fun priority(): Int {
        return 100
    }

    override fun filterIdToBson(obj: Any, filterNullProperties: Boolean): BsonDocument {
        val idProperty = MongoIdUtil.findIdProperty(obj.javaClass.kotlin)
        return RawBsonDocument(
            if (idProperty == null) {
                (if (filterNullProperties) KMongoConfiguration.bsonMapperWithoutNullSerialization
                else KMongoConfiguration.bsonMapper).writeValueAsBytes(obj)
            } else {
                filterIdWriter(
                    obj,
                    idProperty,
                    (if (filterNullProperties) KMongoConfiguration.currentFilterIdBsonMapperWithoutNullSerialization
                    else KMongoConfiguration.filterIdBsonMapper)
                )
                    .writeValueAsBytes(obj)
            }
        )
    }

    override fun toExtendedJson(obj: Any?): String {
        return KMongoConfiguration.extendedJsonMapper.writeValueAsString(obj)
    }

    private fun filterIdWriter(
        obj: Any,
        idProperty: KProperty1<*, *>,
        mapper: ObjectMapper
    ): ObjectWriter {
        return mapper.writer(
            object : FilterProvider() {

                override fun findFilter(filterId: Any): BeanPropertyFilter? {
                    throw UnsupportedOperationException()
                }

                override fun findPropertyFilter(filterId: Any, valueToFilter: Any): PropertyFilter? {
                    return if (valueToFilter === obj) {
                        SerializeExceptFilter(setOf(idProperty.name))
                    } else SimpleBeanPropertyFilter.serializeAll()
                }
            }
        )
    }

    override fun findIdProperty(type: KClass<*>): KProperty1<*, *>? {
        return MongoIdUtil.findIdProperty(type)
    }

    override fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R? {
        @Suppress("UNCHECKED_CAST")
        return MongoIdUtil.getIdValue(idProperty as KProperty1<Any, *>, instance as Any) as R?
    }

    override fun coreCodecRegistry(baseCodecRegistry: CodecRegistry): CodecRegistry {
        if (!customModuleInitialized) {
            customModuleInitialized = true
            registerBsonModule(CustomJacksonModule)
        }

        return CodecRegistries.fromProviders(KMongoConfiguration.jacksonCodecProvider)
    }

    override fun <T> calculatePath(property: KProperty<T>): String {
        val owner = property.javaField?.declaringClass
                ?: try {
                    property.javaGetter?.declaringClass
                } catch (e: Exception) {
                    null
                }
        return if (
            owner?.kotlin
                ?.let {
                    findIdProperty(it)?.name == property.name
                } == true
        ) "_id"
        else KMongoConfiguration
            .extendedJsonMapper
            .deserializationConfig
            ?.let { config ->
                owner?.let {
                    config
                        .classIntrospector
                        .forDeserialization(
                            config,
                            KMongoConfiguration.extendedJsonMapper.constructType(it),
                            config
                        )
                        .findProperties()
                        .firstOrNull { beanDef ->
                            beanDef.accessor?.member?.let { member ->
                                member.name == property.javaGetter?.name || member.name == property.javaField?.name
                            } ?: false
                        }
                }
            }
            ?.name ?: property.name
    }
}