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

package org.bson.codecs.pojo

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecConfigurationException
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdGenerator.Companion.defaultGenerator
import java.util.HashMap
import java.util.Locale

/**
 * Improves [MapPropertyCodecProvider].
 */
internal object KeyObjectMapPropertyCodecProvider : PropertyCodecProvider {

    override fun <T> get(type: TypeWithTypeParameters<T>, registry: PropertyCodecRegistry): Codec<T>? {

        if (Map::class.java.isAssignableFrom(type.type) && type.typeParameters.size == 2) {
            val keyType = type.typeParameters[0].type
            if (keyType.isAssignableFrom(Id::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return IdMapCodec(
                        type.type as Class<Map<Id<*>, Any>>,
                        registry.get(type.typeParameters[1]) as Codec<Any>
                ) as Codec<T>
            } else if (keyType == Locale::class.java) {
                @Suppress("UNCHECKED_CAST")
                return LocaleMapCodec(
                        type.type as Class<Map<Locale, Any>>,
                        registry.get(type.typeParameters[1]) as Codec<Any>
                ) as Codec<T>
            }
        }

        return null
    }

    abstract class KeyObjectMapCodec<K, T>(
            val mapEncoderClass: Class<Map<K, T>>,
            val codec: Codec<T>) : Codec<Map<K, T>> {

        private val instance: MutableMap<K, T>
            get() {
                if (mapEncoderClass.isInterface) {
                    return HashMap()
                }
                try {
                    return mapEncoderClass.newInstance().toMutableMap()
                } catch (e: Exception) {
                    throw CodecConfigurationException(e.message, e)
                }
            }

        abstract fun encode(key: K): String

        abstract fun decode(key: String): K

        override fun encode(writer: BsonWriter, map: Map<K, T>, encoderContext: EncoderContext) {
            writer.writeStartDocument()
            for ((key, value) in map) {
                writer.writeName(key.toString())
                codec.encode(writer, value, encoderContext)
            }
            writer.writeEndDocument()
        }

        override fun decode(reader: BsonReader, context: DecoderContext): Map<K, T> {
            reader.readStartDocument()
            val map = instance
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                map.put(decode(reader.readName()), codec.decode(reader, context))
            }
            reader.readEndDocument()
            return map
        }

        override fun getEncoderClass(): Class<Map<K, T>> {
            return mapEncoderClass
        }
    }

    class IdMapCodec<T>(mapEncoderClass: Class<Map<Id<*>, T>>,
                        codec: Codec<T>) : KeyObjectMapCodec<Id<*>, T>(mapEncoderClass, codec) {

        override fun encode(key: Id<*>): String = key.toString()

        override fun decode(key: String): Id<*> = defaultGenerator.create(key)
    }

    class LocaleMapCodec<T>(mapEncoderClass: Class<Map<Locale, T>>,
                            codec: Codec<T>) : KeyObjectMapCodec<Locale, T>(mapEncoderClass, codec) {

        override fun encode(key: Locale): String = key.toLanguageTag()

        override fun decode(key: String): Locale = Locale.forLanguageTag(key)
    }
}