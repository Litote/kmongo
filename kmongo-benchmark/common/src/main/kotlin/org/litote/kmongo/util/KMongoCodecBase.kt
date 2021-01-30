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

package org.litote.kmongo.util

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.codecs.Codec
import org.bson.codecs.Decoder
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistry

/**
 * Base class for generated codecs.
 */
abstract class KMongoCodecBase<T>(private val codecRegistryProvider: () -> (CodecRegistry)) : Codec<T> {

    val codecRegistry: CodecRegistry get() = codecRegistryProvider.invoke()

    inline fun <reified C : Any> decodeClass(reader: BsonReader, decoderContext: DecoderContext): C =
        decoderContext.decodeWithChildContext(codecRegistry.get(C::class.java), reader)

    fun <C> decodeClass(decoder: Decoder<C>, reader: BsonReader, decoderContext: DecoderContext): C =
        decoderContext.decodeWithChildContext(decoder, reader)

    inline fun <reified C : Any> decodeList(reader: BsonReader, decoderContext: DecoderContext): List<C> {
        reader.readStartArray()

        val list = mutableListOf<C>()
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(decodeClass(reader, decoderContext))
        }

        reader.readEndArray()
        return list
    }

    inline fun <reified C : Any> decodeSet(reader: BsonReader, decoderContext: DecoderContext): Set<C> {
        reader.readStartArray()

        val set = mutableSetOf<C>()
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            set.add(decodeClass(reader, decoderContext))
        }

        reader.readEndArray()
        return set
    }

    inline fun <reified C : Any> decodeArray(reader: BsonReader, decoderContext: DecoderContext): Array<C> {
        return decodeList<C>(reader, decoderContext).toTypedArray()
    }

    fun <C> decodeList(decoder: Decoder<C>, reader: BsonReader, decoderContext: DecoderContext): List<C> {
        reader.readStartArray()

        val list = mutableListOf<C>()
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(decodeClass(decoder, reader, decoderContext))
        }

        reader.readEndArray()
        return list
    }

    fun <C> decodeSet(decoder: Decoder<C>, reader: BsonReader, decoderContext: DecoderContext): Set<C> {
        reader.readStartArray()

        val set = mutableSetOf<C>()
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            set.add(decodeClass(decoder, reader, decoderContext))
        }

        reader.readEndArray()
        return set
    }

    inline fun <reified C : Any> decodeArray(
        decoder: Decoder<C>,
        reader: BsonReader,
        decoderContext: DecoderContext
    ): Array<C> {
        return decodeList(decoder, reader, decoderContext).toTypedArray()
    }
}