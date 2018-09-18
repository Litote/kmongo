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

package org.litote.kmongo.service

import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.litote.kmongo.util.ObjectMappingConfiguration
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.internal.ReflectProperties.lazySoft

private val disablePathCache = System.getProperty("org.litote.kmongo.disablePathCache") == "true"

internal val pathCache: MutableMap<String, String>
        by lazySoft { ConcurrentHashMap<String, String>() }

/**
 *  Provides an object mapping utility using [java.util.ServiceLoader].
 */
interface ClassMappingTypeService {

    /**
     * Priority of this service. Greater is better.
     */
    fun priority(): Int

    fun filterIdToBson(obj: Any): BsonDocument

    fun toExtendedJson(obj: Any?): String

    fun findIdProperty(type: KClass<*>): KProperty1<*, *>?

    fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R?

    fun codecRegistry(codecRegistry: CodecRegistry): CodecRegistry {
        lateinit var codec: CodecRegistry
        codec = CodecRegistries.fromRegistries(
            codecRegistry,
            CodecRegistries.fromCodecs(
                ObjectMappingConfiguration.customCodecProviders.map { it.codec { codec } }
            ),
            coreCodecRegistry()
        )

        return codec
    }


    fun coreCodecRegistry(): CodecRegistry

    fun <T> getPath(property: KProperty<T>): String {
        //the idea is that KProperties are usually generated as (java) anonymous class
        //so we can safely store them as class name
        //we check that class package does not start with kotlin to avoid corner cases
        val javaClassName = property.javaClass.name
        return if (disablePathCache || javaClassName.startsWith("kotlin") || javaClassName == "org.litote.kmongo.property.KPropertyPath\$Companion\$CustomProperty") {
            calculatePath(property)
        } else {
            pathCache.getOrPut(javaClassName) { calculatePath(property) }
        }

    }

    fun <T> calculatePath(property: KProperty<T>): String
}