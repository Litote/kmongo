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
import kotlin.reflect.KProperty1

/**
 *
 */
class KPropertyPath<T>(previous: KPropertyPath<*>?, property: KProperty<T?>) {

    constructor(previous: KProperty<Any?>, property: KProperty<T?>) : this(KPropertyPath(null as (KPropertyPath<*>?), previous), property)

    val path: String = "${previous?.path ?: ""}${if (previous == null) "" else "."}${property.path()}"

    operator fun <T2> div(p2: KProperty1<T, T2?>): KPropertyPath<T2?> = KPropertyPath(this, p2)

    operator fun <T2> div(p2: KProperty<T2?>): KPropertyPath<T2?> = KPropertyPath(this, p2)

    //filters
    infix fun eq(item: @NoInfer T): Bson = Filters.eq<T>(path, item)

    infix fun <V, T : Collection<V>> KProperty<T>.contains(item: @NoInfer V): Bson = Filters.eq<V>(path, item)

    infix fun ne(item: @NoInfer T): Bson = Filters.ne<T>(path, item)

    infix fun lt(item: T): Bson = Filters.lt(path, item)

    infix fun gt(item: T): Bson = Filters.gt(path, item)

    infix fun lte(item: T): Bson = Filters.lte(path, item)

    infix fun gte(item: T): Bson = Filters.gte(path, item)

    infix fun `in`(values: Iterable<T>): Bson = Filters.`in`(path, values)

    infix fun nin(values: Iterable<T>): Bson = Filters.nin(path, values)

    fun exists(): Bson = Filters.exists(path)

    infix fun exists(exists: Boolean): Bson = Filters.exists(path, exists)

    infix fun type(type: BsonType): Bson = Filters.type(path, type)

    fun mod(divisor: Long, remainder: Long): Bson = Filters.mod(path, divisor, remainder)

    infix fun regex(regex: String): Bson = Filters.regex(path, regex)

    infix fun regex(regex: Pattern): Bson = Filters.regex(path, regex)

    fun regex(pattern: String, options: String): Bson = Filters.regex(path, pattern, options)

    infix fun all(values: Iterable<T>): Bson = Filters.all(path, values)

    infix fun elemMatch(filter: Bson): Bson = Filters.elemMatch(path, filter)

    infix fun size(size: Int): Bson = Filters.size(path, size)

    infix fun bitsAllClear(bitmask: Long): Bson = Filters.bitsAllClear(path, bitmask)

    infix fun bitsAllSet(bitmask: Long): Bson = Filters.bitsAllSet(path, bitmask)

    infix fun bitsAnyClear(bitmask: Long): Bson = Filters.bitsAnyClear(path, bitmask)

    infix fun bitsAnySet(bitmask: Long): Bson = Filters.bitsAnySet(path, bitmask)

    infix fun geoWithin(geometry: Geometry): Bson = Filters.geoWithin(path, geometry)

    fun geoWithinBox(lowerLeftX: Double, lowerLeftY: Double, upperRightX: Double, upperRightY: Double): Bson = Filters.geoWithinBox(path, lowerLeftX, lowerLeftY, upperRightX, upperRightY)

    infix fun geoWithinPolygon(points: List<List<Double>>): Bson = Filters.geoWithinPolygon(path, points)

    fun geoWithinCenter(x: Double, y: Double, radius: Double): Bson = Filters.geoWithinCenter(path, x, y, radius)

    fun geoWithinCenterSphere(x: Double, y: Double, radius: Double): Bson = Filters.geoWithinCenterSphere(path, x, y, radius)

    infix fun geoIntersects(geometry: Geometry): Bson = Filters.geoIntersects(path, geometry)

    fun near(geometry: Point, maxDistance: Double?, minDistance: Double?): Bson = Filters.near(path, geometry, maxDistance, minDistance)

    fun nearSphere(geometry: Point, maxDistance: Double?, minDistance: Double?): Bson = Filters.nearSphere(path, geometry, maxDistance, minDistance)

}