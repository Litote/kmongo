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

package org.litote.kmongo.serialization

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerialName
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

@Volatile
internal var idController: IdController = ReflectionIdController

/**
 * Set the current [IdController].
 */
fun changeIdController(controller: IdController) {
    idController = controller
}

/**
 *  To manage ids.
 */
interface IdController {

    fun findIdProperty(type: KClass<*>): KProperty1<*, *>? = null

    fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R? = null

    fun <T, R> setIdValue(idProperty: KProperty1<T, R>, instance: T) {}
}

/**
 * Default IdController implementation.
 */
object ReflectionIdController : IdController {

    @ImplicitReflectionSerializer
    override fun findIdProperty(type: KClass<*>): KProperty1<*, *>? {
        return type.declaredMemberProperties.find { it.name == "_id" || it.findAnnotation<SerialName>()?.value == "_id" }
    }

    override fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R? {
        return idProperty.run {
            isAccessible = true
            get(instance)
        }
    }

    override fun <T, R> setIdValue(idProperty: KProperty1<T, R>, instance: T) {
        val javaField = idProperty.javaField!!
        javaField.isAccessible = true
        javaField.set(instance, KMongoUtil.generateNewIdforIdClass(javaField.type.kotlin))
    }
}