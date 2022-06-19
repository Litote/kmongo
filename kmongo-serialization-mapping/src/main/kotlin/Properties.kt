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

package org.litote.kmongo.serialization

import org.litote.kmongo.path
import kotlin.reflect.KProperty
import kotlin.reflect.KClass

/**
 * Provides the path for subtype discriminator, in order to write this kind of filter:
 *
 * ```eq(subtypePath, B::class.subtypeQualifier)```
 */
val subtypePath: String get() = configuration.classDiscriminator

/**
 * Provides the path for subtype discriminator, in order to write this kind of filter:
 *
 * ```eq(A::myProperty.subtypePath, B::class.subtypeQualifier)```
 */
val KProperty<*>.subtypePath: String get() = "${path()}.${configuration.classDiscriminator}"

/**
 * Provides the value of subtype discriminator.
 */
val KClass<*>.subtypeQualifier: String get() = qualifiedName ?: error("no qualified name for class $this")