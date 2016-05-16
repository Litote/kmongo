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

import com.fasterxml.jackson.core.Base64Variants
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.databind.node.ValueNode
import org.bson.types.Binary
import org.bson.types.ObjectId
import org.litote.kmongo.jackson.ExtendedJsonModule.BinaryExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.ObjectIdExtendedJsonSerializer
import java.io.IOException


internal class BsonModule : SimpleModule() {

    private object ObjectIdBsonSerializer : JsonSerializer<ObjectId>() {

        override fun serialize(objectId: ObjectId, gen: JsonGenerator, serializerProvider: SerializerProvider) {
            if (gen is KMongoBsonFactory.KMongoBsonGenerator) {
                gen.writeObjectId(objectId)
            } else {
                ObjectIdExtendedJsonSerializer.serialize(objectId, gen, serializerProvider)
            }
        }
    }

    private object BinaryBsonSerializer : JsonSerializer<Binary>() {

        override fun serialize(obj: Binary, gen: JsonGenerator, serializerProvider: SerializerProvider) {
            if (gen is KMongoBsonFactory.KMongoBsonGenerator) {
                gen.writeBinary(obj)
            } else {
                BinaryExtendedJsonSerializer.serialize(obj, gen, serializerProvider)
            }
        }
    }

    private object BinaryBsonDeserializer : JsonDeserializer<Binary>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Binary {
            val tree = jp.codec.readTree<TreeNode>(jp)
            if (tree.isObject) {
                val binary = Base64Variants.MIME_NO_LINEFEEDS.decode((tree.get("\$binary") as ValueNode).asText())
                val type = Integer.valueOf((tree.get("\$type") as ValueNode).asText().toLowerCase(), 16)!!.toByte()
                return Binary(type, binary)
            } else if (tree is POJONode) {
                return tree.pojo as Binary
            } else if (tree is BinaryNode) {
                return Binary(tree.binaryValue())
            } else {
                throw ctxt.mappingException(ObjectId::class.java)
            }
        }
    }

    init {
        addSerializer(ObjectId::class.java, ObjectIdBsonSerializer)
        addSerializer(Binary::class.java, BinaryBsonSerializer)
        addDeserializer(Binary::class.java, BinaryBsonDeserializer)
    }
}
