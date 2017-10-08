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

import com.mongodb.client.model.Filters
import com.mongodb.client.model.geojson.Geometry
import com.mongodb.client.model.geojson.Point
import org.bson.BsonType
import org.bson.conversions.Bson
import java.util.regex.Pattern
import kotlin.internal.NoInfer
import kotlin.reflect.KProperty

infix fun <T> KProperty<T>.eq(item: @NoInfer T): Bson = Filters.eq<T>(path(), item)

/**
 * See [https://docs.mongodb.com/manual/reference/operator/query/eq/#op._S_eq]
 */
infix fun <T> KProperty<Collection<T>>.contains(item: @NoInfer T): Bson = Filters.eq<T>(path(), item)

infix fun <T> KProperty<T>.ne(item: @NoInfer T): Bson = Filters.ne<T>(path(), item)

infix fun <T> KProperty<T>.lt(item: T): Bson = Filters.lt(path(), item)

infix fun <T> KProperty<T>.gt(item: T): Bson = Filters.gt(path(), item)

infix fun <T> KProperty<T>.lte(item: T): Bson = Filters.lte(path(), item)

infix fun <T> KProperty<T>.gte(item: T): Bson = Filters.gte(path(), item)

infix fun <T> KProperty<T>.`in`(values: Iterable<T>): Bson = Filters.`in`(path(), values)

infix fun <T> KProperty<T>.nin(values: Iterable<T>): Bson = Filters.nin(path(), values)

fun <T> KProperty<T>.exists(): Bson = Filters.exists(path())

infix fun <T> KProperty<T>.exists(exists: Boolean): Bson = Filters.exists(path(), exists)

infix fun <T> KProperty<T>.type(type: BsonType): Bson = Filters.type(path(), type)

fun <T> KProperty<T>.mod(divisor: Long, remainder: Long): Bson = Filters.mod(path(), divisor, remainder)

infix fun KProperty<String>.regex(regex: String): Bson = Filters.regex(path(), regex)

infix fun KProperty<String>.regex(regex: Pattern): Bson = Filters.regex(path(), regex)

fun KProperty<String>.regex(pattern: String, options: String): Bson = Filters.regex(path(), pattern, options)

infix fun <T> KProperty<T>.all(values: Iterable<T>): Bson = Filters.all(path(), values)

infix fun <T> KProperty<T>.elemMatch(filter: Bson): Bson = Filters.elemMatch(path(), filter)

infix fun <T> KProperty<T>.size(size: Int): Bson = Filters.size(path(), size)

infix fun <T> KProperty<T>.bitsAllClear(bitmask: Long): Bson = Filters.bitsAllClear(path(), bitmask)

infix fun <T> KProperty<T>.bitsAllSet(bitmask: Long): Bson = Filters.bitsAllSet(path(), bitmask)

infix fun <T> KProperty<T>.bitsAnyClear(bitmask: Long): Bson = Filters.bitsAnyClear(path(), bitmask)

infix fun <T> KProperty<T>.bitsAnySet(bitmask: Long): Bson = Filters.bitsAnySet(path(), bitmask)

infix fun <T> KProperty<T>.geoWithin(geometry: Geometry): Bson = Filters.geoWithin(path(), geometry)

fun <T> KProperty<T>.geoWithinBox(lowerLeftX: Double, lowerLeftY: Double, upperRightX: Double, upperRightY: Double): Bson = Filters.geoWithinBox(path(), lowerLeftX, lowerLeftY, upperRightX, upperRightY)

infix fun <T> KProperty<T>.geoWithinPolygon(points: List<List<Double>>): Bson = Filters.geoWithinPolygon(path(), points)

fun <T> KProperty<T>.geoWithinCenter(x: Double, y: Double, radius: Double): Bson = Filters.geoWithinCenter(path(), x, y, radius)

fun <T> KProperty<T>.geoWithinCenterSphere(x: Double, y: Double, radius: Double): Bson = Filters.geoWithinCenterSphere(path(), x, y, radius)

infix fun <T> KProperty<T>.geoIntersects(geometry: Geometry): Bson = Filters.geoIntersects(path(), geometry)

fun <T> KProperty<T>.near(geometry: Point, maxDistance: Double?, minDistance: Double?): Bson = Filters.near(path(), geometry, maxDistance, minDistance)

fun <T> KProperty<T>.nearSphere(geometry: Point, maxDistance: Double?, minDistance: Double?): Bson = Filters.nearSphere(path(), geometry, maxDistance, minDistance)


infix fun Bson.and(item: Bson): Bson = Filters.and(listOf(this, item))

infix fun Bson.or(item: Bson): Bson = Filters.or(listOf(this, item))

