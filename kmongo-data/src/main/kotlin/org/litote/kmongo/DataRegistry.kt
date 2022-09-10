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

package org.litote.kmongo

import kotlin.reflect.KClass

/**
 * Give the same behaviour than @[Data] to the specified KClass array.
 * Useful if you can't annotate directly the target classes.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DataRegistry(
    /**
     * List of data classes.
     */
    val value: Array<KClass<*>>,
    /**
     * Set to internal visibility the generated classes.
     */
    val internal: Boolean = false
)