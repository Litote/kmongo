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

import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistry
import org.litote.kmongo.service.ClassMappingTypeService
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 *
 */
class MockedClassMappingTypeService : ClassMappingTypeService {

    override fun priority(): Int {
        return 1000
    }

    override fun filterIdToBson(obj: Any): BsonDocument = error("not implemented")

    override fun toExtendedJson(obj: Any?): String = error("not implemented")

    override fun filterIdToExtendedJson(obj: Any): String = error("not implemented")

    override fun findIdProperty(type: KClass<*>): KProperty1<*, *>? = error("not implemented")

    override fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R? = error("not implemented")

    override fun codecRegistry(): CodecRegistry = error("not implemented")

    override fun <T> getPath(property: KProperty<T>): String = property.name
}