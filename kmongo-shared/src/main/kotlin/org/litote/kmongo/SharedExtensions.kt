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
import org.litote.kmongo.util.KMongoUtil

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