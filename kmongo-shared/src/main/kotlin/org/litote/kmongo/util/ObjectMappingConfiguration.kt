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

package org.litote.kmongo.util

import org.bson.codecs.Codec
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdGenerator
import org.litote.kmongo.id.ObjectIdToStringGenerator
import org.litote.kmongo.service.ClassMappingType
import org.litote.kmongo.service.CustomCodecProvider

/**
 * Default object mapping configuration options.
 * Set values before KMongo initialization.
 */
object ObjectMappingConfiguration {

    /**
     * Are null value serialized?
     * Default to true for jackson & kotlinx.serialization.
     * Default to false for native driver (for backward compatibility).
     */
    @Volatile
    var serializeNull: Boolean = ClassMappingType.defaultNullSerialization

    /**
     * Adds a custom codec.
     */
    fun <T> addCustomCodec(codec: Codec<T>) {
        CustomCodecProvider.addCustomCodec(codec)
    }

    /**
     * Generates [Id] (using [newId()]) as String.
     */
    fun generateIdsAsStrings() {
        IdGenerator.defaultGenerator = ObjectIdToStringGenerator
    }

    /**
     * Generates [Id] (using [newId()]) as ObjectId (default behaviour).
     */
    fun generateIdsAsObjectIds() {
        IdGenerator.defaultGenerator = ObjectIdGenerator
    }
}