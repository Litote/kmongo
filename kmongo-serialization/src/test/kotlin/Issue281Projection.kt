/*
 * Copyright (C) 2016/2021 Litote
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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.Ignore
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.projection
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertEquals

@Serializable
data class MyDocumentClass(
    val identifiers: List<@Serializable(with = IdentifierSerializer::class) Identifier>,
    //... more fields, therefore the projection function makes sense
)

data class Identifier(val namespace: String)

object IdentifierSerializer : KSerializer<Identifier> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Identifier) {
        encoder.encodeString(value.namespace)
    }

    override fun deserialize(decoder: Decoder): Identifier {
        val string = decoder.decodeString()
        return Identifier(string)
    }
}

@Serializable
data class MyDocumentClassProjection(
    val identifiers: List<@Serializable(with = IdentifierSerializer::class) Identifier>,
)

/**
 *
 */
class Issue281Projection : AllCategoriesKMongoBaseTest<MyDocumentClass>() {

    @Test
    @Ignore
    fun `test projection`() {
        val c = MyDocumentClass(listOf(Identifier("namespace")))
        col.insertOne(c)
        val p = col.projection(MyDocumentClass::identifiers)
        assertEquals("namespace", p.first()?.first()?.namespace)
    }

    @Test
    fun `test projection with custom class`() {
        val c = MyDocumentClass(listOf(Identifier("namespace")))
        col.insertOne(c)
        val p = col.withDocumentClass<MyDocumentClassProjection>()
            .find()
            .projection(MyDocumentClass::identifiers)
            .toList()
        assertEquals("namespace", p.first().identifiers.first().namespace)
    }
}