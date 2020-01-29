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

package org.litote.kmongo

import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import org.bson.BsonDocument
import org.bson.Document
import org.litote.kmongo.util.KMongoUtil

/**
 * Construct an instance with the given keys and options.
 *
 * @param keys the index keys
 * @param options the index options
 */
fun IndexModel.IndexModel(keys: String, options: IndexOptions = IndexOptions()): IndexModel =
    IndexModel(KMongoUtil.toBson(keys), options)

/**
 * Get the extended json representation of this object
 *
 * See [Mongo extended json format](https://docs.mongodb.com/manual/reference/mongodb-extended-json) for details
 */
val Any.json: String
    get() = KMongoUtil.toExtendedJson(this)

/**
 * Get the [org.bson.BsonValue] of this string.
 *
 * @throws Exception if the string content is not a valid json document format
 */
val String.bson: BsonDocument
    get() = KMongoUtil.toBson(this)

/**
 * Format this string to remove space(s) between $ and next char
 */
fun String.formatJson(): String = KMongoUtil.formatJson(this)

/**
 * Find the value for the specified path from the document.
 */
fun <T> Document.findValue(path: String): T? {
    val paths = path.split(".")
    var d = this
    if (paths.size > 1) {
        for (s in paths.take(paths.size - 1)) {
            d = d[s] as Document
        }
    }
    @Suppress("UNCHECKED_CAST")
    return d[paths.last()] as T?
}