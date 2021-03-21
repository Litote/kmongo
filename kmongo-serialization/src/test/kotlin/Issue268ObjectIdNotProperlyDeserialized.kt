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

import kotlinx.serialization.Contextual
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.newId
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable

@Serializable
sealed class Animal {
    abstract val _id: Id<Animal>
    abstract val _type: String
    abstract val name: String
    abstract val lifeExpectancyYears: Int
}

@Serializable
data class Fish(
    @Contextual
    override val _id: Id<Animal> = newId(),
    override val _type: String = Fish::class.java.simpleName,
    override val name: String,
    override val lifeExpectancyYears: Int,
    val deepSeaFish: Boolean,
) : Animal()

/**
 *
 */
class Issue268ObjectIdNotProperlyDeserialized : AllCategoriesKMongoBaseTest<Animal>() {

    @Test
    fun `test insert and load`() {
        val fishInstance = Fish(name = "Lanternfish", lifeExpectancyYears = 8, deepSeaFish = true)
            .also { col.insertOne(it) }
        val fishInstanceAfterDeserialization = col.findOne(Animal::name eq fishInstance.name)
            ?: error("fish not found")

        assertEquals(fishInstance._id::class, fishInstanceAfterDeserialization._id::class)
    }

}