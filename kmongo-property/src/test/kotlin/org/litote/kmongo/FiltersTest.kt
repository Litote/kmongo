/*
 * Copyright (C) 2017/2019 Litote
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
class FiltersTest {

    class T(val s: List<String>)

    @Test
    fun `all works with Iterable sub interface`() {
        //check this compile
        T::s all setOf("test")
    }

    @Test
    fun `in works with a collection property`() {
        //check this compile
        T::s `in` setOf("test")
    }

    @Test
    fun `nin works with a collection property`() {
        //check this compile
        T::s nin setOf("test")
    }
}