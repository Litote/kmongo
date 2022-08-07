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

import org.junit.Test

/**
 *
 */
class UnsignedUpdatesTest {

    class T(val uLong: ULong, val uInt: UInt, val uShort: UShort, val uByte: UByte)

    @Test
    fun `inc compiles for unsigned numbers`() {
        //check this compile
        inc(T::uLong, 1)
        inc(T::uInt, 1)
        inc(T::uShort, 1)
        inc(T::uByte, 1)
    }

    @Test
    fun `mul compiles for unsigned numbers`() {
        //check this compile
        mul(T::uLong, 1)
        mul(T::uInt, 1)
        mul(T::uShort, 1)
        mul(T::uByte, 1)
    }

    @Test
    fun `bitwiseAnd compiles for unsigned numbers`() {
        //check this compile
        bitwiseAnd(T::uLong, 1)
        bitwiseAnd(T::uInt, 1)
        bitwiseAnd(T::uShort, 1)
        bitwiseAnd(T::uByte, 1)
        bitwiseAnd(T::uLong, 1L)
        bitwiseAnd(T::uInt, 1L)
        bitwiseAnd(T::uShort, 1L)
        bitwiseAnd(T::uByte, 1L)
    }

    @Test
    fun `bitwiseOr compiles for unsigned numbers`() {
        //check this compile
        bitwiseOr(T::uLong, 1)
        bitwiseOr(T::uInt, 1)
        bitwiseOr(T::uShort, 1)
        bitwiseOr(T::uByte, 1)
        bitwiseOr(T::uLong, 1L)
        bitwiseOr(T::uInt, 1L)
        bitwiseOr(T::uShort, 1L)
        bitwiseOr(T::uByte, 1L)
    }

    @Test
    fun `bitwiseXor compiles for unsigned numbers`() {
        //check this compile
        bitwiseXor(T::uLong, 1)
        bitwiseXor(T::uInt, 1)
        bitwiseXor(T::uShort, 1)
        bitwiseXor(T::uByte, 1)
        bitwiseXor(T::uLong, 1L)
        bitwiseXor(T::uInt, 1L)
        bitwiseXor(T::uShort, 1L)
        bitwiseXor(T::uByte, 1L)
    }
}