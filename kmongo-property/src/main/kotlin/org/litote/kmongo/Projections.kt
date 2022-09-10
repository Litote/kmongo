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

package org.litote.kmongo

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import org.bson.conversions.Bson
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * The projection of the property.
 */
val <T> KProperty<T>.projection: String get() = path().projection

/**
 * The projection of the property.
 */
val String.projection: String get() = "\$$this"

/**
 * In order to write `$p.p2`
 */
infix fun <T0, T1> KProperty1<T0, T1?>.projectionWith(p2: String): String = "$projection.$p2"

/**
 * Creates a projection of a property whose value is computed from the given expression.
 *
 * @param expression    the expression
 * @param <T> the expression type
 * @return the projection
 * @see Aggregates#project(Bson)
 */
infix fun <T> KProperty<T>.from(expression: T): Bson =
    Projections.computed(path(), expression)

/**
 * Builds Bson for the [MongoOperator] and the specified expression.
 */
infix fun MongoOperator.from(expression: Any): Bson = toString().from(expression)

/**
 * Builds Bson from this String format and the specified expression.
 */
infix fun String.from(expression: Any): Bson =
    @Suppress("UNCHECKED_CAST")
    Projections.computed(this, (expression as? KProperty<Any>)?.projection ?: expression)

/**
 * Creates a projection that includes all given properties.
 *
 * @param properties the field names
 * @return the projection
 */
fun include(vararg properties: KProperty<*>): Bson = include(properties.toList())

/**
 * Creates a projection that includes all of the given properties.
 *
 * @param properties the field names
 * @return the projection
 */
fun include(properties: Iterable<KProperty<*>>): Bson = Projections.include(properties.map { it.path() })

/**
 * Creates a projection that excludes all of the given properties.
 *
 * @param properties the field names
 * @return the projection
 */
fun exclude(vararg properties: KProperty<*>): Bson = exclude(properties.toList())

/**
 * Creates a projection that excludes all of the given properties.
 *
 * @param properties the field names
 * @return the projection
 */
fun exclude(properties: Iterable<KProperty<*>>): Bson = Projections.exclude(properties.map { it.path() })

/**
 * Creates a projection that excludes the _id field.  This suppresses the automatic inclusion of _id that is the default, even when
 * other fields are explicitly included.
 *
 * @return the projection
 */
fun excludeId(): Bson = Projections.excludeId()

/**
 * Creates a projection that includes for the given property only the first element of an array that matches the query filter.  This is
 * referred to as the positional $ operator.
 *
 * @return the projection
 * @mongodb.driver.manual reference/operator/projection/positional/#projection Project the first matching element ($ operator)
 */
fun <T> KProperty<T>.elemMatchProj(): Bson = Projections.elemMatch(path())

/**
 * Creates a projection that includes for the given property only the first element of the array value of that field that matches the given
 * query filter.
 *
 * @param filter    the filter to apply
 * @return the projection
 * @mongodb.driver.manual reference/operator/projection/elemMatch elemMatch
 */
fun <T> KProperty<T>.elemMatchProj(filter: Bson): Bson = Projections.elemMatch(path(), filter)

/**
 * Creates a projection to the given property of the textScore, for use with text queries.
 *
 * @return the projection
 * @mongodb.driver.manual reference/operator/projection/meta/#projection textScore
 */
fun <T> KProperty<T>.metaTextScore(): Bson = Projections.metaTextScore(path())

/**
 * Creates a projection to the given property of a slice of the array value of that field.
 *
 * @param limit     the number of elements to project.
 * @return the projection
 * @mongodb.driver.manual reference/operator/projection/slice Slice
 */
fun <T> KProperty<T>.slice(limit: Int): Bson = Projections.slice(path(), limit)

/**
 * Creates a projection to the given property of a slice of the array value of that field.
 *
 * @param skip      the number of elements to skip before applying the limit
 * @param limit     the number of elements to project
 * @return the projection
 * @mongodb.driver.manual reference/operator/projection/slice Slice
 */
fun <T> KProperty<T>.slice(skip: Int, limit: Int): Bson = Projections.slice(path(), skip, limit)


/**
 * Creates a projection that combines the list of projections into a single one.  If there are duplicate keys, the last one takes
 * precedence.
 *
 * @param projections the list of projections to combine
 * @return the combined projection
 */
fun fields(vararg projections: Bson): Bson = Projections.fields(*projections)

/**
 * Creates a projection that combines the list of projections into a single one.  If there are duplicate keys, the last one takes
 * precedence.
 *
 * @param projections the list of projections to combine
 * @return the combined projection
 * @mongodb.driver.manual
 */
fun fields(projections: List<Bson>): Bson = Projections.fields(projections)
