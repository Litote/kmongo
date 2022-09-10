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
import org.litote.kmongo.eq
import org.litote.kmongo.issues.Issue157FilterForEnumValueFails.Data
import kotlin.test.assertEquals

/**
 *
 */
class Issue157FilterForEnumValueFails : AllCategoriesKMongoBaseTest<Data>() {

    @Serializable
    enum class Status { ACTIVE, INACTIVE }

    @Serializable
    data class Data(val status: Status)

    @Test
    fun `querying with enum is ok`() {
        val data = Data(Status.ACTIVE)
        col.insertOne(data)
        val count = col.countDocuments(Data::status eq Status.ACTIVE)
        assertEquals(1, count)
    }
}