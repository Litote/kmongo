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

import com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBRef
import org.bson.UuidRepresentation
import org.bson.types.ObjectId
import org.litote.jackson.registerModulesFromServiceLoader
import org.litote.kmongo.util.ObjectMappingConfiguration
import java.math.BigDecimal
import java.math.BigInteger

internal object ObjectMapperFactory {

    private class SetMappingModule : SimpleModule() {
        init {
            addAbstractTypeMapping(Set::class.java, LinkedHashSet::class.java)
        }
    }

    fun createExtendedJsonObjectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule()
            .registerModule(SetMappingModule())
            .registerModule(ExtendedJsonModule())
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .setSerializationInclusion(if (ObjectMappingConfiguration.serializeNull) ALWAYS else NON_NULL)
            .registerModulesFromServiceLoader()
    }

    fun createBsonObjectMapper(uuidRepresentation: UuidRepresentation? = null): ObjectMapper {
        return configureBson(ObjectMapper(KMongoBsonFactory()), uuidRepresentation)
    }

    fun createBsonObjectMapperCopy(uuidRepresentation: UuidRepresentation? = null): ObjectMapper {
        return configureBson(ObjectMapper(), uuidRepresentation)
    }

    private fun configureBson(mapper: ObjectMapper, uuidRepresentation: UuidRepresentation?): ObjectMapper {
        return mapper.registerModule(de.undercouch.bson4jackson.BsonModule())
            .registerKotlinModule()
            .registerModule(CustomJacksonModule)
            .registerModule(SetMappingModule())
            .registerModule(BsonModule(uuidRepresentation))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .setSerializationInclusion(if (ObjectMappingConfiguration.serializeNull) ALWAYS else NON_NULL)
            .addHandler(StringDeserializationProblemHandler)
            .registerModulesFromServiceLoader()
    }

    fun createFilterIdObjectMapper(objectMapper: ObjectMapper): ObjectMapper {
        return objectMapper.copy().registerModule(FilterIdModule())
    }

}

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
                    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): DBObject {
                        val map = jp.readValueAs(Map::class.java)
                        return BasicDBObject(map)
                    }
                })
        } catch (exception: Throwable) {
            //ignore - this is a sync driver class only
        }
    }

}