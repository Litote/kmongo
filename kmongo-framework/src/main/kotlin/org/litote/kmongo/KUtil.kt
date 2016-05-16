/*
 * Copyright (C) 2016 Litote
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
import org.bson.codecs.BsonArrayCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.bson.json.JsonReader
import org.litote.kmongo.jackson.ObjectMapperFactory

/**
 *
 */
object KUtil {

    val EMPTY_BSON = "{}"

    fun toBson(json: String): Bson
            = if (json == EMPTY_BSON) BsonDocument() else BsonDocument.parse(json)

    fun toBson(o: Any): Bson
    //TODO o -> Bson directly
            = toBson(toExtendedJson(o))

    fun toBsonList(json: Array<out String>, codecRegistry: CodecRegistry): List<Bson>
            = if (json.size == 1 && isJsonArray(json[0])) {
        BsonArrayCodec(codecRegistry).decode(JsonReader(json[0]), DecoderContext.builder().build())
                .map { it as BsonDocument }
    } else {
        json.map { toBson(it) }
    }

    fun toExtendedJson(a : Any) : String
    = ObjectMapperFactory.extendedJsonMapper.writeValueAsString(a)

    private fun isJsonArray(json: String)
            = json.trim().startsWith('[')


}