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
import org.bson.BsonDocumentWriter
import org.bson.BsonString
import org.bson.codecs.Encoder
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

internal fun <TItem : Any> encodeValue(writer: BsonDocumentWriter, value: TItem?, codecRegistry: CodecRegistry) {
    when (value) {
        null -> writer.writeNull()
        is Bson -> @Suppress("UNCHECKED_CAST")
        (codecRegistry.get(BsonDocument::class.java) as Encoder<Any>).encode(
            writer,
            (value as Bson).toBsonDocument(BsonDocument::class.java, codecRegistry),
            EncoderContext.builder().build()
        )
        else -> @Suppress("UNCHECKED_CAST")
        (codecRegistry.get<TItem>(value::class.java as Class<TItem>) as Encoder<TItem>).encode(
            writer,
            value,
            EncoderContext.builder().build()
        )
    }
}