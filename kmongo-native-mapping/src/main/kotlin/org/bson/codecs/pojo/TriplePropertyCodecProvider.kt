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
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecConfigurationException

/**
 *
 */
internal object TriplePropertyCodecProvider : PropertyCodecProvider {

    override fun <T> get(type: TypeWithTypeParameters<T>, registry: PropertyCodecRegistry): Codec<T>? {
        if (Triple::class.java == type.type) {
            val firstCodec = registry.get(type.typeParameters[0])
                    ?: throw CodecConfigurationException("no codec found for ${type.typeParameters[0]}")
            val secondCodec = registry.get(type.typeParameters[1])
                    ?: throw CodecConfigurationException("no codec found for ${type.typeParameters[1]}")
            val thirdCodec = registry.get(type.typeParameters[2])
                    ?: throw CodecConfigurationException("no codec found for ${type.typeParameters[2]}")
            @Suppress("UNCHECKED_CAST")
            return TripleCodec(firstCodec as Codec<Any>, secondCodec as Codec<Any>, thirdCodec as Codec<Any>) as Codec<T>
        } else {
            return null
        }
    }

    private class TripleCodec(val firstCodec: Codec<Any>, val secondCodec: Codec<Any>, val thirdCodec: Codec<Any>) : Codec<Triple<*, *, *>> {

        override fun encode(writer: BsonWriter, pair: Triple<*, *, *>, encoderContext: EncoderContext) {
            writer.writeStartDocument()
            writer.writeName("first")
            firstCodec.encode(writer, pair.first, encoderContext)
            writer.writeName("second")
            secondCodec.encode(writer, pair.second, encoderContext)
            writer.writeName("third")
            thirdCodec.encode(writer, pair.third, encoderContext)
            writer.writeEndDocument()
        }

        override fun decode(reader: BsonReader, context: DecoderContext): Triple<*, *, *> {
            reader.readStartDocument()
            reader.readName()
            val first = firstCodec.decode(reader, context)
            reader.readName()
            val second = secondCodec.decode(reader, context)
            reader.readName()
            val third = thirdCodec.decode(reader, context)
            val triple = Triple(first, second, third)
            reader.readEndDocument()
            return triple
        }

        override fun getEncoderClass(): Class<Triple<*, *, *>> {
            return Triple::class.java
        }
    }
}