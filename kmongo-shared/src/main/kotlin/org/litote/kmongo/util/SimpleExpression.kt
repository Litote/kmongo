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

package org.litote.kmongo.util

import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil.encodeValue

/**
 *
 */
internal class SimpleExpression<TExpression : Any>(
    private val name: String,
    private val expression: TExpression
) : Bson {

    override fun <TDocument> toBsonDocument(
        documentClass: Class<TDocument>,
        codecRegistry: CodecRegistry
    ): BsonDocument {
        val writer = BsonDocumentWriter(BsonDocument())

        writer.writeStartDocument()
        writer.writeName(name)
        encodeValue(writer, expression, codecRegistry)
        writer.writeEndDocument()

        return writer.document
    }

    override fun toString(): String {
        return ("Expression{"
                + "name='" + name + '\''.toString()
                + ", expression=" + expression
                + '}'.toString())
    }
}