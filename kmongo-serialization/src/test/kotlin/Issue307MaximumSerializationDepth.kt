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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.withDocumentClass
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertEquals

@Serializable
data class Bla(@Serializable(with = PathSerializer::class) var path: Path)

object PathSerializer : KSerializer<Path> {
    override val descriptor = PrimitiveSerialDescriptor("Path", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Path) = encoder.encodeString(value.toAbsolutePath().toString())
    override fun deserialize(decoder: Decoder): Path = Paths.get(decoder.decodeString())
}

/**
 *
 */
@ExperimentalSerializationApi
class Issue307MaximumSerializationDepth : AllCategoriesKMongoBaseTest<Bla>() {
    @Test
    fun canFindOne() {
        val path = Paths.get("e")
        val bla = Bla(path)
        col.insertOne(bla)
        assertEquals(Bla(path.toAbsolutePath()), col.findOne())
    }
}