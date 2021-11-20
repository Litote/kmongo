/*
 * Copyright (C) 2016/2021 Litote
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

import com.mongodb.client.model.DeleteManyModel
import com.mongodb.client.model.DeleteOneModel
import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.InsertOneModel
import com.mongodb.client.model.PushOptions
import com.mongodb.client.model.ReplaceOneModel
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateManyModel
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.ObjectMappingConfiguration
import kotlin.internal.OnlyInputTypes
import kotlin.reflect.KProperty

/**
 * Generates a [SetTo] used in updateOne or updateMany operations.
 *
 * @param the value to set.
 * @return the SetTo instance.
 */
infix fun <@OnlyInputTypes T> KProperty<T>.setTo(value: T): SetTo<T> = SetTo(this, value)

/**
 * Combine a list of updates into a single update.
 *
 * @param updates the list of updates
 * @return a combined update
 */
fun combine(vararg updates: Bson): Bson = Updates.combine(*updates)

/**
 * Combine a list of updates into a single update.
 *
 * @param updates the list of updates
 * @return a combined update
 */
fun combine(updates: List<Bson>): Bson = Updates.combine(updates)

/**
 * Creates an update that sets the value of the property to the given value.
 *
 * @param property the property
 * @param value     the value
 * @param <T>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/set/ $set
 */
fun <@OnlyInputTypes T> setValue(property: KProperty<T?>, value: T?): Bson = Updates.set(property.path(), value)


/**
 * Creates an update that sets the values of the properties to the specified values.
 *
 * @param properties the properties to update
 * @return the update
 * @mongodb.driver.manual reference/operator/update/set/ $set
 */
fun set(vararg properties: SetTo<*>): Bson =
    combine(properties.map { setValue(it.property, it.value) })

/**
 * Creates an update that deletes the property with the given name.
 *
 * @param property the property
 * @return the update
 * @mongodb.driver.manual reference/operator/update/unset/ $unset
 */
fun <T> unset(property: KProperty<T>): Bson = Updates.unset(property.path())

/**
 * Creates an update that sets the value of the property to the given value, but only if the update is an upsert that
 * results in an insert of a document.
 *
 * @param property the property
 * @param value     the value
 * @param <TItem>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/setOnInsert/ $setOnInsert
 * @see UpdateOptions#upsert(boolean)
 */
fun <@OnlyInputTypes T> setOnInsert(property: KProperty<T?>, value: T): Bson =
    Updates.setOnInsert(property.path(), value)

/**
 * Creates an update that sets the collection to the given value, but only if the update is an upsert that
 * results in an insert of a document.
 *
 * @param value the value to insert
 * @return the update
 * @mongodb.driver.manual reference/operator/update/setOnInsert/ $setOnInsert
 * @see UpdateOptions#upsert(boolean)
 */
fun setValueOnInsert(
    value: Any
): Bson =
    Updates.setOnInsert(KMongoUtil.filterIdToBson(value, !ObjectMappingConfiguration.serializeNull))

/**
 * Creates an update that renames a field.
 *
 * @param property    the property
 * @param newProperty the new property
 * @return the update
 * @mongodb.driver.manual reference/operator/update/rename/ $rename
 */
fun <@OnlyInputTypes T> rename(property: KProperty<T?>, newProperty: KProperty<T>): Bson =
    Updates.rename(property.path(), newProperty.path())

/**
 * Creates an update that increments the value of the property by the given value.
 *
 * @param property the property
 * @param number    the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/inc/ $inc
 */
fun <T : Number?> inc(property: KProperty<T>, number: Number): Bson = Updates.inc(property.path(), number)

/**
 * Creates an update that multiplies the value of the property by the given number.
 *
 * @param property the property
 * @param number    the non-null number
 * @return the update
 * @mongodb.driver.manual reference/operator/update/mul/ $mul
 */
fun <T : Number?> mul(property: KProperty<T>, number: Number): Bson = Updates.mul(property.path(), number)

/**
 * Creates an update that sets the value of the property if the given value is less than the current value of the
 * property.
 *
 * @param property the property
 * @param value     the value
 * @param <TItem>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/min/ $min
 */
fun <@OnlyInputTypes T> min(property: KProperty<T>, value: T): Bson = Updates.min(property.path(), value)

/**
 * Creates an update that sets the value of the property if the given value is greater than the current value of the
 * property.
 *
 * @param property the property
 * @param value     the value
 * @param <TItem>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/min/ $min
 */
fun <@OnlyInputTypes T> max(property: KProperty<T>, value: T): Bson = Updates.max(property.path(), value)

/**
 * Creates an update that sets the value of the property to the current date as a BSON date.
 *
 * @param property the property
 * @return the update
 * @mongodb.driver.manual reference/operator/update/currentDate/ $currentDate
 * @mongodb.driver.manual reference/bson-types/#date Date
 */
fun <T> currentDate(property: KProperty<T>): Bson = Updates.currentDate(property.path())

/**
 * Creates an update that sets the value of the property to the current date as a BSON timestamp.
 *
 * @param property the property
 * @return the update
 * @mongodb.driver.manual reference/operator/update/currentDate/ $currentDate
 * @mongodb.driver.manual reference/bson-types/#document-bson-type-timestamp Timestamp
 */
fun <T> currentTimestamp(property: KProperty<T>): Bson = Updates.currentTimestamp(property.path())

/**
 * Creates an update that adds the given value to the array value of the property, unless the value is
 * already present, in which case it does nothing
 *
 * @param property the property
 * @param value     the value
 * @param <TItem>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/addToSet/ $addToSet
 */
fun <@OnlyInputTypes T> addToSet(property: KProperty<Iterable<T>?>, value: T): Bson =
    Updates.addToSet(property.path(), value)

/**
 * Creates an update that adds each of the given values to the array value of the property, unless the value is
 * already present, in which case it does nothing
 *
 * @param property the property
 * @param values    the values
 * @param <TItem>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/addToSet/ $addToSet
 */
fun <@OnlyInputTypes T> addEachToSet(property: KProperty<Iterable<T>?>, values: List<T>): Bson =
    Updates.addEachToSet(property.path(), values)

/**
 * Creates an update that adds the given value to the array value of the property.
 *
 * @param property the property
 * @param value     the value
 * @param <TItem>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/push/ $push
 */
fun <@OnlyInputTypes T> push(property: KProperty<Iterable<T>?>, value: T): Bson = Updates.push(property.path(), value)

/**
 * Creates an update that adds each of the given values to the array value of the property, applying the given
 * options for positioning the pushed values, and then slicing and/or sorting the array.
 *
 * @param property the property
 * @param values    the values
 * @param options   the non-null push options
 * @param <TItem>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/push/ $push
 */
fun <@OnlyInputTypes T> pushEach(
    property: KProperty<Iterable<T>?>,
    values: List<T?>,
    options: PushOptions = PushOptions()
): Bson =
    Updates.pushEach(property.path(), values, options)

/**
 * Creates an update that removes all instances of the given value from the array value of the property.
 *
 * @param property the property
 * @param value     the value
 * @param <T>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/pull/ $pull
 */
fun <@OnlyInputTypes T> pull(property: KProperty<Iterable<T?>?>, value: T?): Bson = Updates.pull(property.path(), value)

/**
 * Creates an update that removes all instances of the given value from the array value of the property.
 *
 * @param property the property
 * @param filter     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/pull/ $pull
 */
fun pullByFilter(property: KProperty<*>, filter: Bson): Bson = Updates.pull(property.path(), filter)

/**
 * Creates an update that removes from an array all elements that match the given filter.
 *
 * @param filter the query filter
 * @return the update
 * @mongodb.driver.manual reference/operator/update/pull/ $pull
 */
fun pullByFilter(filter: Bson): Bson = Updates.pullByFilter(filter)

/**
 * Creates an update that removes all instances of the given values from the array value of the property.
 *
 * @param property the property
 * @param values    the values
 * @param <TItem>   the value type
 * @return the update
 * @mongodb.driver.manual reference/operator/update/pull/ $pull
 */
fun <@OnlyInputTypes T> pullAll(property: KProperty<Iterable<T>?>, values: List<T?>?): Bson =
    Updates.pullAll(property.path(), values ?: emptyList())

/**
 * Creates an update that pops the first element of an array that is the value of the property.
 *
 * @param property the property
 * @return the update
 * @mongodb.driver.manual reference/operator/update/pop/ $pop
 */
fun <T> popFirst(property: KProperty<T>): Bson = Updates.popFirst(property.path())

/**
 * Creates an update that pops the last element of an array that is the value of the property.
 *
 * @param property the property
 * @return the update
 * @mongodb.driver.manual reference/operator/update/pop/ $pop
 */
fun <T> popLast(property: KProperty<T>): Bson = Updates.popLast(property.path())

/**
 * Creates an update that performs a bitwise and between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
fun <T : Number?> bitwiseAnd(property: KProperty<T>, value: Int): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise and between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
fun <T : Number?> bitwiseAnd(property: KProperty<T>, value: Long): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
fun <T : Number?> bitwiseOr(property: KProperty<T>, value: Int): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
fun <T : Number?> bitwiseOr(property: KProperty<T>, value: Long): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
fun <T : Number?> bitwiseXor(property: KProperty<T>, value: Int): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
fun <T : Number?> bitwiseXor(property: KProperty<T>, value: Long): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an InsertOneModel.
 */
fun <T> insertOne(document: T): InsertOneModel<T> = InsertOneModel(document)

/**
 * Creates an UpdateOneModel.
 */
fun <T> updateOne(filter: Bson, update: Bson, options: UpdateOptions = UpdateOptions()): UpdateOneModel<T> =
    UpdateOneModel(filter, update, options)

/**
 * Creates an UpdateManyModel.
 */
fun <T> updateMany(filter: Bson, update: Bson, options: UpdateOptions = UpdateOptions()): UpdateManyModel<T> =
    UpdateManyModel(filter, update, options)

/**
 * Creates an ReplaceOneModel.
 */
fun <T> replaceOne(filter: Bson, replacement: T, options: ReplaceOptions = ReplaceOptions()): ReplaceOneModel<T> =
    ReplaceOneModel(filter, replacement, options)

/**
 * Creates an DeleteOneModel.
 */
fun <T> deleteOne(filter: Bson, options: DeleteOptions = DeleteOptions()): DeleteOneModel<T> =
    DeleteOneModel(filter, options)

/**
 * Creates an DeleteManyModel.
 */
fun <T> deleteMany(filter: Bson, options: DeleteOptions = DeleteOptions()): DeleteManyModel<T> =
    DeleteManyModel(filter, options)

/**
 * Creates an [UpdateOptions] and set upsert to true.
 */
fun upsert(): UpdateOptions = UpdateOptions().upsert(true)

/**
 * Creates an [ReplaceOptions] and set upsert to true.
 */
fun replaceUpsert(): ReplaceOptions = ReplaceOptions().upsert(true)

/**
 * Creates an [FindOneAndUpdateOptions] and set upsert to true.
 */
fun findOneAndUpdateUpsert(): FindOneAndUpdateOptions = FindOneAndUpdateOptions().upsert(true)

