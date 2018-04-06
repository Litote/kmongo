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

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.BsonField
import kotlin.reflect.KProperty

/**
 * Gets a field name for a $group operation representing the sum of the values of the given expression when applied to all members of
 * the group.
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/sum/ $sum
 */
infix fun <T> KProperty<T>.sum(expression: T): BsonField =
    Accumulators.sum(path(), expression)

/**
 * Gets a field name for a $group operation representing the average of the values of the given expression when applied to all
 * members of the group.
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/avg/ $avg
 */
infix fun <T> KProperty<T>.avg(expression: T): BsonField =
    Accumulators.avg(path(), expression)

/**
 * Gets a field name for a $group operation representing the value of the given expression when applied to the first member of
 * the group.
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/first/ $first
 */
infix fun <T> KProperty<T>.first(expression: T): BsonField =
    Accumulators.first(path(), expression)

/**
 * Gets a field name for a $group operation representing the value of the given expression when applied to the last member of
 * the group.
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/last/ $last
 */
infix fun <T> KProperty<T>.last(expression: T): BsonField =
    Accumulators.last(path(), expression)

/**
 * Gets a field name for a $group operation representing the maximum of the values of the given expression when applied to all
 * members of the group.
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/max/ $max
 */
infix fun <T> KProperty<T>.max(expression: T): BsonField =
    Accumulators.max(path(), expression)

/**
 * Gets a field name for a $group operation representing the minimum of the values of the given expression when applied to all
 * members of the group.
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/min/ $min
 */
infix fun <T> KProperty<T>.min(expression: T): BsonField =
    Accumulators.min(path(), expression)

/**
 * Gets a field name for a $group operation representing an array of all values that results from applying an expression to each
 * document in a group of documents that share the same group by key.
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/push/ $push
 */
infix fun <T> KProperty<T>.push(expression: T): BsonField =
    Accumulators.push(path(), expression)

/**
 * Gets a field name for a $group operation representing all unique values that results from applying the given expression to each
 * document in a group of documents that share the same group by key.
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/addToSet/ $addToSet
 */
infix fun <T> KProperty<T>.addToSet(expression: T): BsonField =
    Accumulators.addToSet(path(), expression)

/**
 * Gets a field name for a $group operation representing the sample standard deviation of the values of the given expression
 * when applied to all members of the group.
 *
 * <p>Use if the values encompass the entire population of data you want to represent and do not wish to generalize about
 * a larger population.</p>
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/stdDevPop/ $stdDevPop
 */
infix fun <T> KProperty<T>.stdDevPop(expression: T): BsonField =
    Accumulators.stdDevPop(path(), expression)

/**
 * Gets a field name for a $group operation representing the sample standard deviation of the values of the given expression
 * when applied to all members of the group.
 *
 * <p>Use if the values encompass a sample of a population of data from which to generalize about the population.</p>
 *
 * @param expression the expression
 * @param <TExpression> the expression type
 * @return the field
 * @mongodb.driver.manual reference/operator/aggregation/stdDevSamp/ $stdDevSamp
 */
infix fun <T> KProperty<T>.stdDevSamp(expression: T): BsonField =
    Accumulators.stdDevSamp(path(), expression)
