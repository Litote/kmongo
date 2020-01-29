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

import com.mongodb.client.model.Indexes
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

/**
 * Create an index key for an ascending index on the given fields.
 *
 * @param properties the properties, which must contain at least one
 * @return the index specification
 * @mongodb.driver.manual core/indexes indexes
 */
fun ascendingIndex(vararg properties: KProperty<*>): Bson = Indexes.ascending(properties.map { it.path() })

/**
 * Create an index key for an ascending index on the given fields.
 *
 * @param properties the properties, which must contain at least one
 * @return the index specification
 * @mongodb.driver.manual core/indexes indexes
 */
fun ascendingIndex(properties: Iterable<KProperty<*>>): Bson = Indexes.ascending(properties.map { it.path() })

/**
 * Create an index key for a descending index on the given fields.
 *
 * @param properties the properties, which must contain at least one
 * @return the index specification
 * @mongodb.driver.manual core/indexes indexes
 */
fun descendingIndex(vararg properties: KProperty<*>): Bson = Indexes.descending(properties.map { it.path() })

/**
 * Create an index key for a descending index on the given fields.
 *
 * @param properties the properties, which must contain at least one
 * @return the index specification
 * @mongodb.driver.manual core/indexes indexes
 */
fun descendingIndex(properties: Iterable<KProperty<*>>): Bson = Indexes.descending(properties.map { it.path() })

/**
 * Create an index key for an 2dsphere index on the given fields.
 *
 * @param properties the properties, which must contain at least one
 * @return the index specification
 * @mongodb.driver.manual core/2dsphere 2dsphere Index
 */
fun geo2dsphere(vararg properties: KProperty<*>): Bson = Indexes.geo2dsphere(properties.map { it.path() })

/**
 * Create an index key for an 2dsphere index on the given fields.
 *
 * @param properties the properties, which must contain at least one
 * @return the index specification
 * @mongodb.driver.manual core/2dsphere 2dsphere Index
 */
fun geo2dsphere(properties: Iterable<KProperty<*>>): Bson = Indexes.geo2dsphere(properties.map { it.path() })

/**
 * Create an index key for a text index on the given property.
 *
 * @return the index specification
 * @mongodb.driver.manual core/text text index
 */
fun <T> KProperty<T>.textIndex(): Bson = Indexes.text(path())

/**
 * Create an index key for a hashed index on the given property.
 *
 * @return the index specification
 * @mongodb.driver.manual core/hashed hashed index
 */
fun <T> KProperty<T>.hashedIndex(): Bson = Indexes.hashed(path())

/**
 * Create a compound index specifications.  If any properties are repeated, the last one takes precedence.
 *
 * @param indexes the index specifications
 * @return the compound index specification
 * @mongodb.driver.manual core/index-compound compoundIndex
 */
fun index(vararg properties: Pair<KProperty<*>, Boolean>): Bson =
    index(properties.toMap())

/**
 * Create a compound multiple index specifications.
 * If any properties are repeated, the last one takes precedence.
 *
 * @param indexes the index specifications
 * @return the compound index specification
 * @mongodb.driver.manual core/index-compound compoundIndex
 */
fun index(properties: Map<KProperty<*>, Boolean>): Bson =
    Indexes.compoundIndex(properties.map { BsonDocument(it.key.path(), BsonInt32(if (it.value) 1 else -1)) })
