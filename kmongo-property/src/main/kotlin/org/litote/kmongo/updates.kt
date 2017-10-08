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

import com.mongodb.client.model.PushOptions
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.path
import kotlin.reflect.KProperty

fun <T> KProperty<T>.set(value: T): Bson = Updates.set(path(), value)

fun <T> KProperty<T>.unset(): Bson = Updates.unset(path())

fun <T> KProperty<T>.setOnInsert(value: T): Bson = Updates.setOnInsert(path(), value)

fun <T> KProperty<T>.rename(newFieldName: String): Bson = Updates.rename(path(), newFieldName)

fun <T> KProperty<T>.inc(number: Number): Bson = Updates.inc(path(), number)

fun <T> KProperty<T>.mul(number: Number): Bson = Updates.mul(path(), number)

fun <T> KProperty<T>.min(value: T): Bson = Updates.min(path(), value)

fun <T> KProperty<T>.max(value: T): Bson = Updates.max(path(), value)

fun <T> KProperty<T>.currentDate(): Bson = Updates.currentDate(path())

fun <T> KProperty<T>.currentTimestamp(): Bson = Updates.currentTimestamp(path())

fun <T> KProperty<T>.addToSet(value: T): Bson = Updates.addToSet(path(), value)

fun <T> KProperty<T>.addEachToSet(values: List<T>): Bson = Updates.addEachToSet(path(), values)

fun <T> KProperty<T>.push(value: T): Bson = Updates.push(path(), value)

fun <T> KProperty<T>.pushEach(values: List<T>): Bson = Updates.pushEach(path(), values)

fun <T> KProperty<T>.pushEach(values: List<T>, options: PushOptions): Bson = Updates.pushEach(path(), values, options)

fun <T> KProperty<T>.pull(value: T): Bson = Updates.pull(path(), value)

fun <T> KProperty<T>.pullAll(values: List<T>): Bson = Updates.pullAll(path(), values)

fun <T> KProperty<T>.popFirst(): Bson = Updates.popFirst(path())

fun <T> KProperty<T>.popLast(): Bson = Updates.popLast(path())

fun <T> KProperty<T>.bitwiseAnd(value: Int): Bson = Updates.bitwiseAnd(path(), value)

fun <T> KProperty<T>.bitwiseAnd(value: Long): Bson = Updates.bitwiseAnd(path(), value)

fun <T> KProperty<T>.bitwiseOr(value: Int): Bson = Updates.bitwiseOr(path(), value)

fun <T> KProperty<T>.bitwiseOr(value: Long): Bson = Updates.bitwiseOr(path(), value)

fun <T> KProperty<T>.bitwiseXor(value: Int): Bson = Updates.bitwiseXor(path(), value)

fun <T> KProperty<T>.bitwiseXor(value: Long): Bson = Updates.bitwiseXor(path(), value)
