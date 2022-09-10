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

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.addToSet
import org.litote.kmongo.newId
import org.litote.kmongo.push
import org.litote.kmongo.updateOneById

@Serializable
data class User(@Contextual val _id: Id<User> = newId(), val tokens: List<Token> = emptyList())

@Serializable
data class Token(val a: String, val b: String)

/**
 *
 */
class Issue257BsonFieldNotSerialized : AllCategoriesKMongoBaseTest<User>() {

    @Test(expected = IllegalStateException::class)
    fun `updateOneById throws an error when using BsonField with push`() {
        col
            .updateOneById(
                User()._id,
                User::tokens push Token("a", "b")
            )
    }

    @Test(expected = IllegalStateException::class)
    fun `updateOneById throws an error when using BsonField with addToSet`() {
        col
            .updateOneById(
                User()._id,
                User::tokens addToSet listOf(Token("a", "b"))
            )
    }
}