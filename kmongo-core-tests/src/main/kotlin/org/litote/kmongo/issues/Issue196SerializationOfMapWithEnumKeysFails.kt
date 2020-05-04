/*
 * Copyright (C) 2016/2020 Litote
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
import org.junit.experimental.categories.Category
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.SerializationMappingCategory
import org.litote.kmongo.findOne
import org.litote.kmongo.issues.Issue196SerializationOfMapWithEnumKeysFails.AdventurerEntity
import org.litote.kmongo.json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
@Category(JacksonMappingCategory::class, SerializationMappingCategory::class)
class Issue196SerializationOfMapWithEnumKeysFails : KMongoBaseTest<AdventurerEntity>() {

    enum class Rarity {
        TWO, THREE, FOUR, FIVE, SIX;
    }

    @Serializable
    data class AdventurerEntity(
        val icons: Map<Rarity, String?>,
        val enumEnumMap: Map<Rarity, Rarity>
    )

    @Test
    fun serializationOfMapWithEnumKeysSucceed() {
        val e = AdventurerEntity(mapOf(Rarity.TWO to "2"), mapOf(Rarity.TWO to Rarity.FIVE))

        val json = e.json

        assertEquals(
            """{"icons": {"TWO": "2"}, "enumEnumMap": {"TWO": "FIVE"}}""".replace(" ", ""),
            json.replace(" ", "")
        )
    }

    @Test
    fun insertAndLoadIsOk() {
        val e = AdventurerEntity(mapOf(Rarity.TWO to "2"), mapOf(Rarity.TWO to Rarity.FIVE))
        col.insertOne(e)
        assertEquals(e, col.findOne())
    }
}