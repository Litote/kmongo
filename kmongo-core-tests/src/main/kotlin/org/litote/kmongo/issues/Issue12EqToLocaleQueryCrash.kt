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

import com.mongodb.client.model.Filters
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.issues.Issue12EqToLocaleQueryCrash.ClassWithLocalField
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class Issue12EqToLocaleQueryCrash : AllCategoriesKMongoBaseTest<ClassWithLocalField>() {

    @Serializable
    data class ClassWithLocalField(@Contextual val locale: Locale)

    @Test
    fun testSerializeAndDeserializeLocale() {
        val l = ClassWithLocalField(Locale.ENGLISH)
        col.insertOne(l)
        val l2 = col.findOne(Filters.eq("locale", Locale.ENGLISH))
        assertEquals(l, l2)
        val l3 = col.findOne(l::locale eq Locale.ENGLISH)
        assertEquals(l, l3)
    }

}