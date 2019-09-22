package org.litote.kmongo.serialization

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.Document
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.types.ObjectId
import org.junit.Ignore
import org.junit.Test
import org.litote.kmongo.Id
import org.litote.kmongo.model.Friend
import org.litote.kmongo.newId
import kotlin.test.assertEquals

/**
 *
 */
class SerializationCodecTest {

    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode Friend`() {
        val friend = Friend("Joe","22 Wall Street Avenue", _id = ObjectId())
        val codec = SerializationCodec(Friend::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, friend, EncoderContext.builder().build())

        println(document)
        val newFriend = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(friend, newFriend)
    }

    @Serializable
    data class TestWithId(@ContextualSerialization val id : Id<TestWithId> = newId())

    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode Ids`() {
        val id = TestWithId()
        val codec = SerializationCodec(TestWithId::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, id, EncoderContext.builder().build())

        println(document)
        val newFriend = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(id, newFriend)
    }

    @Serializable
    data class TestWithSetOfIds(val list : Set<@ContextualSerialization Id<TestWithId>> = setOf(newId()))

    @Ignore
    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode list of ids`() {
        val idList = TestWithSetOfIds()
        val codec = SerializationCodec(TestWithSetOfIds::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, idList, EncoderContext.builder().build())

        println(document)
        val newFriend = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(idList, newFriend)
    }

    @Serializable
    data class TestWithMapOfIds(val map : Map<@ContextualSerialization Id<TestWithId>, String>
                                    = mapOf(newId<TestWithId>() to "a") )

    @Ignore
    @ImplicitReflectionSerializer
    @Test
    fun `encode and decode map of ids`() {
        val idList = TestWithMapOfIds()
        val codec = SerializationCodec(TestWithMapOfIds::class)
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        codec.encode(writer, idList, EncoderContext.builder().build())

        println(document)
        val newFriend = codec.decode(BsonDocumentReader(document), DecoderContext.builder().build())

        assertEquals(idList, newFriend)
    }
}