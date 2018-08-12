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

package org.litote.kmongo.util

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.codecs.configuration.CodecProvider
import org.litote.kmongo.jackson.JacksonCodecProvider
import org.litote.kmongo.jackson.ObjectMapperFactory
import kotlin.reflect.KClass

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

    /**
     * To change the default collection name strategy - default is camel case.
     */
    @Deprecated("Use CollectionNameFormatter.defaultCollectionNameBuilder")
    var defaultCollectionNameBuilder: (KClass<*>) -> String
        get() = CollectionNameFormatter.defaultCollectionNameBuilder
        set(value) {
            CollectionNameFormatter.defaultCollectionNameBuilder = value
        }

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
        extendedJsonMapper= ObjectMapperFactory.createExtendedJsonObjectMapper()
        bsonMapper = ObjectMapperFactory.createBsonObjectMapper()
        bsonMapperCopy = ObjectMapperFactory.createBsonObjectMapperCopy()
        currentJacksonCodecProvider = null
        currentFilterIdBsonMapper = null
    }

    /**
     * Use Camel Case default collection name builder.
     *
     * @param fromClass optional custom [KClass] -> [String] transformer (default is [KClass.simpleName])
     */
    @Deprecated("Use CollectionNameFormatter.useCamelCaseCollectionNameBuilder")
    fun useCamelCaseCollectionNameBuilder(fromClass: (KClass<*>) -> String = { it.simpleName!! }) {
        CollectionNameFormatter.useCamelCaseCollectionNameBuilder(fromClass)
    }

    /**
     * Use Snake Case default collection name builder.
     *
     * @param fromClass optional custom [KClass] -> [String] transformer (default is [KClass.simpleName])
     */
    @Deprecated("Use CollectionNameFormatter.useSnakeCaseCollectionNameBuilder")
    fun useSnakeCaseCollectionNameBuilder(fromClass: (KClass<*>) -> String = { it.simpleName!! }) {
        CollectionNameFormatter.useSnakeCaseCollectionNameBuilder(fromClass)
    }

    /**
     * Use Lower Case default collection name builder.
     *
     * @param fromClass optional custom [KClass] -> [String] transformer (default is [KClass.simpleName])
     */
    @Deprecated("Use CollectionNameFormatter.useLowerCaseCollectionNameBuilder")
    fun useLowerCaseCollectionNameBuilder(fromClass: (KClass<*>) -> String = { it.simpleName!! }) {
        CollectionNameFormatter.useLowerCaseCollectionNameBuilder(fromClass)
    }
}