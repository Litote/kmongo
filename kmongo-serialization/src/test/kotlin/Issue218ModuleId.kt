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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import org.litote.kmongo.newId
import kotlin.test.assertEquals

/**
 *
 */
class Issue218ModuleId : AllCategoriesKMongoBaseTest<Issue218ModuleId.Data>() {

    @Serializable
    data class Data(@Contextual val _id: Id<Data> = newId())

    @Test
    fun canSerializeAndDeserializeInJson() {
        val data = Data()
        col.insertOne(data)
        val loadedData = col.findOne(data::_id eq data._id)
        assertEquals(data._id, loadedData!!._id)

        val json = Json {
            serializersModule = IdKotlinXSerializationModule
        }
        val serialized = json.encodeToString(data)
        assertEquals(data, json.decodeFromString(serialized))
    }
}