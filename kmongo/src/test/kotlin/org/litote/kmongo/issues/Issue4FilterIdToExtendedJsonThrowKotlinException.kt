/*
 * Copyright (C) 2016 Litote
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

import org.junit.Test
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.findOneById
import org.litote.kmongo.issues.Issue4FilterIdToExtendedJsonThrowKotlinException.ClassWithValue
import org.litote.kmongo.issues.Issue4FilterIdToExtendedJsonThrowKotlinException.EnumWithBooleanProperty.A
import org.litote.kmongo.issues.Issue4FilterIdToExtendedJsonThrowKotlinException.EnumWithBooleanProperty.B
import org.litote.kmongo.updateOneById
import kotlin.reflect.KClass
import kotlin.test.assertEquals


/**
 * [filterIdToExtendedJson throw Kotlin exception](https://github.com/Litote/kmongo/issues/4)
 */
class Issue4FilterIdToExtendedJsonThrowKotlinException : KMongoBaseTest<ClassWithValue>() {

    data class ClassWithValue(val _id: String? = null, val v: String = "a", val e: EnumWithBooleanProperty = A)

    enum class EnumWithBooleanProperty(val test: Boolean) {
        A(true), B(false)
    }

    override fun getDefaultCollectionClass(): KClass<ClassWithValue> {
        return ClassWithValue::class
    }

    @Test
    fun testSerializeAndDeserialize() {
        val e = ClassWithValue()
        col.insertOne(e)
        col.updateOneById(e._id!!, ClassWithValue(null, "b", B))
        assertEquals("b", col.findOneById(e._id)!!.v)
        assertEquals(B, col.findOneById(e._id)!!.e)
        col.updateOneById(e._id, "{$set:{v:'a',e:'A'}}")
        assertEquals("a", col.findOneById(e._id)!!.v)
        assertEquals(A, col.findOneById(e._id)!!.e)
    }
}