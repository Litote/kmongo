/*
 * Copyright (C) 2017/2018 Litote
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

package org.litote.kmongo.rxjava2

import com.mongodb.async.client.FindIterable
import org.litote.kmongo.ascending
import org.litote.kmongo.descending
import org.litote.kmongo.include
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KProperty

/**
 * Sets the query filter to apply to the query.
 *
 * @param filter the filter, which may be null
 * @return this
 */
fun <T> FindIterable<T>.filter(filter: String): FindIterable<T> = filter(KMongoUtil.toBson(filter))

/**
 * Sets the query modifiers to apply to this operation.
 *
 * @param modifiers the query modifiers to apply
 * @return this
 */
fun <T> FindIterable<T>.modifiers(modifiers: String): FindIterable<T> = modifiers(KMongoUtil.toBson(modifiers))

/**
 * Sets a document describing the fields to return for all matching documents.
 *
 * @param projection the project document
 * @return this
 */
fun <T> FindIterable<T>.projection(projection: String): FindIterable<T> = projection(KMongoUtil.toBson(projection))

/**
 * Sets a document describing the fields to return for all matching documents.
 *
 * @param projections the properties of the returned fields
 * @return this
 */
fun <T> FindIterable<T>.projection(vararg projections: KProperty<*>): FindIterable<T> =
    projection(include(*projections))

/**
 * Sets the sort criteria to apply to the query.
 *
 * @param sort the sort criteria
 * @return this
 */
fun <T> FindIterable<T>.sort(sort: String): FindIterable<T> = sort(KMongoUtil.toBson(sort))

/**
 * Sets the sort criteria with specified ascending properties to apply to the query.
 *
 * @param properties the properties
 * @return this
 */
fun <T> FindIterable<T>.ascendingSort(vararg properties: KProperty<*>): FindIterable<T> =
    sort(ascending(*properties))

/**
 * Sets the sort criteria with specified descending properties to apply to the query.
 *
 * @param properties the properties
 * @return this
 */
fun <T> FindIterable<T>.descendingSort(vararg properties: KProperty<*>): FindIterable<T> =
    sort(descending(*properties))
