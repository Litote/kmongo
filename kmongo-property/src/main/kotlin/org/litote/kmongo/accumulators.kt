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

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.BsonField
import org.litote.kmongo.path
import kotlin.reflect.KProperty

fun <T> KProperty<T>.sum(expression: T): BsonField = Accumulators.sum(path(), expression)

fun <T> KProperty<T>.avg(expression: T): BsonField = Accumulators.avg(path(), expression)

fun <T> KProperty<T>.first(expression: T): BsonField = Accumulators.first(path(), expression)

fun <T> KProperty<T>.last(expression: T): BsonField = Accumulators.last(path(), expression)

//fun <T> KProperty<T>.max(expression: T): BsonField = Accumulators.max(path(), expression)

//fun <T> KProperty<T>.min(expression: T): BsonField = Accumulators.min(path(), expression)

//fun <T> KProperty<T>.push(expression: T): BsonField = Accumulators.push(path(), expression)

//fun <T> KProperty<T>.addToSet(expression: T): BsonField = Accumulators.addToSet(path(), expression)

fun <T> KProperty<T>.stdDevPop(expression: T): BsonField = Accumulators.stdDevPop(path(), expression)

fun <T> KProperty<T>.stdDevSamp(expression: T): BsonField = Accumulators.stdDevSamp(path(), expression)
