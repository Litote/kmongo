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

import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOneById
import kotlin.test.assertEquals

enum class EnumWithBooleanProperty(val test: Boolean) {
    A(true), B(false)
}

@Serializable
data class ClassWithEnumProperty(val _id: String? = null, val v: EnumWithBooleanProperty = EnumWithBooleanProperty.A)


/**
 * [Crash on enum property in Annotation Introspector](https://github.com/Litote/kmongo/issues/3)
 */
class Issue3CrashOnEnumPropertyInAnnotationIntrospector : AllCategoriesKMongoBaseTest<ClassWithEnumProperty>() {

    @Test
    fun testSerializeAndDeserialize() {
        val e = ClassWithEnumProperty()
        col.insertOne(e)
        val e2 = col.findOneById(e._id!!)
        assertEquals(e, e2)
    }
}