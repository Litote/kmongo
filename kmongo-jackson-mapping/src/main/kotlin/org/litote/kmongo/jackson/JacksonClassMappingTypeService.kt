/*
 * Copyright (C) 2016/2022 Litote
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
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.PropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter.SerializeExceptFilter
import org.bson.BsonDocument
import org.bson.RawBsonDocument
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.litote.kmongo.service.ClassMappingTypeService
import org.litote.kmongo.util.KMongoConfiguration
import org.litote.kmongo.util.MongoIdUtil
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter


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
                else KMongoConfiguration.bsonMapperWithNullSerialization).writeValueAsBytes(obj)
            } else {
                filterIdWriter(
                    obj,
                    idProperty,
                    (if (filterNullProperties) KMongoConfiguration.filterIdBsonMapperWithoutNullSerialization
                    else KMongoConfiguration.filterIdBsonMapperWithNullSerialization)
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

    override fun coreCodecRegistry(baseCodecRegistry: CodecRegistry): CodecRegistry =
        CodecRegistries.fromProviders(KMongoConfiguration.jacksonCodecProvider)

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

    override fun resetConfiguration() {
        KMongoConfiguration.resetConfiguration()
    }
}