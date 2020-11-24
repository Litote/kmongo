/*
 * Copyright (C) 2016/2020 Litote
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
package org.litote.kmongo.issues

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import com.github.jershell.kbson.Configuration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.EmptySerializersModule
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.junit.Test
import kotlin.test.assertEquals

@Serializable
sealed class Message

@Serializable
data class StringMessage(val message: String) : Message()

@Serializable
data class IntMessage(val int: Int) : Message()

@Serializable
object ObjectMessage : Message() {
    val message: String = "Message"
}

@Serializable
data class ComplexObject(
    val a: Int,
    val m: Message,
    val c: String
)

class ObjectInPolymorphicNotWorking {
    @Test
    fun `serialize and deserialize object in polymorphic`() {
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        val encoder = BsonEncoder(writer, EmptySerializersModule, Configuration())
        Message.serializer().serialize(encoder, ObjectMessage)

        val expected = BsonDocument.parse("""{"___type":"ObjectMessage"}""")
        assertEquals(expected, document)

        val reader = BsonDocumentReader(document)
        val decoder = BsonFlexibleDecoder(reader, EmptySerializersModule, Configuration())
        val parsed = Message.serializer().deserialize(decoder)
        assertEquals(ObjectMessage, parsed)
    }

    @Test
    fun `serialize and deserialize class in polymorphic`(){
        val message = StringMessage("msg")

        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        val encoder = BsonEncoder(writer, EmptySerializersModule, Configuration())
        Message.serializer().serialize(encoder, message)
        writer.flush()

        val expected = BsonDocument.parse("""{"___type":"StringMessage", "message":"msg"}""")
        assertEquals(expected, document)

        val reader = BsonDocumentReader(document)
        val decoder = BsonFlexibleDecoder(reader, EmptySerializersModule, Configuration())
        val parsed = Message.serializer().deserialize(decoder)
        assertEquals(message, parsed)
    }

    @Test
    fun `serialize in complex object`(){
        val complex = ComplexObject(3, ObjectMessage, "msg")
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        val encoder = BsonEncoder(writer, EmptySerializersModule, Configuration())
        ComplexObject.serializer().serialize(encoder, complex)
        writer.flush()

        val expected = BsonDocument.parse("""{"a":3,"m":{"___type":"ObjectMessage"},"c":"msg"}""")
        assertEquals(expected, document)

        val reader = BsonDocumentReader(document)
        val decoder = BsonFlexibleDecoder(reader, EmptySerializersModule, Configuration())
        val parsed = ComplexObject.serializer().deserialize(decoder)
        assertEquals(complex, parsed)
    }
}