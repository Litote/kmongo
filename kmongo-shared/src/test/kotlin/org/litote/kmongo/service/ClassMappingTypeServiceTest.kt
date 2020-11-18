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

package org.litote.kmongo.service

import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertEquals

object ClassMappingType : ClassMappingTypeService {
    override fun priority(): Int {
        return 0
    }

    override fun filterIdToBson(obj: Any, filterNullProperties: Boolean): BsonDocument {
        error("unsupported")
    }

    override fun toExtendedJson(obj: Any?): String {
        error("unsupported")
    }

    override fun findIdProperty(type: KClass<*>): KProperty1<*, *>? {
        error("unsupported")
    }

    override fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R? {
        error("unsupported")
    }

    override fun coreCodecRegistry(baseCodecRegistry: CodecRegistry): CodecRegistry {
        error("unsupported")
    }

    override fun <T> calculatePath(property: KProperty<T>): String {
        return property.name
    }
}

/**
 *
 */
class ClassMappingTypeServiceTest {

    @Test
    fun `kotlin property is not cached`() {
        val p = String::class.memberProperties.first { it.name == "length" }
        val path = ClassMappingType.getPath(p)
        assertEquals("length", path)
        assertEquals(0, pathCache.size)
    }

    @Test
    fun `generated property is cached`() {
        val p = String::length
        val path = ClassMappingType.getPath(p)
        assertEquals("length", path)
        assertEquals(1, pathCache.size)
        assertEquals("length", ClassMappingType.getPath(p))
        assertEquals(1, pathCache.size)
    }
}