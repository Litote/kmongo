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
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass

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

private class PathMap : LinkedHashMap<String, Any>()

/**
 * This method is not part of the public API and may be removed or changed at any time.
 */
fun singleProjectionCodecRegistry(
    property: String,
    propertyClass: KClass<*>,
    baseRegistry: CodecRegistry
): CodecRegistry {
    val pathMap = getMapPath(property to propertyClass)
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
                    val result: MutableMap<String, Any> = mutableMapOf()
                    reader.decode(
                        pathMap,
                        "",
                        decoderContext,
                        baseRegistry,
                        result
                    )
                    return SingleProjection(result[property])
                }
            }
        ),
        baseRegistry
    )
}

/**
 * This method is not part of the public API and may be removed or changed at any time.
 */
fun pairProjectionCodecRegistry(
    property1: String,
    property1Class: KClass<*>,
    property2: String,
    property2Class: KClass<*>,
    baseRegistry: CodecRegistry
): CodecRegistry {
    val pathMap = getMapPath(property1 to property1Class, property2 to property2Class)
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
                    val result: MutableMap<String, Any> = mutableMapOf()
                    reader.decode(
                        pathMap,
                        "",
                        decoderContext,
                        baseRegistry,
                        result
                    )
                    return PairProjection(result[property1], result[property2])
                }
            }
        ),
        baseRegistry
    )
}

/**
 * This method is not part of the public API and may be removed or changed at any time.
 */
fun tripleProjectionCodecRegistry(
    property1: String,
    property1Class: KClass<*>,
    property2: String,
    property2Class: KClass<*>,
    property3: String,
    property3Class: KClass<*>,
    baseRegistry: CodecRegistry
): CodecRegistry {
    val pathMap = getMapPath(property1 to property1Class, property2 to property2Class, property3 to property3Class)
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
                    val result: MutableMap<String, Any> = mutableMapOf()
                    reader.decode(
                        pathMap,
                        "",
                        decoderContext,
                        baseRegistry,
                        result
                    )
                    return TripleProjection(result[property1], result[property2], result[property3])
                }
            }
        ),
        baseRegistry
    )
}

private fun getMapPath(vararg paths: Pair<String, KClass<*>>): PathMap {
    val pathMap = PathMap()
    paths.forEach { (p, k) ->
        val split = p.split(".")
        var m = pathMap
        for (s in split.take(split.size - 1)) {
            @Suppress("UNCHECKED_CAST")
            m = m.getOrPut(s) { PathMap() } as PathMap
        }
        m[split.last()] = k

    }
    return pathMap
}

private fun BsonReader.decode(
    pathMap: PathMap,
    current: String,
    decoderContext: DecoderContext,
    registry: CodecRegistry,
    result: MutableMap<String, Any>
) {
    readStartDocument()
    while (readBsonType() != BsonType.END_OF_DOCUMENT) {
        val n = readName()
        val value = pathMap[n]
        val newPath = if (current.isEmpty()) n else "$current.$n"
        if (value is PathMap) {
            decode(value, newPath, decoderContext, registry, result)
        } else {
            result[newPath] = registry.get((value as KClass<*>).java).decode(this, decoderContext)
        }
    }

    readEndDocument()
}