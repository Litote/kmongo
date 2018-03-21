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

import com.mongodb.client.model.Projections
import org.bson.conversions.Bson
import org.litote.kmongo.path
import kotlin.reflect.KProperty

val <T> KProperty<T>.proj: String get() = "\$${path()}"

fun <T> KProperty<T>.computed(expression: T): Bson = Projections.computed(path(), expression)

fun <T> KProperty<T>.elemMatch(): Bson = Projections.elemMatch(path())

fun <T> KProperty<T>.elemMatch(filter: Bson): Bson = Projections.elemMatch(path(), filter)

fun <T> KProperty<T>.metaTextScore(): Bson = Projections.metaTextScore(path())

fun <T> KProperty<T>.slice(limit: Int): Bson = Projections.slice(path(), limit)

fun <T> KProperty<T>.slice(skip: Int, limit: Int): Bson = Projections.slice(path(), skip, limit)
