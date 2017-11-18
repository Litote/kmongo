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

package org.litote.kmongo.id

import org.litote.kmongo.Id
import kotlin.reflect.KClass

/**
 * A generator of Ids.
 */
interface IdGenerator {

    companion object {

        var defaultGenerator: IdGenerator
            get() = defaultIdGenerator
            set(value) {
                defaultIdGenerator = value
                initialized = true
            }

        val defaultGeneratorInitialized: Boolean get() = initialized

        @Volatile
        private var defaultIdGenerator: IdGenerator = UUIDStringIdGenerator

        @Volatile
        private var initialized: Boolean = false

    }

    /**
     * The class of the id.
     */
    val idClass: KClass<out Id<*>>

    /**
     * The class of the wrapped id.
     */
    val wrappedIdClass: KClass<out Any>

    /**
     * Generate a new id.
     */
    fun <T> generateNewId(): Id<T>
}