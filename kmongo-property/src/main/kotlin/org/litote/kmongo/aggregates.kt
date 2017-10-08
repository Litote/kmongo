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

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

fun <T> KProperty<T>.count(): Bson = Aggregates.count(path())

/**
 * Creates a $match with $and on each filter.
 */
fun match(vararg filters: Bson): Bson {
    return Aggregates.match(Filters.and(*filters))
}

fun project(vararg properties: KProperty<*>): Bson {
    return Aggregates.project(Filters.and(properties.map { it eq true }))
}

fun project(vararg properties: Pair<KProperty<*>, Boolean>): Bson {
    return Aggregates.project(Filters.and(properties.map { it.first eq it.second }))
}

fun project(properties: Map<KProperty<*>, String>): Bson {
    return Aggregates.project(Filters.and(properties.map { it.key eq it.value }))
}
