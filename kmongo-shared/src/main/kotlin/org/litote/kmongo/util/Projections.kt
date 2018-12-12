/*
 * Copyright (C) 2017/2018 Litote
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
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

/**
 * Single projection (one field only).
 */
class SingleProjection<F>(val field: F?)

/**
 * Pair projection (two fields).
 */
class PairProjection<F1, F2>(val field1: F1?, val field2: F2?)

/**
 * Triple projection (three fields).
 */
class TripleProjection<F1, F2, F3>(val field1: F1?, val field2: F2?, val field3: F3?)

/**
 * This method is not part of the public API and may be removed or changed at any time.
 */
inline fun <reified T> singleProjectionCodecRegistry(
    baseRegistry: CodecRegistry
): CodecRegistry {
    return CodecRegistries.fromRegistries(
        CodecRegistries.fromCodecs(
            object : Codec<SingleProjection<*>> {
                override fun getEncoderClass(): Class<SingleProjection<*>> = SingleProjection::class.java

                override fun encode(
                    writer: BsonWriter,
                    value: SingleProjection<*>,
                    encoderContext: EncoderContext
                ) {
                    error("not supported")
                }

                override fun decode(reader: BsonReader, decoderContext: DecoderContext): SingleProjection<*> {
                    reader.readStartDocument()
                    reader.readName()
                    val codec = baseRegistry.get(T::class.java)
                    val r = codec.decode(reader, decoderContext)
                    reader.readEndDocument()
                    return SingleProjection(r)
                }
            }
        ),
        baseRegistry
    )
}

/**
 * This method is not part of the public API and may be removed or changed at any time.
 */
inline fun <reified T1, reified T2> pairProjectionCodecRegistry(
    property1: String,
    property2: String,
    baseRegistry: CodecRegistry
): CodecRegistry {
    return CodecRegistries.fromRegistries(
        CodecRegistries.fromCodecs(
            object : Codec<PairProjection<*, *>> {
                override fun getEncoderClass(): Class<PairProjection<*, *>> = PairProjection::class.java

                override fun encode(
                    writer: BsonWriter,
                    value: PairProjection<*, *>,
                    encoderContext: EncoderContext
                ) {
                    error("not supported")
                }

                override fun decode(reader: BsonReader, decoderContext: DecoderContext): PairProjection<*, *> {
                    reader.readStartDocument()
                    var r1: T1? = null
                    var r2: T2? = null
                    try {
                        while (r1 == null || r2 == null)
                            when (reader.readName()) {
                                property1 -> {
                                    val codec1 = baseRegistry.get(T1::class.java)
                                    r1 = codec1.decode(reader, decoderContext)
                                }
                                property2 -> {
                                    val codec2 = baseRegistry.get(T2::class.java)
                                    r2 = codec2.decode(reader, decoderContext)
                                }
                            }
                    } catch (e: Exception) {
                        //ignore
                    }
                    reader.readEndDocument()
                    return PairProjection(r1, r2)
                }
            }
        ),
        baseRegistry
    )
}

/**
 * This method is not part of the public API and may be removed or changed at any time.
 */
inline fun <reified T1, reified T2, reified T3> tripleProjectionCodecRegistry(
    property1: String,
    property2: String,
    property3: String,
    baseRegistry: CodecRegistry
): CodecRegistry {

    return CodecRegistries.fromRegistries(
        CodecRegistries.fromCodecs(
            object : Codec<TripleProjection<*, *, *>> {
                override fun getEncoderClass(): Class<TripleProjection<*, *, *>> = TripleProjection::class.java

                override fun encode(
                    writer: BsonWriter,
                    value: TripleProjection<*, *, *>,
                    encoderContext: EncoderContext
                ) {
                    error("not supported")
                }

                override fun decode(reader: BsonReader, decoderContext: DecoderContext): TripleProjection<*, *, *> {
                    reader.readStartDocument()
                    var r1: T1? = null
                    var r2: T2? = null
                    var r3: T3? = null
                    try {
                        while (r1 == null || r2 == null || r3 == null)
                            when (reader.readName()) {
                                property1 -> {
                                    val codec1 = baseRegistry.get(T1::class.java)
                                    r1 = codec1.decode(reader, decoderContext)
                                }
                                property2 -> {
                                    val codec2 = baseRegistry.get(T2::class.java)
                                    r2 = codec2.decode(reader, decoderContext)
                                }
                                property3 -> {
                                    val codec3 = baseRegistry.get(T3::class.java)
                                    r3 = codec3.decode(reader, decoderContext)
                                }
                            }
                    } catch (e: Exception) {
                        //ignore
                    }
                    reader.readEndDocument()
                    return TripleProjection(r1, r2, r3)
                }
            }
        ),
        baseRegistry
    )
}