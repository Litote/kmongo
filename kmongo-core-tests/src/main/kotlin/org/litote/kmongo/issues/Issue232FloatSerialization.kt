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
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import kotlin.test.assertEquals

@Serializable
data class FloatContainer(val float: Float, val double: Double)

/**
 *
 */
class Issue232FloatSerialization : AllCategoriesKMongoBaseTest<FloatContainer>() {

    @Test
    fun `test insert and load`() {
        val e = FloatContainer(2.0F, 2.0)
        col.insertOne(e)
        assertEquals(e, col.findOne())
    }
}