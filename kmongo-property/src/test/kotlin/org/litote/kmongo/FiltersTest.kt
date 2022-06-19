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

import com.mongodb.client.model.Filters
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class FiltersTest {

    class T(val s: List<String>, val string: String, val zz:List<Z>)

    class Z(val a: String)

    @Test
    fun `all works with Iterable sub interface`() {
        //check this compile
        T::s all setOf("test")
    }

    @Test
    fun `eq works with null`() {
        //check this compile
        T::s eq null
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

    @Test
    fun `regex works with a string property`() {
        val r1 = T::string regex "test"
        val r2 = T::string regex "test".toRegex().toPattern()
        val r3 = T::string regex "test".toRegex()
        val r4 = T::string.regex("test", "")

        assertEquals(r1.document, Filters.regex("string", "test").document)
        assertEquals(r1.document, r2.document)
        assertEquals(r1.document, r3.document)
        assertEquals(r1.document, r4.document)

    }

    @Test
    fun `regex works with a collection property`() {
        val r1 = T::s regex "test"
        val r2 = T::s regex "test".toRegex().toPattern()
        val r3 = T::s regex "test".toRegex()
        val r4 = T::s.regex("test", "")

        assertEquals(r1.document, Filters.regex("s", "test").document)
        assertEquals(r1.document, r2.document)
        assertEquals(r1.document, r3.document)
        assertEquals(r1.document, r4.document)

    }

    @Test
    fun `positional projection`() {
        val p = T::zz.pos(0) / Z::a
        assertEquals("zz.0.a", p.path())
    }

    data class Map1(val p1: Map<String, Map2>)
    data class Map2(val p2: Map<String, String>)

    @Test
    fun `multiple key projection`() {
        val m1 = Map1::p1.keyProjection("a")
        val m2 = Map2::p2.keyProjection("b")
        val doc = m1 / m2
        assertEquals("p1.a.p2.b", doc.path())
    }

}