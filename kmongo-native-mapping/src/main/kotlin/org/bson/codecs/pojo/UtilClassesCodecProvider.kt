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

package org.bson.codecs.pojo

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.PatternCodec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdTransformer
import org.litote.kmongo.id.StringId
import org.litote.kmongo.id.WrappedObjectId
import org.litote.kmongo.projection
import java.util.Locale
import kotlin.reflect.KProperty

/**
 *
 */
internal object UtilClassesCodecProvider : CodecProvider {

    private object LocaleCodec : Codec<Locale> {

        override fun encode(writer: BsonWriter, value: Locale, encoderContext: EncoderContext) {
            writer.writeString(value.toLanguageTag())
        }

        override fun getEncoderClass(): Class<Locale> = Locale::class.java

        override fun decode(reader: BsonReader, decoderContext: DecoderContext): Locale? =
            Locale.forLanguageTag(reader.readString())
    }

    private object IdCodec : Codec<Id<*>> {

        override fun encode(writer: BsonWriter, value: Id<*>, encoderContext: EncoderContext) {
            val wrapped = IdTransformer.unwrapId(value)
            when (wrapped) {
                is String -> writer.writeString(wrapped)
                is ObjectId -> writer.writeObjectId(wrapped)
                else -> error("unsupported id type $value")
            }
        }

        override fun getEncoderClass(): Class<Id<*>> = Id::class.java

        override fun decode(reader: BsonReader, decoderContext: DecoderContext): Id<*>? {
            return IdTransformer.wrapId(
                when (reader.currentBsonType) {
                    BsonType.STRING -> reader.readString()
                    BsonType.OBJECT_ID -> reader.readObjectId()
                    else -> error("unsupported id token ${reader.currentBsonType}")
                }
            )
        }

    }

    private object KPropertyCodec : Codec<KProperty<*>> {
        override fun getEncoderClass(): Class<KProperty<*>> = KProperty::class.java

        override fun encode(writer: BsonWriter, value: KProperty<*>, encoderContext: EncoderContext?) {
            writer.writeString(value.projection)
        }

        override fun decode(reader: BsonReader, decoderContext: DecoderContext): KProperty<*> {
            error("decoding property is not supported")
        }
    }

    private object RegexCodec : Codec<Regex> {

        private val patternCodec = PatternCodec()

        override fun getEncoderClass(): Class<Regex> = Regex::class.java

        override fun encode(writer: BsonWriter, value: Regex, encoderContext: EncoderContext) {
            patternCodec.encode(writer, value.toPattern(), encoderContext)
        }

        override fun decode(reader: BsonReader, decoderContext: DecoderContext): Regex {
            return patternCodec.decode(reader, decoderContext).toRegex()
        }
    }

    private val codecsMap: Map<Class<*>, Codec<*>> = listOf(LocaleCodec, IdCodec)
        .map { it.encoderClass to it }
        .toMap() +
            mapOf(
                StringId::class.java to IdCodec,
                WrappedObjectId::class.java to IdCodec,
                Regex::class.java to RegexCodec
            )

    override fun <T : Any?> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
        @Suppress("UNCHECKED_CAST")
        return if (KProperty::class.java.isAssignableFrom(clazz)) {
            KPropertyCodec
        } else {
            codecsMap[clazz]
        } as Codec<T>?
    }
}