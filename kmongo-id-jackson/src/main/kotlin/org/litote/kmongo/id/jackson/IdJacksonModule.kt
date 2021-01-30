/*
 * Copyright (C) 2016/2021 Litote
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

package org.litote.kmongo.id.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdGenerator

/**
 * Add support for serialization and deserialization of [Id] to or from json [String].
 * The [IdGenerator] used must have a public constructor with only one String argument.
 * @param idGenerator if null [IdGenerator.defaultGenerator] is used
 */
class IdJacksonModule(idGenerator: IdGenerator? = null) : SimpleModule() {

    init {
        addSerializer(Id::class.java, IdToStringSerializer())
        addDeserializer(Id::class.java, StringToIdDeserializer(idGenerator))
        addKeySerializer(Id::class.java, IdKeySerializer())
        addKeyDeserializer(Id::class.java, IdKeyDeserializer(idGenerator))
    }
}