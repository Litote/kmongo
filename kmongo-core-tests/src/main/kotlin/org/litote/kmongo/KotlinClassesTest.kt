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

package org.litote.kmongo

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.KotlinClassesTest.KotlinClasses
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class KotlinClassesTest : AllCategoriesKMongoBaseTest<KotlinClasses>() {

    @Serializable
    data class KotlinClasses(var pair: Pair<String, Int>, var triple: Triple<String, Double, @Contextual Locale>)

    @Test
    fun savedAndLoad() {
        val c = KotlinClasses(Pair("a", 1), Triple("b", 1.0, Locale.ENGLISH))
        col.save(c)
        assertEquals(c, col.findOne())
    }
}