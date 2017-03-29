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

import com.mongodb.client.model.Filters
import org.junit.Test
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.issues.Issue12EqToLocaleQueryCrash.ClassWithLocalField
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.test.assertEquals

/**
 *
 */
class Issue12EqToLocaleQueryCrash : KMongoBaseTest<ClassWithLocalField>() {

    data class ClassWithLocalField(val locale: Locale)

    override fun getDefaultCollectionClass(): KClass<ClassWithLocalField> {
        return ClassWithLocalField::class
    }

    @Test
    fun testSerializeAndDeserializeLocale() {
        println("")
        val l = ClassWithLocalField(Locale.ENGLISH)
        col.insertOne(l)
        val l2 = col.findOne(Filters.eq("locale", Locale.ENGLISH))
        assertEquals(l, l2)
    }

}