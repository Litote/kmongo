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
internal object PairPropertyCodecProvider : PropertyCodecProvider {

    override fun <T> get(type: TypeWithTypeParameters<T>, registry: PropertyCodecRegistry): Codec<T>? {
        if (Pair::class.java == type.type) {
            val firstCodec = registry.get(type.typeParameters[0])
                    ?: throw CodecConfigurationException("no codec found for ${type.typeParameters[0]}")
            val secondCodec = registry.get(type.typeParameters[1])
                    ?: throw CodecConfigurationException("no codec found for ${type.typeParameters[1]}")
            @Suppress("UNCHECKED_CAST")
            return PairCodec(firstCodec as Codec<Any>, secondCodec as Codec<Any>) as Codec<T>
        } else {
            return null
        }
    }

    private class PairCodec(val firstCodec: Codec<Any>, val secondCodec: Codec<Any>) : Codec<Pair<*, *>> {

        override fun encode(writer: BsonWriter, pair: Pair<*, *>, encoderContext: EncoderContext) {
            writer.writeStartDocument()
            writer.writeName("first")
            firstCodec.encode(writer, pair.first, encoderContext)
            writer.writeName("second")
            secondCodec.encode(writer, pair.second, encoderContext)
            writer.writeEndDocument()
        }

        override fun decode(reader: BsonReader, context: DecoderContext): Pair<*, *> {
            reader.readStartDocument()
            reader.readName()
            val first = firstCodec.decode(reader, context)
            reader.readName()
            val second = secondCodec.decode(reader, context)
            val pair = Pair(first, second)
            reader.readEndDocument()
            return pair
        }

        override fun getEncoderClass(): Class<Pair<*, *>> {
            return Pair::class.java
        }
    }
}