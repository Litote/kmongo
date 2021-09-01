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

package org.litote.kmongo.util

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.util.MongoIdUtil.IdPropertyWrapper.Companion.NO_ID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError
import kotlin.reflect.jvm.internal.ReflectProperties.lazySoft
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

/**
 * Returns the Mongo Id property of the [KClass],
 * or null if no id property is found.
 */
@Suppress("UNCHECKED_CAST")
val KClass<*>.idProperty: KProperty1<Any, *>?
    get() = MongoIdUtil.findIdProperty(this) as KProperty1<Any, *>?

/**
 * Returns the Mongo Id value (which can be null),
 * or null if no id property is found.
 */
val Any?.idValue: Any?
    get() = this?.javaClass?.kotlin?.idProperty?.get(this)

internal object MongoIdUtil {

    private sealed class IdPropertyWrapper {

        companion object {
            val NO_ID = NoIdProperty()
        }

        val property: KProperty1<*, *>?
            get() = when (this) {
                is NoIdProperty -> null
                is IdProperty -> prop
            }

        class NoIdProperty : IdPropertyWrapper()
        class IdProperty(val prop: KProperty1<*, *>) : IdPropertyWrapper()
    }

    private val propertyIdCache: MutableMap<KClass<*>, IdPropertyWrapper>
        by lazySoft { ConcurrentHashMap<KClass<*>, IdPropertyWrapper>() }

    fun findIdProperty(type: KClass<*>): KProperty1<*, *>? =
        propertyIdCache.getOrPut(type) {
            (getAnnotatedMongoIdProperty(type)
                ?: getIdProperty(type))
                ?.let { IdPropertyWrapper.IdProperty(it) }
                ?: NO_ID

        }.property

    private fun getIdProperty(type: KClass<*>): KProperty1<*, *>? =
        try {
            val idEnabled = System.getProperty("kmongo.id.enabled").toBoolean()
            type.memberProperties.find { "_id" == it.name || (idEnabled && "id" == it.name) }
        } catch (error: KotlinReflectionInternalError) {
            //ignore
            null
        }

    private fun getAnnotatedMongoIdProperty(type: KClass<*>): KProperty1<*, *>? =
        try {
            val parameter = findPrimaryConstructorParameter(type)
            if (parameter != null) {
                type.memberProperties.firstOrNull { it.name == parameter.name }
            } else {
                type.memberProperties.find { p ->
                    p.javaField?.isAnnotationPresent(BsonId::class.java) == true
                        || p.getter.javaMethod?.isAnnotationPresent(BsonId::class.java) == true
                }
            }
        } catch (error: KotlinReflectionInternalError) {
            //ignore
            null
        }

    private fun findPrimaryConstructorParameter(type: KClass<*>): KParameter? =
        try {
            type.primaryConstructor?.parameters?.firstOrNull { it.findAnnotation<BsonId>() != null }
                ?: type.superclasses
                    .asSequence()
                    .map { findPrimaryConstructorParameter(it) }
                    .filterNotNull()
                    .firstOrNull()
        } catch (error: KotlinReflectionInternalError) {
            //ignore
            null
        }

    fun getIdValue(idProperty: KProperty1<Any, *>, instance: Any): Any? {
        idProperty.isAccessible = true
        return idProperty.get(instance)
    }
}