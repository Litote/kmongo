/*
 * Copyright (C) 2016/2022 Litote
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

package org.litote.kmongo.reactivestreams

import com.mongodb.reactivestreams.client.MapReducePublisher
import org.litote.kmongo.ascending
import org.litote.kmongo.descending
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KProperty

/**
 * Sets the global variables that are accessible in the map, reduce and finalize functions.
 *
 * @param scope the global variables that are accessible in the map, reduce and finalize functions.
 * @return this
 */
fun <T> MapReducePublisher<T>.scope(scope: String): MapReducePublisher<T> = scope(KMongoUtil.toBson(scope))

/**
 * Sets the sort criteria to apply to the query.
 *
 * @param sort the sort criteria, which may be null
 * @return this
 */
fun <T> MapReducePublisher<T>.sort(sort: String): MapReducePublisher<T> = sort(KMongoUtil.toBson(sort))

/**
 * Sets the sort criteria with specified ascending properties to apply to the query.
 *
 * @param properties the properties
 * @return this
 */
fun <T> MapReducePublisher<T>.ascendingSort(vararg properties: KProperty<*>): MapReducePublisher<T> =
    sort(ascending(*properties))

/**
 * Sets the sort criteria with specified descending properties to apply to the query.
 *
 * @param properties the properties
 * @return this
 */
fun <T> MapReducePublisher<T>.descendingSort(vararg properties: KProperty<*>): MapReducePublisher<T> =
    sort(descending(*properties))

/**
 * Sets the query filter to apply to the query.
 *
 * @param filter the filter to apply to the query
 * @return this
 */
fun <T> MapReducePublisher<T>.filter(filter: String): MapReducePublisher<T> = filter(KMongoUtil.toBson(filter))
