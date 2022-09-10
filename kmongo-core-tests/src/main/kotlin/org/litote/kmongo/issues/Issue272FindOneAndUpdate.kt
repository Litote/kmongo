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

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.MongoOperator.*
import org.litote.kmongo.findOneAndUpdate
import org.litote.kmongo.json
import kotlin.test.assertEquals

@Serializable
data class Guild(
    val guildID: String,
    val generators: List<Generator> = emptyList(),
    val prefix: String = "<>",
)

@Serializable
data class Generator(
    val autoChat: Boolean,
)

/**
 *
 */
class Issue272FindOneAndUpdate : AllCategoriesKMongoBaseTest<Guild>() {

    @Test
    fun `test insert and load`() {
        val guildID = "715868269692583979"
        val route = "generators.0.autoChat"
        val value = true
        val d = Guild(guildID)
        col.insertOne(d)
        val d2 = col.findOneAndUpdate(
            "{ guildID: ${guildID.json} }",
            "{ $set: { \"$route\": $value } }",
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
        assertEquals(Guild(guildID, listOf(Generator(true))), d2)
    }

    @Test
    fun `test insert and load2`() {
        val guildID = "715868269692583979"
        val route = "prefix"
        val value = "!"
        val d = Guild(guildID)
        col.insertOne(d)
        val d2 = col.findOneAndUpdate(
            "{ guildID: ${guildID.json} }",
            "{ $set: { \"$route\": ${value.json} } }",
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
        assertEquals(Guild(guildID, prefix = "!"), d2)
    }
}