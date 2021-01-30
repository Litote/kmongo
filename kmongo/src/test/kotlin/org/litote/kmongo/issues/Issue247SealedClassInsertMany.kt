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

package org.litote.kmongo.issues

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import kotlin.test.assertTrue

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "value")
sealed class SealedClass

@JsonTypeName("A")
data class A(val s: String = "s") : SealedClass()

@JsonTypeName("B")
data class B(val i: Int = 1) : SealedClass()

class Issue247SealedClassInsertMany : AllCategoriesKMongoBaseTest<SealedClass>() {

    @Test
    fun `test insert and load`() {
        col.insertOne(A())
        col.insertOne(B())
        assertTrue(col.find().toList().contains(A()))
        assertTrue(col.find().toList().contains(B()))
    }

    @Test
    fun `test insert many and load`() {
        col.insertMany(listOf(A(), B()))
        assertTrue(col.find().toList().contains(A()))
        assertTrue(col.find().toList().contains(B()))
    }

}