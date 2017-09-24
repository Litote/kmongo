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

import org.bson.BsonDouble
import org.bson.BsonInt32
import org.bson.BsonInt64
import org.bson.BsonObjectId
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.litote.kmongo.MongoId
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

/**
 *
 */
internal object MongoIdUtil {

    //TODO need to cache something here
    fun findIdProperty(type: KClass<*>): KProperty1<*, *>?
            = getAnnotatedMongoIdProperty(type) ?: getIdProperty(type)

    private fun getIdProperty(type: KClass<*>): KProperty1<*, *>?
            =
            try {
                type.memberProperties.find { "_id" == it.name }
            } catch (error: KotlinReflectionInternalError) {
                //ignore
                null
            }

    fun getAnnotatedMongoIdProperty(type: KClass<*>): KProperty1<*, *>?
            =
            try {
                type.memberProperties.find { p ->
                    p.javaField?.isAnnotationPresent(BsonId::class.java) == true
                            || p.getter.javaMethod?.isAnnotationPresent(BsonId::class.java) == true
                            || p.findAnnotation<MongoId>() != null
                }
            } catch (error: KotlinReflectionInternalError) {
                //ignore
                null
            }

    fun getIdValue(idProperty: KProperty1<*, *>, instance: Any): Any? {
        idProperty.isAccessible = true
        return (idProperty)(instance)
    }

    fun getIdBsonValue(idProperty: KProperty1<*, *>, instance: Any): BsonValue? {
        val idValue = (idProperty)(instance)
        return when (idValue) {
            null -> null
            is ObjectId -> BsonObjectId(idValue)
            is String -> BsonString(idValue)
            is Double -> BsonDouble(idValue)
            is Int -> BsonInt32(idValue)
            is Long -> BsonInt64(idValue)
        //TODO direct mapping
            else -> KMongoUtil.toBson(KMongoUtil.toExtendedJson(idValue))
        }
    }
}