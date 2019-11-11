package org.litote.kmongo.serialization

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.modules.SerializersModule
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.Id
import org.litote.kmongo.model.Friend
import org.litote.kmongo.newId
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse

/**
 *
 */
class SerializationCodecTest {

    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode Friend`() {
        val friend = Friend("Joe", "22 Wall Street Avenue", _id = ObjectId())
        val codec = SerializationCodec(Friend::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, friend, EncoderContext.builder().build())

        val newFriend = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(friend, newFriend)
    }

    @Serializable
    data class TestWithId(@ContextualSerialization val id: Id<TestWithId> = newId())

    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode Ids`() {
        val id = TestWithId()
        val codec = SerializationCodec(TestWithId::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, id, EncoderContext.builder().build())

        val newFriend = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(id, newFriend)
    }

    @Serializable
    data class TestWithSetOfIds(val list: Set<@ContextualSerialization Id<TestWithId>> = setOf(newId()))

    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode list of ids`() {
        val idList = TestWithSetOfIds()
        val codec = SerializationCodec(TestWithSetOfIds::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, idList, EncoderContext.builder().build())

        val newFriend = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(idList, newFriend)
    }

    @Serializable
    data class TestWithMapOfIds(
        val map: Map<@ContextualSerialization Id<TestWithId>, String>
        = mapOf(newId<TestWithId>() to "a")
    )

    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode map of ids`() {
        val idList = TestWithMapOfIds()
        val codec = SerializationCodec(TestWithMapOfIds::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, idList, EncoderContext.builder().build())

        val newFriend = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(idList, newFriend)
    }

    data class Custom(val s: String, val b: Boolean = false)

    @Serializer(forClass = Custom::class)
    class CustomSerializer : KSerializer<Custom> {

        object CustomClassDesc : SerialClassDescImpl("Custom") {
            init {
                addElement("s")
                pushDescriptor(StringDescriptor)
            }
        }

        override fun deserialize(decoder: Decoder): Custom {
            decoder as CompositeDecoder
            decoder.beginStructure(CustomClassDesc)
            val c = Custom(decoder.decodeStringElement(CustomClassDesc, 0))
            decoder.endStructure(CustomClassDesc)
            return c
        }

        override fun serialize(encoder: Encoder, obj: Custom) {
            encoder as CompositeEncoder
            encoder.beginStructure(CustomClassDesc)
            encoder.encodeStringElement(CustomClassDesc, 0, obj.s)
            encoder.endStructure(CustomClassDesc)
        }
    }

    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode with custom serializer`() {
        registerSerializer(CustomSerializer())
        val c = Custom("a", true)
        val codec = SerializationCodec(Custom::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, c, EncoderContext.builder().build())

        val newC = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(c.s, newC.s)
        assertFalse(newC.b)
    }

    interface Message

    @Serializable
    data class StringMessage(val message: String) : Message

    @Serializable
    data class IntMessage(val number: Int) : Message

    @Serializable
    data class Container(val m: Message)

    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode with custom polymorphic serializer`() {
        registerModule(
            SerializersModule {
                polymorphic(Message::class) {
                    StringMessage::class with StringMessage.serializer()
                    IntMessage::class with IntMessage.serializer()
                }
            })
        val c = Container(StringMessage("a"))
        val codec = SerializationCodec(Container::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, c, EncoderContext.builder().build())

        val newC = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(c, newC)
    }

    interface Message2

    @Serializable
    data class StringMessage2(val message: String) : Message2

    @Serializable
    data class IntMessage2(val number: Int) : Message2

    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode directly custom polymorphic serializer`() {
        registerModule(
            SerializersModule {
                polymorphic(Message2::class) {
                    StringMessage2::class with StringMessage2.serializer()
                    IntMessage2::class with IntMessage2.serializer()
                }
            })
        val c = StringMessage2("a")
        val codec = SerializationCodec(Message2::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, c, EncoderContext.builder().build())

        val newC = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(c, newC)
    }

    @Serializable
    data class SerializableClass(val s: String)

    data class NotSerializableClass(val s: String)

    @ImplicitReflectionSerializer
    @Test
    fun `encoding a not serializable class throws a SerializationException`() {
        val t = assertFails {
            val c = NotSerializableClass("a")
            val codec = SerializationCodec(NotSerializableClass::class)
            val document = BsonDocument()
            val writer = BsonDocumentWriter(document)
            codec.encode(writer, c, EncoderContext.builder().build())
        }
        assertEquals(
            "Can't locate argument-less serializer for class org.litote.kmongo.serialization.SerializationCodecTest\$NotSerializableClass. For generic classes, such as lists, please provide serializer explicitly.",
            t.message
        )
    }

    @ImplicitReflectionSerializer
    @Test
    fun `decoding a not serializable class throws a SerializationException`() {
        val t = assertFails {
            val c = SerializableClass("a")
            val codec = SerializationCodec(SerializableClass::class)
            val document = BsonDocument()
            val writer = BsonDocumentWriter(document)
            codec.encode(writer, c, EncoderContext.builder().build())

            val codec2 = SerializationCodec(NotSerializableClass::class)
            codec2.decode(BsonDocumentReader(document), DecoderContext.builder().build())
        }
        assertEquals(
            "Can't locate argument-less serializer for class org.litote.kmongo.serialization.SerializationCodecTest\$NotSerializableClass. For generic classes, such as lists, please provide serializer explicitly.",
            t.message
        )
    }
}