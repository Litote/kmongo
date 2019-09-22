/*
 * Copyright (C) 2017 Litote
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

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.issues.Issue49ShouldNotFilterInnerObjectIdOnUpdate.User
import org.litote.kmongo.save
import org.litote.kmongo.updateOne
import kotlin.test.assertEquals

/**
 *
 */
class Issue49ShouldNotFilterInnerObjectIdOnUpdate : AllCategoriesKMongoBaseTest<User>() {

    @Serializable
    data class Planet(@ContextualSerialization val _id: ObjectId, val name: String)

    @Serializable
    data class User(@ContextualSerialization val _id: ObjectId, val name: String, val planets: List<Planet>)

    @Test
    fun updateShouldNotRemoveInnerObjectId() {
        val user = User(ObjectId.get(), "user", listOf(Planet(ObjectId.get(), "planet")))
        col.insertOne(user)
        assertEquals(user, col.findOne())
        col.updateOne("{}", user)
        assertEquals(user, col.findOne())
        col.save(user)
        assertEquals(user, col.findOne())
    }
}