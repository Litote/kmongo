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

package org.bson.codecs.pojo

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObjectInstance

/**
 *
 */
internal class KotlinInstanceCreator<T : Any>(val kClass: KClass<T>, val instantiator: KFunction<*>) :
    InstanceCreator<T> {

    private val properties: MutableMap<String, Pair<Any?, PropertyModel<Any>>> = mutableMapOf()

    override fun <S : Any?> set(value: S, propertyModel: PropertyModel<S>) {
        @Suppress("UNCHECKED_CAST")
        properties[propertyModel.name] = value to propertyModel as PropertyModel<Any>
    }

    override fun getInstance(): T {
        val params = instantiator
            .parameters
            .asSequence()
            .mapNotNull { paramDef ->
                if (paramDef.kind == KParameter.Kind.INSTANCE) {
                    paramDef to kClass.companionObjectInstance
                } else {
                    val name = paramDef.name
                    val pair = properties.remove(name)

                    if (pair == null && paramDef.isOptional) {
                        null
                    } else {
                        val value = pair?.first
                        if (value == null && !paramDef.type.isMarkedNullable) {
                            throw MissingKotlinParameterException(
                                "Instantiation of $kClass value failed for property $name due to missing (therefore NULL) value for creator parameter $name which is a non-nullable type"
                            )
                        }
                        paramDef to value
                    }
                }
            }
            .toMap()

        @Suppress("UNCHECKED_CAST")
        val result = instantiator.callBy(params) as T

        //now try to set remaining properties
        properties.values.forEach { (v, model) ->
            if (model.isWritable) {
                model.propertyAccessor.set(result, v)
            }
        }

        return result
    }

}