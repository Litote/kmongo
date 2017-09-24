/*
 * Copyright (C) 2017 Litote
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
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import java.util.Locale

/**
 *
 */
internal object UtilClassesCodecProvider : CodecProvider {

    private object LocaleCodec : Codec<Locale> {

        override fun encode(writer: BsonWriter, value: Locale, encoderContext: EncoderContext) {
            writer.writeString(value.toLanguageTag())
        }

        override fun getEncoderClass(): Class<Locale> = Locale::class.java

        override fun decode(reader: BsonReader, decoderContext: DecoderContext): Locale?
                = Locale.forLanguageTag(reader.readString())
    }

    private val codecsMap: Map<Class<*>, Codec<*>> = listOf(LocaleCodec)
            .map { it.encoderClass to it }
            .toMap()

    override fun <T : Any?> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
        @Suppress("UNCHECKED_CAST")
        return codecsMap[clazz] as Codec<T>?
    }
}