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

package org.litote.kmongo.serialization

import com.github.jershell.kbson.Configuration
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecRegistry

/**
 *
 */
internal class SerializationCodecRegistry(private val configuration: Configuration) : CodecRegistry {

    override fun <T : Any> get(clazz: Class<T>): Codec<T> = SerializationCodec(clazz.kotlin, configuration)

    override fun <T : Any> get(clazz: Class<T>, codecRegistry: CodecRegistry): Codec<T> = get(clazz)
}