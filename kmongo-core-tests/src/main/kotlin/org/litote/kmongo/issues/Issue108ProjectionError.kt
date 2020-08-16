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
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import org.litote.kmongo.issues.Issue108ProjectionError.Data
import org.litote.kmongo.newId
import org.litote.kmongo.projection
import kotlin.test.assertEquals

/**
 *
 */
class Issue108ProjectionError : AllCategoriesKMongoBaseTest<Data>() {

    @Serializable
    data class Data(@Contextual val _id: Id<Data> = newId(), @Contextual  val nation: Nation)
    @Serializable
    data class Nation(val name: String)

    @Test
    fun `deserializing is ok`() {
        val data = Data(nation = Nation("b"))
        col.insertOne(data)
        mongoClient.startSession().use {
            assertEquals(Nation("b"), col.projection(it, Data::nation, Data::_id eq data._id).first())
        }
    }

}