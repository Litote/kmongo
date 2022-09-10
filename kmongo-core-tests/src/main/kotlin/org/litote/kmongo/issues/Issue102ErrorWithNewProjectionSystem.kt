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

import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.Id
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.issues.Issue102ErrorWithNewProjectionSystem.NationRelation
import org.litote.kmongo.newId
import org.litote.kmongo.projection
import kotlin.test.assertEquals

/**
 *
 */
@Category(JacksonMappingCategory::class)
class Issue102ErrorWithNewProjectionSystem : KMongoBaseTest<NationRelation>() {

    enum class Level { NATION, NONE, OTHER }

    data class Nation(val _id: Id<Nation>)
    data class NationRelation(val wish: Level, val nation: Id<Nation>, val other: Id<Nation>)

    private fun getRelationWish(nation: Id<Nation>, other: Id<Nation>): Level = when (nation) {
        other -> Level.NATION
        else -> col.projection(
            NationRelation::wish,
            and(NationRelation::nation eq nation, NationRelation::other eq other)
        ).firstOrNull() ?: Level.NONE
    }

    @Test
    fun `test enum deserialization`() {
        val id1: Id<Nation> = newId()
        val id2: Id<Nation> = newId()
        col.insertOne(NationRelation(Level.OTHER, id1, id2))
        val level = getRelationWish(id1, id2)
        assertEquals(Level.OTHER, level)
    }
}