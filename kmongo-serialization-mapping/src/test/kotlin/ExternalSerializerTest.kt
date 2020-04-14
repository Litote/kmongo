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
@file:ContextualSerialization(LocalDateTime::class, ExternalSerializerTest.Role::class)

package org.litote.kmongo.serialization

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

/**
 *
 */
class ExternalSerializerTest {

    sealed class Role(val level: Int, val name: String) {

        object ADMIN : Role(100, "Admin")

        object USER : Role(10, "User")

        object NONE : Role(0, "None")

    }

    data class User(
        val username: String,
        val email: String,
        val firstName: String,
        val lastName: String,
        val password: String,
        val role: Role,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    @Serializer(forClass = User::class)
    object UserSerializer {}


    @Serializer(forClass = Role::class)
    object RoleSerializer : KSerializer<Role> {
        override val descriptor: SerialDescriptor =
            PrimitiveDescriptor("RoleStringSerializer", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Role =
            decoder.decodeString().toRole()

        override fun serialize(encoder: Encoder, value: Role) =
            encoder.encodeString(value.name)

        fun String.toRole(): Role = when (this) {
            Role.ADMIN.name -> Role.ADMIN
            Role.USER.name -> Role.USER
            else -> Role.NONE
        }
    }

    @Serializer(forClass = Role.ADMIN::class)
    object AdminRoleSerializer : KSerializer<Role.ADMIN> {
        override val descriptor: SerialDescriptor =
            PrimitiveDescriptor("AdminRoleStringSerializer", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Role.ADMIN =
            Role.ADMIN

        override fun serialize(encoder: Encoder, value: Role.ADMIN) =
            encoder.encodeString(value.name)
    }

    @Serializer(forClass = Role.USER::class)
    object UserRoleSerializer : KSerializer<Role.USER> {
        override val descriptor: SerialDescriptor =
            PrimitiveDescriptor("UserRoleStringSerializer", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Role.USER =
            Role.USER

        override fun serialize(encoder: Encoder, value: Role.USER) =
            encoder.encodeString(value.name)
    }

    @Serializer(forClass = Role.NONE::class)
    object NoneRoleSerializer : KSerializer<Role.NONE> {
        override val descriptor: SerialDescriptor =
            PrimitiveDescriptor("NoneRoleStringSerializer", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Role.NONE =
            Role.NONE

        override fun serialize(encoder: Encoder, value: Role.NONE) =
            encoder.encodeString(value.name)
    }

    @kotlinx.serialization.ImplicitReflectionSerializer
    @Test
    fun `encode and decode User`() {
        registerSerializer(UserSerializer)
        registerSerializer(RoleSerializer)
        registerSerializer(AdminRoleSerializer)
        registerSerializer(UserRoleSerializer)
        registerSerializer(NoneRoleSerializer)
        val user = User(
            "test",
            "test@test.com",
            "TestName",
            "TestLastName",
            "1234",
            Role.ADMIN,
            LocalDateTime.now(),
            LocalDateTime.now()
        )
        val codec = SerializationCodec(User::class, configuration)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, user, EncoderContext.builder().build())

        println(document)

        val newUser = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(user, newUser)
    }
}