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
package org.litote.kmongo.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.databind.node.ValueNode
import de.undercouch.bson4jackson.BsonParser
import de.undercouch.bson4jackson.deserializers.BsonDeserializer
import org.bson.types.Binary
import org.bson.types.ObjectId

internal class ExtendedJsonModule : SimpleModule() {

    internal object ObjectIdExtendedJsonSerializer : JsonSerializer<ObjectId>() {
        override fun serialize(value: ObjectId, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("\$oid", value.toHexString())
            gen.writeEndObject()
        }
    }

    internal object ObjectIdBsonDeserializer : BsonDeserializer<ObjectId>() {

        override fun deserialize(jp: BsonParser, ctxt: DeserializationContext): ObjectId? {
            val tree = jp.codec.readTree<TreeNode>(jp)
            if (tree.isObject) {
                val hexString = (tree.get("\$oid") as ValueNode).asText()
                return ObjectId(hexString)
            } else if (tree is POJONode) {
                return tree.pojo as ObjectId
            } else {
                throw ctxt.mappingException(ObjectId::class.java)
            }
        }
    }

    internal object BinaryExtendedJsonSerializer : JsonSerializer<Binary>() {

        override fun serialize(obj: Binary, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeBinaryField("\$binary", obj.data)
            jsonGenerator.writeStringField("\$type", Integer.toHexString(obj.type.toInt()).toUpperCase())
            jsonGenerator.writeEndObject()
        }
    }

    init {
        addSerializer(ObjectId::class.java, ObjectIdExtendedJsonSerializer)
        addSerializer(Binary::class.java, BinaryExtendedJsonSerializer)
    }
}


