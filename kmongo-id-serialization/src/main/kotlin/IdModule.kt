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
package org.litote.kmongo.id.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.StringId
import kotlin.reflect.KClass


/**
 * The Id kotlin.x Serialization module.
 */
val IdKotlinXSerializationModule: SerializersModule by lazy {
    SerializersModule {
        contextual(Id::class, IdSerializer())
        contextual(StringId::class, IdSerializer())
        if (IdGenerator.defaultGenerator.idClass != StringId::class) {
            @Suppress("UNCHECKED_CAST")
            contextual(
                IdGenerator.defaultGenerator.idClass as KClass<Id<*>>,
                IdSerializer()
            )
        }
    }
}

private class IdSerializer<T : Id<*>> : KSerializer<T> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IdSerializer", PrimitiveKind.STRING)

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): T =
        IdGenerator.defaultGenerator.create(decoder.decodeString()) as T

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.toString())
    }

}