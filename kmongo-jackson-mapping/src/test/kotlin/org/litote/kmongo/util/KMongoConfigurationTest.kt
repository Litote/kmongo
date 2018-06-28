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

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test

/**
 *
 */
class KMongoConfigurationTest {

    @Test
    fun `all object mappers have loaded modules`() {
        fun getRegisteredModuleTypes(mapper: ObjectMapper): Set<String> =
            ObjectMapper::class.java.getDeclaredField("_registeredModuleTypes")
                .run {
                    isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    get(mapper) as Set<String>
                }

        assert(getRegisteredModuleTypes(KMongoConfiguration.bsonMapper).contains(TestModule::class.qualifiedName))
        assert(getRegisteredModuleTypes(KMongoConfiguration.bsonMapperCopy).contains(TestModule::class.qualifiedName))
        assert(getRegisteredModuleTypes(KMongoConfiguration.extendedJsonMapper).contains(TestModule::class.qualifiedName))
    }
}