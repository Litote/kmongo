import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse
import kotlinx.serialization.stringify
import org.litote.kmongo.Id
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import org.litote.kmongo.newId
import kotlin.test.Test
import kotlin.test.assertEquals

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

/**
 *
 */
class IdModuleTest {

    @Serializable
    data class Data(@ContextualSerialization val id: Id<Data> = newId())

    @ImplicitReflectionSerializer
    @Test
    fun testSerializationAndDeserialization() {
        val json = Json(
            context = IdKotlinXSerializationModule
        )
        val data = Data()
        val serialized = json.stringify(data)
        assertEquals(data, json.parse(serialized))
    }
}