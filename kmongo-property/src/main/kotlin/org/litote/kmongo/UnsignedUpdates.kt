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

import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

//ULong

/**
 * Creates an update that performs a bitwise and between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("ulongbitwiseAnd")
fun bitwiseAnd(property: KProperty<ULong>, value: Int): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise and between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("ulongbitwiseAnd")
fun bitwiseAnd(property: KProperty<ULong>, value: Long): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("ulongbitwiseOr")
fun bitwiseOr(property: KProperty<ULong>, value: Int): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("ulongbitwiseOr")
fun bitwiseOr(property: KProperty<ULong>, value: Long): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("ulongbitwiseXor")
fun bitwiseXor(property: KProperty<ULong>, value: Int): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("ulongbitwiseXor")
fun bitwiseXor(property: KProperty<ULong>, value: Long): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an update that increments the value of the property by the given value.
 *
 * @param property the property
 * @param number    the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/inc/ $inc
 */
@JvmName("ulonginc")
fun inc(property: KProperty<ULong>, number: Number): Bson = Updates.inc(property.path(), number)

/**
 * Creates an update that multiplies the value of the property by the given number.
 *
 * @param property the property
 * @param number    the non-null number
 * @return the update
 * @mongodb.driver.manual reference/operator/update/mul/ $mul
 */
@JvmName("ulongmul")
fun mul(property: KProperty<ULong>, number: Number): Bson = Updates.mul(property.path(), number)

//UInt

/**
 * Creates an update that performs a bitwise and between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("uintbitwiseAnd")
fun bitwiseAnd(property: KProperty<UInt>, value: Int): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise and between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("uintbitwiseAnd")
fun bitwiseAnd(property: KProperty<UInt>, value: Long): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("uintbitwiseOr")
fun bitwiseOr(property: KProperty<UInt>, value: Int): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("uintbitwiseOr")
fun bitwiseOr(property: KProperty<UInt>, value: Long): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("uintbitwiseXor")
fun bitwiseXor(property: KProperty<UInt>, value: Int): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("uintbitwiseXor")
fun bitwiseXor(property: KProperty<UInt>, value: Long): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an update that increments the value of the property by the given value.
 *
 * @param property the property
 * @param number    the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/inc/ $inc
 */
@JvmName("uintinc")
fun inc(property: KProperty<UInt>, number: Number): Bson = Updates.inc(property.path(), number)

/**
 * Creates an update that multiplies the value of the property by the given number.
 *
 * @param property the property
 * @param number    the non-null number
 * @return the update
 * @mongodb.driver.manual reference/operator/update/mul/ $mul
 */
@JvmName("uintmul")
fun mul(property: KProperty<UInt>, number: Number): Bson = Updates.mul(property.path(), number)

//UShort

/**
 * Creates an update that performs a bitwise and between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("ushortbitwiseAnd")
fun bitwiseAnd(property: KProperty<UShort>, value: Int): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise and between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("ushortbitwiseAnd")
fun bitwiseAnd(property: KProperty<UShort>, value: Long): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("ushortbitwiseOr")
fun bitwiseOr(property: KProperty<UShort>, value: Int): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("ushortbitwiseOr")
fun bitwiseOr(property: KProperty<UShort>, value: Long): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("ushortbitwiseXor")
fun bitwiseXor(property: KProperty<UShort>, value: Int): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("ushortbitwiseXor")
fun bitwiseXor(property: KProperty<UShort>, value: Long): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an update that increments the value of the property by the given value.
 *
 * @param property the property
 * @param number    the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/inc/ $inc
 */
@JvmName("ushortinc")
fun inc(property: KProperty<UShort>, number: Number): Bson = Updates.inc(property.path(), number)

/**
 * Creates an update that multiplies the value of the property by the given number.
 *
 * @param property the property
 * @param number    the non-null number
 * @return the update
 * @mongodb.driver.manual reference/operator/update/mul/ $mul
 */
@JvmName("ushortmul")
fun mul(property: KProperty<UShort>, number: Number): Bson = Updates.mul(property.path(), number)

//UByte

/**
 * Creates an update that performs a bitwise and between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("ubytebitwiseAnd")
fun bitwiseAnd(property: KProperty<UByte>, value: Int): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise and between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("ubytebitwiseAnd")
fun bitwiseAnd(property: KProperty<UByte>, value: Long): Bson = Updates.bitwiseAnd(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("ubytebitwiseOr")
fun bitwiseOr(property: KProperty<UByte>, value: Int): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise or between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/bit/ $bit
 */
@JvmName("ubytebitwiseOr")
fun bitwiseOr(property: KProperty<UByte>, value: Long): Bson = Updates.bitwiseOr(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given integer value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("ubytebitwiseXor")
fun bitwiseXor(property: KProperty<UByte>, value: Int): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an update that performs a bitwise xor between the given long value and the integral value of the property.
 *
 * @param property the property
 * @param value     the value
 * @return the update
 */
@JvmName("ubytebitwiseXor")
fun bitwiseXor(property: KProperty<UByte>, value: Long): Bson = Updates.bitwiseXor(property.path(), value)

/**
 * Creates an update that increments the value of the property by the given value.
 *
 * @param property the property
 * @param number    the value
 * @return the update
 * @mongodb.driver.manual reference/operator/update/inc/ $inc
 */
@JvmName("ubyteinc")
fun inc(property: KProperty<UByte>, number: Number): Bson = Updates.inc(property.path(), number)

/**
 * Creates an update that multiplies the value of the property by the given number.
 *
 * @param property the property
 * @param number    the non-null number
 * @return the update
 * @mongodb.driver.manual reference/operator/update/mul/ $mul
 */
@JvmName("ubytemul")
fun mul(property: KProperty<UByte>, number: Number): Bson = Updates.mul(property.path(), number)