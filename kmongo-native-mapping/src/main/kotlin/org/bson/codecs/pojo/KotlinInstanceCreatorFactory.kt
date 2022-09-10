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

package org.bson.codecs.pojo

import org.bson.codecs.pojo.KMongoConvention.Companion.getInstantiator
import kotlin.reflect.KClass
import kotlin.reflect.jvm.isAccessible

/**
 *
 */
internal class KotlinInstanceCreatorFactory<T : Any>(val kClass: KClass<T>) : InstanceCreatorFactory<T> {

    private val instantiator = getInstantiator(kClass)?.apply { isAccessible = true }

    override fun create(): InstanceCreator<T> {
        return KotlinInstanceCreator(kClass, instantiator ?: error("No instantiator found for $kClass"))
    }
}