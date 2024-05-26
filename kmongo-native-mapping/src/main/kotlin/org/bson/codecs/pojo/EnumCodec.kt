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
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

/**
 *
 */
internal class EnumCodec<T : Enum<T>>(private val clazz: Class<T>) : Codec<T> {

    companion object {
        @Suppress("UPPER_BOUND_VIOLATED", "UNCHECKED_CAST")
        fun newCodec(clazz: Class<Any>) : EnumCodec<*> = EnumCodec(clazz as Class<out Enum<*>>)
    }

    override fun encode(writer: BsonWriter, value: T, encoderContext: EncoderContext) {
        writer.writeString(value.name)
    }

    override fun getEncoderClass(): Class<T> {
        return clazz
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): T {
        return java.lang.Enum.valueOf(clazz, reader.readString()) as T
    }

}