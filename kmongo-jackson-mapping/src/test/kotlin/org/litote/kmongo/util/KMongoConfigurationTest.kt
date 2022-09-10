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

package org.litote.kmongo.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.Test
import org.litote.kmongo.jackson.JacksonCodecProvider
import kotlin.reflect.KClass
import kotlin.test.assertFalse

class Module1 : SimpleModule()

class Module2 : SimpleModule()

/**
 *
 */
class KMongoConfigurationTest {

    private fun getRegisteredModuleTypes(mapper: ObjectMapper): Set<String> =
        ObjectMapper::class.java.getDeclaredField("_registeredModuleTypes")
            .run {
                isAccessible = true
                @Suppress("UNCHECKED_CAST")
                get(mapper) as Set<String>
            }

    private fun containsModule(mapper: ObjectMapper, moduleClass: KClass<out SimpleModule>) =
        getRegisteredModuleTypes(mapper).contains(moduleClass.qualifiedName)

    @Test
    fun `all object mappers have loaded modules`() {
        assert(containsModule(KMongoConfiguration.bsonMapper, TestModule::class))
        assert(containsModule(KMongoConfiguration.bsonMapperCopy, TestModule::class))
        assert(containsModule(KMongoConfiguration.extendedJsonMapper, TestModule::class))
        assert(containsModule(KMongoConfiguration.filterIdBsonMapper, TestModule::class))
        assert(containsModule(KMongoConfiguration.bsonMapperWithNullSerialization, TestModule::class))
        assert(containsModule(KMongoConfiguration.bsonMapperWithoutNullSerialization, TestModule::class))
        assert(containsModule(KMongoConfiguration.filterIdBsonMapperWithoutNullSerialization, TestModule::class))
        assert(containsModule(KMongoConfiguration.filterIdBsonMapperWithNullSerialization, TestModule::class))

    }

    @Test
    fun `resetConfiguration does not keep old jackson modules`() {
        KMongoConfiguration.resetConfiguration()

        KMongoConfiguration.registerBsonModule(Module1())
        var codecProvider = KMongoConfiguration.jacksonCodecProvider
        assert(containsModule(codecProvider.bsonObjectMapper, Module1::class))
        assert(containsModule(codecProvider.notBsonObjectMapper, Module1::class))
        assert(containsModule(KMongoConfiguration.bsonMapperWithNullSerialization, Module1::class))
        assert(containsModule(KMongoConfiguration.bsonMapperWithoutNullSerialization, Module1::class))
        assert(containsModule(KMongoConfiguration.filterIdBsonMapperWithoutNullSerialization, Module1::class))
        assert(containsModule(KMongoConfiguration.filterIdBsonMapperWithNullSerialization, Module1::class))

        KMongoConfiguration.resetConfiguration()

        codecProvider = KMongoConfiguration.jacksonCodecProvider
        assertFalse(containsModule(codecProvider.bsonObjectMapper, Module1::class))
        assertFalse(containsModule(codecProvider.notBsonObjectMapper, Module1::class))
        assertFalse(containsModule(KMongoConfiguration.bsonMapperWithNullSerialization, Module1::class))
        assertFalse(containsModule(KMongoConfiguration.bsonMapperWithoutNullSerialization, Module1::class))
        assertFalse(containsModule(KMongoConfiguration.filterIdBsonMapperWithoutNullSerialization, Module1::class))
        assertFalse(containsModule(KMongoConfiguration.filterIdBsonMapperWithNullSerialization, Module1::class))

        KMongoConfiguration.resetConfiguration()

        KMongoConfiguration.registerBsonModule(Module2())
        codecProvider = KMongoConfiguration.jacksonCodecProvider
        assertFalse(containsModule(codecProvider.bsonObjectMapper, Module1::class))
        assertFalse(containsModule(codecProvider.notBsonObjectMapper, Module1::class))
        assertFalse(containsModule(KMongoConfiguration.bsonMapperWithNullSerialization, Module1::class))
        assertFalse(containsModule(KMongoConfiguration.bsonMapperWithoutNullSerialization, Module1::class))
        assertFalse(containsModule(KMongoConfiguration.filterIdBsonMapperWithoutNullSerialization, Module1::class))
        assertFalse(containsModule(KMongoConfiguration.filterIdBsonMapperWithNullSerialization, Module1::class))
        assert(containsModule(codecProvider.bsonObjectMapper, Module2::class))
        assert(containsModule(codecProvider.notBsonObjectMapper, Module2::class))
        assert(containsModule(KMongoConfiguration.bsonMapperWithNullSerialization, Module2::class))
        assert(containsModule(KMongoConfiguration.bsonMapperWithoutNullSerialization, Module2::class))
        assert(containsModule(KMongoConfiguration.filterIdBsonMapperWithoutNullSerialization, Module2::class))
        assert(containsModule(KMongoConfiguration.filterIdBsonMapperWithNullSerialization, Module2::class))
    }
}