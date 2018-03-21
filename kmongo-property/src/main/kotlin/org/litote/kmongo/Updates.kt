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

import com.mongodb.client.model.Collation
import com.mongodb.client.model.DeleteManyModel
import com.mongodb.client.model.DeleteOneModel
import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.InsertOneModel
import com.mongodb.client.model.PushOptions
import com.mongodb.client.model.ReplaceOneModel
import com.mongodb.client.model.UpdateManyModel
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.path
import kotlin.reflect.KProperty

fun <T> set(property: KProperty<T>, value: T): Bson = Updates.set(property.path(), value)

fun <T> unset(property: KProperty<T>): Bson = Updates.unset(property.path())

fun <T> setOnInsert(property: KProperty<T>, value: T): Bson = Updates.setOnInsert(property.path(), value)

fun <T> rename(property: KProperty<T>, newFieldName: String): Bson = Updates.rename(property.path(), newFieldName)

fun <T> inc(property: KProperty<T>, number: Number): Bson = Updates.inc(property.path(), number)

fun <T> mul(property: KProperty<T>, number: Number): Bson = Updates.mul(property.path(), number)

fun <T> min(property: KProperty<T>, value: T): Bson = Updates.min(property.path(), value)

fun <T> max(property: KProperty<T>, value: T): Bson = Updates.max(property.path(), value)

fun <T> currentDate(property: KProperty<T>): Bson = Updates.currentDate(property.path())

fun <T> currentTimestamp(property: KProperty<T>): Bson = Updates.currentTimestamp(property.path())

fun <T> addToSet(property: KProperty<T>, value: T): Bson = Updates.addToSet(property.path(), value)

fun <T> addEachToSet(property: KProperty<T>, values: List<T>): Bson = Updates.addEachToSet(property.path(), values)

fun <T> push(property: KProperty<T>, value: T): Bson = Updates.push(property.path(), value)

fun <T> pushEach(property: KProperty<T>, values: List<T>): Bson = Updates.pushEach(property.path(), values)

fun <T> pushEach(property: KProperty<T>, values: List<T>, options: PushOptions): Bson =
    Updates.pushEach(property.path(), values, options)

fun <T> pull(property: KProperty<T>, value: T): Bson = Updates.pull(property.path(), value)

fun <T> pullAll(property: KProperty<T>, values: List<T>): Bson = Updates.pullAll(property.path(), values)

fun <T> popFirst(property: KProperty<T>): Bson = Updates.popFirst(property.path())

fun <T> popLast(property: KProperty<T>): Bson = Updates.popLast(property.path())

fun <T> bitwiseAnd(property: KProperty<T>, value: Int): Bson = Updates.bitwiseAnd(property.path(), value)

fun <T> bitwiseAnd(property: KProperty<T>, value: Long): Bson = Updates.bitwiseAnd(property.path(), value)

fun <T> bitwiseOr(property: KProperty<T>, value: Int): Bson = Updates.bitwiseOr(property.path(), value)

fun <T> bitwiseOr(property: KProperty<T>, value: Long): Bson = Updates.bitwiseOr(property.path(), value)

fun <T> bitwiseXor(property: KProperty<T>, value: Int): Bson = Updates.bitwiseXor(property.path(), value)

fun <T> bitwiseXor(property: KProperty<T>, value: Long): Bson = Updates.bitwiseXor(property.path(), value)

fun <T> insertOne(document: T): InsertOneModel<T> = InsertOneModel(document)

fun <T> updateOne(filter: Bson, update: Bson, options: UpdateOptions = UpdateOptions()): UpdateOneModel<T> =
    UpdateOneModel(filter, update, options)

fun <T> updateMany(filter: Bson, update: Bson, options: UpdateOptions = UpdateOptions()): UpdateManyModel<T> =
    UpdateManyModel(filter, update, options)

fun <T> replaceOne(filter: Bson, replacement: T, options: UpdateOptions = UpdateOptions()): ReplaceOneModel<T> =
    ReplaceOneModel(filter, replacement, options)

fun <T> deleteOne(filter: Bson, options: DeleteOptions = DeleteOptions()): DeleteOneModel<T> =
    DeleteOneModel(filter, options)

fun <T> deleteMany(filter: Bson, options: DeleteOptions = DeleteOptions()): DeleteManyModel<T> =
    DeleteManyModel(filter, options)

fun updateUpsert(): UpdateOptions = UpdateOptions().upsert(true)
fun updateBypassDocumentValidation(): UpdateOptions = UpdateOptions().bypassDocumentValidation(true)
fun updateCollation(collation: Collation): UpdateOptions = UpdateOptions().collation(collation)
fun updateArrayFilters(filters: List<Bson>): UpdateOptions = UpdateOptions().arrayFilters(filters)

fun findOneAndUpdateUpsert(): FindOneAndUpdateOptions = FindOneAndUpdateOptions().upsert(true)

