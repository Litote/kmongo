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

package org.litote.kmongo.util

import com.mongodb.client.model.DeleteManyModel
import com.mongodb.client.model.DeleteOneModel
import com.mongodb.client.model.InsertOneModel
import com.mongodb.client.model.ReplaceOneModel
import com.mongodb.client.model.UpdateManyModel
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.WriteModel
import org.bson.BsonBoolean
import org.bson.BsonDocument
import org.bson.BsonObjectId
import org.bson.BsonString
import org.bson.RawBsonDocument
import org.bson.codecs.BsonArrayCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.bson.json.JsonReader
import org.bson.types.ObjectId
import org.litote.kmongo.MongoOperator.currentDate
import org.litote.kmongo.MongoOperator.inc
import org.litote.kmongo.MongoOperator.max
import org.litote.kmongo.MongoOperator.min
import org.litote.kmongo.MongoOperator.mul
import org.litote.kmongo.MongoOperator.rename
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.MongoOperator.setOnInsert
import org.litote.kmongo.MongoOperator.unset
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.KClass

/**
 * Internal utility methods
 */
object KMongoUtil {

    val EMPTY_JSON = "{}"
    private val SPACE_REPLACE_PATTERN = Pattern.compile("\\\$\\s+")
    private val QUOTE_REPLACE_MATCHER = Matcher.quoteReplacement("\$")
    private val UPDATE_OPERATORS = listOf(inc, mul, rename, setOnInsert, set, unset, min, max, currentDate).map { it.toString() }

    fun toBson(json: String): BsonDocument
            = if (json == EMPTY_JSON) BsonDocument() else BsonDocument.parse(json)

    fun <T : Any> toBson(json: String, type: KClass<T>): BsonDocument
            = generateIfAbsentAndMayBeMoveId(toBson(json), type)

    private fun <T : Any> generateIfAbsentAndMayBeMoveId(document: BsonDocument, type: KClass<T>): BsonDocument {
        if (!document.containsKey("_id")) {
            val idProperty = MongoIdUtil.findIdProperty(type)
            if (idProperty != null) {
                val idValue = document.get(idProperty.name)
                if (idValue == null) {
                    val toString = idProperty.returnType.toString()
                    if (toString.startsWith(ObjectId::class.qualifiedName!!)) {
                        document.put("_id", BsonObjectId(ObjectId.get()))
                    } else if (toString.startsWith(String::class.qualifiedName!!)) {
                        document.put("_id", BsonString(ObjectId.get().toString()))
                    } else {
                        throw IllegalArgumentException("generation for id property type not supported : $idProperty")
                    }
                } else {
                    document.put("_id", idValue)
                }
                if (idProperty.name != "_id") {
                    document.remove(idProperty.name)
                }
            }
        }

        return document
    }

    fun toBsonList(json: Array<out String>, codecRegistry: CodecRegistry): List<Bson>
            =
            if (json.size == 1 && isJsonArray(json[0])) {
                BsonArrayCodec(codecRegistry).decode(JsonReader(json[0]), DecoderContext.builder().build())
                        .map { it as BsonDocument }
            } else {
                json.map { toBson(it) }
            }

    fun filterIdToBson(obj: Any): BsonDocument
            = RawBsonDocument(KMongoConfiguration.filterIdBsonMapper.writeValueAsBytes(obj))

    fun formatJson(json: String): String {
        return SPACE_REPLACE_PATTERN.matcher(json).replaceAll(QUOTE_REPLACE_MATCHER)
    }

    fun toExtendedJson(obj: Any): String
            = KMongoConfiguration.extendedJsonMapper.writeValueAsString(obj)

    private fun filterIdToExtendedJson(obj: Any): String
            = KMongoConfiguration.filterIdExtendedJsonMapper.writeValueAsString(obj)

    private fun isJsonArray(json: String)
            = json.trim().startsWith('[')

    fun idFilterQuery(id: Any): String
            = "{_id:${toExtendedJson(id)}}"

    private fun containsUpdateOperator(map: Map<*, *>): Boolean
            = UPDATE_OPERATORS.any { map.contains(it) }

    fun setModifier(obj: Any): String {
        return if (obj is Map<*, *> && containsUpdateOperator(obj)) {
            toExtendedJson(obj)
        } else {
            "{\$set:${filterIdToExtendedJson(obj)}}"
        }
    }

    fun extractId(obj: Any, clazz: KClass<*>): Any {
        //check map
        if (obj is Map<*, *>) {
            return obj["_id"] ?: throw IllegalArgumentException("_id is null")
        }
        val idProperty = MongoIdUtil.findIdProperty(clazz)
        if (idProperty == null) {
            throw IllegalArgumentException("$obj has to contain _id field")
        } else {
            return MongoIdUtil.getIdValue(idProperty, obj) ?: throw IllegalArgumentException("id is null")
        }
    }

    fun <T : Any> toWriteModel(json: Array<out String>, codecRegistry: CodecRegistry, type: KClass<T>): List<WriteModel<BsonDocument>>
            =
            if (json.size == 1 && isJsonArray(json[0])) {
                BsonArrayCodec(codecRegistry).decode(JsonReader(json[0]), DecoderContext.builder().build())
                        .map { toWriteModel(it as BsonDocument, type) }
            } else {
                json.map { toWriteModel(toBson(it), type) }
            }

    private fun <T : Any> toWriteModel(bson: BsonDocument, type: KClass<T>): WriteModel<BsonDocument> {
        if (bson.containsKey("insertOne")) {
            return InsertOneModel(generateIfAbsentAndMayBeMoveId(bson.getDocument("insertOne"), type))
        } else if (bson.containsKey("updateOne")) {
            val updateOne = bson.getDocument("updateOne")
            return UpdateOneModel<BsonDocument>(
                    updateOne.getDocument("filter"),
                    updateOne.getDocument("update"),
                    UpdateOptions().upsert(updateOne.getBoolean("upsert", BsonBoolean.FALSE).value))
        } else if (bson.containsKey("updateMany")) {
            val updateMany = bson.getDocument("updateMany")
            return UpdateManyModel<BsonDocument>(
                    updateMany.getDocument("filter"),
                    updateMany.getDocument("update"),
                    UpdateOptions().upsert(updateMany.getBoolean("upsert", BsonBoolean.FALSE).value))
        } else if (bson.containsKey("replaceOne")) {
            val replaceOne = bson.getDocument("replaceOne")
            return ReplaceOneModel<BsonDocument>(
                    replaceOne.getDocument("filter"),
                    replaceOne.getDocument("replacement"),
                    UpdateOptions().upsert(replaceOne.getBoolean("upsert", BsonBoolean.FALSE).value))
        } else if (bson.containsKey("deleteOne")) {
            val deleteOne = bson.getDocument("deleteOne")
            return DeleteOneModel<BsonDocument>(
                    deleteOne.getDocument("filter"))
        } else if (bson.containsKey("deleteMany")) {
            val deleteMany = bson.getDocument("deleteMany")
            return DeleteManyModel<BsonDocument>(
                    deleteMany.getDocument("filter"))
        } else {
            throw IllegalArgumentException("unknown write model : $bson")
        }
    }

    fun defaultCollectionName(clazz: KClass<*>): String
            = KMongoConfiguration.defaultCollectionNameBuilder.invoke(clazz)

    fun getIdValue(value: Any): Any? {
        //check map
        if (value is Map<*, *>) {
            return value["_id"]
        }
        val idProperty = MongoIdUtil.findIdProperty(value.javaClass.kotlin)
        return if (idProperty == null) null else MongoIdUtil.getIdValue(idProperty, value)
    }

}