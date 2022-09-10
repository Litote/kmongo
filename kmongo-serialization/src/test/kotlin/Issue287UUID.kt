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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.serialization.registerSerializer
import java.util.UUID
import kotlin.test.assertNotNull

/**
 *
 */
@ExperimentalSerializationApi
@Serializer(forClass = UUID::class)
object EntitySerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            serialName = "com.malachid.poc.Entity",
            kind = PrimitiveKind.STRING
        )

    override fun deserialize(decoder: Decoder): UUID =
        UUID.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

@ExperimentalSerializationApi
@Serializable
data class ClassUUID(val _id: @Serializable(with = EntitySerializer::class) UUID)

/**
 *
 */
@ExperimentalSerializationApi
class Issue287UUID : AllCategoriesKMongoBaseTest<ClassUUID>() {

    @Test
    fun canFindOne() {
        val c = col.withCodecRegistry(
            CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(
                    object : Codec<UUID> {
                        override fun encode(writer: BsonWriter, value: UUID, encoderContext: EncoderContext) {
                            writer.writeString(value.toString())
                        }

                        override fun getEncoderClass(): Class<UUID> = UUID::class.java

                        override fun decode(reader: BsonReader, decoderContext: DecoderContext): UUID {
                            return UUID.fromString(reader.readString())
                        }
                    }
                ),
                col.codecRegistry
            ))
        registerSerializer(EntitySerializer)
        val uuid = UUID.randomUUID()
        c.insertOne(ClassUUID(uuid))
        assertNotNull(c.findOne(ClassUUID::_id eq uuid))
    }

}
