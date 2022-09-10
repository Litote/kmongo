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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.save
import org.litote.kmongo.serialization.registerSerializer
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
data class DBPlayer(
    @SerialName("_id") @Contextual val uuid: UUID,
)

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        deserialized = true
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        serialized = true
        encoder.encodeString(value.toString())
    }
}

private var deserialized = false
private var serialized = false

/**
 *
 */
class Issue365UUIDSerializer : AllCategoriesKMongoBaseTest<DBPlayer>() {

    init {
        registerSerializer(UUIDSerializer)
    }

    @Test
    fun testInsertSerialization() {
        val test = DBPlayer(UUID.randomUUID())

        col.insertOne(test)
        val test2 = col.findOne()

        assertEquals(test, test2)
        assertTrue(deserialized)
        assertTrue(serialized)
    }

    @Test
    fun testSaveSerialization() {
        val test = DBPlayer(UUID.randomUUID())

        col.save(test)
        val test2 = col.findOne()

        assertEquals(test, test2)
        assertTrue(deserialized)
        assertTrue(serialized)
    }
}