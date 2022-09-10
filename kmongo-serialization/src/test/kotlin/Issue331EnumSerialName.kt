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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.json
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Serializable
data class Jedi(
    @SerialName("Name")
    val name: String,
    val age: Int,
    val rating: Rating
)

@Serializable
enum class Rating {
    @SerialName("Favourite")
    FAVOURITE,

    @SerialName("Hated")
    HATED,
}

class Issue331EnumSerialName : AllCategoriesKMongoBaseTest<Jedi>() {

    @Test
    fun testSerialization() {
        val json = (Jedi::rating eq Rating.FAVOURITE).json
        assertEquals("""{"rating": "Favourite"}""", json)

        col.insertOne(Jedi("Luke Skywalker", 19, Rating.FAVOURITE))

        val jedi1 = col.findOne(Jedi::rating eq Rating.FAVOURITE)
        assertNotNull(jedi1)

    }
}