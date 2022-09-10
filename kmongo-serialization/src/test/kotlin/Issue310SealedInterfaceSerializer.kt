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

package org.litote.kmongo.issues

import kotlinx.serialization.Serializable
import org.junit.Ignore
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import kotlin.test.assertEquals

@Serializable
sealed interface Foo {
    @Serializable
    data class FooA(val x: Int) : Foo
    @Serializable
    data class FooB(val x: Double) : Foo
    @Serializable
    data class FooC(val z: String) : Foo
}

/**
 *
 */
class Issue310SealedInterfaceSerializer : AllCategoriesKMongoBaseTest<Foo>() {

    @Ignore
    @Test
    fun insertAndLoad() {
        val fooA = Foo.FooA(1)
        col.insertOne(fooA)
        assertEquals(fooA, col.findOne())
    }
}