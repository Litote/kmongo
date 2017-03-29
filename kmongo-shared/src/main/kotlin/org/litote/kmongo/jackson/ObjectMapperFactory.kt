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
package org.litote.kmongo.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import de.undercouch.bson4jackson.BsonParser

internal object ObjectMapperFactory {

    fun createExtendedJsonObjectMapper(): ObjectMapper {
        return ObjectMapper()
                .registerKotlinModule()
                .registerModule(ExtendedJsonModule())
    }

    fun createBsonObjectMapper(): ObjectMapper {
        val bsonFactory = KMongoBsonFactory()
        bsonFactory.enable(BsonParser.Feature.HONOR_DOCUMENT_LENGTH)

        return configureBson(ObjectMapper(bsonFactory))
    }

    fun createBsonObjectMapperCopy(): ObjectMapper {
        return configureBson(ObjectMapper())
    }

    private fun configureBson(mapper: ObjectMapper): ObjectMapper {
        return mapper.registerModule(de.undercouch.bson4jackson.BsonModule())
                .registerKotlinModule()
                .registerModule(BsonModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun createFilterIdObjectMapper(objectMapper: ObjectMapper): ObjectMapper {
        val newObjectMapper = objectMapper.copy()
                .setFilterProvider(FilterIdModule.IdPropertyFilterProvider)
                .registerModule(FilterIdModule())
        return newObjectMapper
    }
}