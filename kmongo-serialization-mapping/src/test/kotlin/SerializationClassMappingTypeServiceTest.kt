/*
 * Copyright (C) 2017/2019 Litote
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

import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.Document
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import kotlin.test.assertEquals

/**
 *
 */
class SerializationClassMappingTypeServiceTest {

    @kotlinx.serialization.ImplicitReflectionSerializer
    @org.junit.Test
    fun `encode and decode document`() {
        val doc = Document()
        doc["a"] = "b"
        val codec = SerializationClassMappingTypeService().coreCodecRegistry().get(Document::class.java)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, doc, EncoderContext.builder().build())

        println(document)
        val newFriend = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(doc, newFriend)
    }
}