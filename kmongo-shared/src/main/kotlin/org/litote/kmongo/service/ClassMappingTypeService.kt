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

package org.litote.kmongo.service

import org.bson.BsonDocument
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.litote.kmongo.util.KMongoUtil
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.internal.ReflectProperties.lazySoft

private val disablePathCache = System.getProperty("org.litote.kmongo.disablePathCache") == "true"

internal val pathCache: MutableMap<String, String>
        by lazySoft { ConcurrentHashMap<String, String>() }

private val kPropertyPathClass =
    try {
        Class.forName("org.litote.kmongo.property.KPropertyPath")
    } catch (e: Exception) {
        null
    }

internal object CustomCodecProvider : CodecProvider {

    private val customCodecMap = ConcurrentHashMap<Class<*>, Codec<*>>()

    fun <T> addCustomCodec(codec: Codec<T>) {
        customCodecMap[codec.encoderClass] = codec
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? =
        customCodecMap[clazz] as? Codec<T>
}

/**
 *  Provides an object mapping utility using [java.util.ServiceLoader].
 */
interface ClassMappingTypeService {

    /**
     * Priority of this service. Greater is better.
     */
    fun priority(): Int

    fun filterIdToBson(obj: Any, filterNullProperties: Boolean = false): BsonDocument

    fun toExtendedJson(obj: Any?): String

    fun findIdProperty(type: KClass<*>): KProperty1<*, *>?

    fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R?

    /**
     * Returns a codec registry built with [baseCodecRegistry], and [coreCodeRegistry].
     */
    fun codecRegistry(
        baseCodecRegistry: CodecRegistry,
        coreCodeRegistry: CodecRegistry = coreCodecRegistry(baseCodecRegistry)
    ): CodecRegistry = CodecRegistries.fromProviders(
        baseCodecRegistry,
        CustomCodecProvider,
        coreCodeRegistry
    )

    fun coreCodecRegistry(baseCodecRegistry: CodecRegistry = KMongoUtil.defaultCodecRegistry): CodecRegistry

    fun <T> getPath(property: KProperty<T>): String {
        //sanity check
        if (kPropertyPathClass?.isInstance(property) == true) {
            return calculatePath(property)
        }
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

    val defaultNullSerialization: Boolean get() = true

    fun resetConfiguration() {}
}