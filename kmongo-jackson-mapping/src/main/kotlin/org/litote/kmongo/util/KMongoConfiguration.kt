/*
 * Copyright (C) 2016/2020 Litote
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

package org.litote.kmongo.util

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.codecs.configuration.CodecProvider
import org.litote.kmongo.jackson.JacksonCodecProvider
import org.litote.kmongo.jackson.ObjectMapperFactory
import org.litote.kmongo.jackson.customModuleInitialized
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * Use this class to customize the default behaviour of KMongo jackson bindings.
 */
object KMongoConfiguration {

    /**
     * Manage mongo extended json format.
     */
    var extendedJsonMapper: ObjectMapper = ObjectMapperFactory.createExtendedJsonObjectMapper()

    /**
     * Manage bson format.
     */
    var bsonMapper: ObjectMapper = ObjectMapperFactory.createBsonObjectMapper()

    /**
     * Basically a copy of [bsonMapper] without [org.litote.bson4jackson.BsonFactory].
     * Used by [org.litote.kmongo.jackson.JacksonCodec] to resolves specific serialization issues.
     */
    var bsonMapperCopy: ObjectMapper = ObjectMapperFactory.createBsonObjectMapperCopy()

    @Volatile
    private var currentJacksonCodecProvider: CodecProvider? = null

    internal val jacksonCodecProvider: CodecProvider
        get() {
            if (currentJacksonCodecProvider == null) {
                currentJacksonCodecProvider = JacksonCodecProvider(bsonMapper, bsonMapperCopy)
            }
            return currentJacksonCodecProvider!!
        }

    @Volatile
    private var currentFilterIdBsonMapper: ObjectMapper? = null

    internal val filterIdBsonMapper: ObjectMapper
        get() {
            if (currentFilterIdBsonMapper == null) {
                currentFilterIdBsonMapper = ObjectMapperFactory.createFilterIdObjectMapper(bsonMapper)
            }
            return currentFilterIdBsonMapper!!
        }

    internal val bsonMapperWithoutNullSerialization: ObjectMapper by lazy(PUBLICATION) {
        bsonMapper.copy().setSerializationInclusion(NON_NULL);
    }

    internal val currentFilterIdBsonMapperWithoutNullSerialization: ObjectMapper by lazy(PUBLICATION) {
        filterIdBsonMapper.copy().setSerializationInclusion(NON_NULL);
    }

    /**
     * Register a jackson [Module] for the two bson mappers, [bsonMapper] and [bsonMapperCopy].
     *
     * For example, if you need to manage [DBRefs](https://docs.mongodb.com/manual/reference/database-references/) autoloading,
     * you can write this kind of module:
     *
     *      class KMongoBeanDeserializer(deserializer:BeanDeserializer) : ThrowableDeserializer(deserializer) {
     *
     *              override fun deserializeFromObject(jp: JsonParser, ctxt: DeserializationContext): Any? {
     *                   if(jp.currentName == "\$ref") {
     *                       val ref = jp.nextTextValue()
     *                       jp.nextValue()
     *                       val id = jp.getValueAsString()
     *                       while(jp.currentToken != JsonToken.END_OBJECT) jp.nextToken()
     *                       return database.getCollection(ref).withDocumentClass(_beanType.rawClass).findOneById(id)
     *                    } else {
     *                       return super.deserializeFromObject(jp, ctxt)
     *                    }
     *                   }
     *               }
     *
     *       class KMongoBeanDeserializerModifier : BeanDeserializerModifier() {
     *
     *              override fun modifyDeserializer(config: DeserializationConfig, beanDesc: BeanDescription, deserializer: JsonDeserializer<*>): JsonDeserializer<*> {
     *                  return if(deserializer is BeanDeserializer) {
     *                          KMongoBeanDeserializer( deserializer)
     *                         } else {
     *                          deserializer
     *                         }
     *                  }
     *              }
     *
     *       KMongoConfiguration.registerBsonModule(SimpleModule().setDeserializerModifier(KMongoBeanDeserializerModifier()))
     */
    fun registerBsonModule(module: Module) {
        bsonMapper.registerModule(module)
        bsonMapperCopy.registerModule(module)
    }

    /**
     * Reset the jackson configuration.
     *
     * Useful if you need to manage hot class reloading (see https://github.com/Litote/kmongo/issues/75 )
     *
     * Usage:
     *
     *  KMongoConfiguration.registerBsonModule(MyModule())
     *  client = KMongo.createClient(..)
     *
     *  // then reloading
     *  KMongoConfiguration.resetConfiguration()
     *  KMongoConfiguration.registerBsonModule(MyModule())
     *  client = KMongo.createClient(..)
     */
    fun resetConfiguration() {
        extendedJsonMapper = ObjectMapperFactory.createExtendedJsonObjectMapper()
        bsonMapper = ObjectMapperFactory.createBsonObjectMapper()
        bsonMapperCopy = ObjectMapperFactory.createBsonObjectMapperCopy()
        currentJacksonCodecProvider = null
        currentFilterIdBsonMapper = null
        customModuleInitialized = false
    }

}