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

package org.litote.kmongo

import org.litote.kmongo.service.ClassMappingType
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Returns a composed property. For example Friend.address / Address.postalCode = "friend.address.postalCode".
 */
operator fun <T0, T1, T2> KProperty1<T0, T1?>.div(p2: KProperty1<T1, T2?>): KProperty1<T0, T2?> =
    KPropertyPath(this, p2)

/**
 * Returns a mongo path of a property.
 */
fun <T> KProperty<T>.path(): String {
    return if (this is KPropertyPath<*, T>) path else ClassMappingType.getPath(this)
}