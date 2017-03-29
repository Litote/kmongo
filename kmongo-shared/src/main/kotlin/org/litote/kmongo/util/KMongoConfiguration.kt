/*
 * Copyright (C) 2016 Litote
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

import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.codecs.configuration.CodecProvider
import org.litote.kmongo.jackson.JacksonCodecProvider
import org.litote.kmongo.jackson.ObjectMapperFactory
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.reflect.KClass

/**
 * Use this class to customize the default behaviour of KMongo.
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
     * Basically a copy of [bsonMapper] without [de.undercouch.bson4jackson.BsonFactory].
     * Used by [org.litote.kmongo.jackson.JacksonCodec] to resolves specific serialization issues.
     */
    var bsonMapperCopy = ObjectMapperFactory.createBsonObjectMapperCopy()

    /**
     * To change the default collection name strategy.
     */
    var defaultCollectionNameBuilder: (KClass<*>) -> String = { it.simpleName!!.toLowerCase() }

    val jacksonCodecProvider: CodecProvider by lazy(PUBLICATION) {
        JacksonCodecProvider(bsonMapper, bsonMapperCopy)
    }

    val filterIdBsonMapper: ObjectMapper by lazy(PUBLICATION) {
        ObjectMapperFactory.createFilterIdObjectMapper(bsonMapper)
    }

    val filterIdExtendedJsonMapper: ObjectMapper by lazy(PUBLICATION) {
        ObjectMapperFactory.createFilterIdObjectMapper(extendedJsonMapper)
    }
}