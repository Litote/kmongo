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

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.coroutine.KMongoReactiveStreamsCoroutineBaseTest
import org.litote.kmongo.issues.SealedClass.SecondLevelSealedClass.A
import org.litote.kmongo.issues.SealedClass.SecondLevelSealedClass.B
import kotlin.test.assertTrue

@Serializable
sealed class SealedClass {

    @Serializable
    sealed class SecondLevelSealedClass : SealedClass() {

        @Serializable
        data class A(val s: String = "s") : SecondLevelSealedClass()

        @Serializable
        data class B(val i: Int = 1) : SecondLevelSealedClass()

    }

    @Serializable
    data class C(val c: String = "s") : SealedClass()


}

class Issue247SealedClassInsertMany : KMongoReactiveStreamsCoroutineBaseTest<SealedClass>() {

    @Test
    fun `test insert and load`() = runBlocking {
        col.insertOne(A())
        col.insertOne(B())
        assertTrue(col.find().toList().contains(A()))
        assertTrue(col.find().toList().contains(B()))
    }

    @Test
    fun `test insert many and load`() = runBlocking {
        col.insertMany(listOf(A(), B()))
        assertTrue(col.find().toList().contains(A()))
        assertTrue(col.find().toList().contains(B()))
    }

}