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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdGenerator

/**
 * Deserialize a [String] to an [Id].
 * @param idGenerator if null [IdGenerator.defaultGenerator] is used
 */
class StringToIdDeserializer(private val idGenerator: IdGenerator? = null) : JsonDeserializer<Id<*>>() {

    internal companion object {

        fun deserialize(idGenerator: IdGenerator? = null, s: String, ctxt: DeserializationContext): Id<*>
                = (idGenerator ?: IdGenerator.defaultGenerator).create(s)
    }


    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Id<*>
            = deserialize(idGenerator, p.text, ctxt)

}