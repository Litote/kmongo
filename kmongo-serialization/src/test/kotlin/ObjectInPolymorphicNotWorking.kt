import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import com.github.jershell.kbson.Configuration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptyModule
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

class ObjectInPolymorphicNotWorking {
    @Test
    fun `serialize and deserialize object in polymorphic`() {
        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        val encoder = BsonEncoder(writer, EmptyModule, Configuration())
        Message.serializer().serialize(encoder, ObjectMessage)
        val expected = BsonDocument.parse("""{"___type":"ObjectMessage"}""")
        assertEquals(expected, document)

        val reader = BsonDocumentReader(document)
        val decoder = BsonFlexibleDecoder(reader, EmptyModule, Configuration())
        val parsed = Message.serializer().deserialize(decoder)
        assertEquals(ObjectMessage, parsed)
    }

    @Test
    fun `serialize and deserialize class in polymorphic`(){
        val message = StringMessage("msg")

        val document = BsonDocument()
        val writer = BsonDocumentWriter(document)
        val encoder = BsonEncoder(writer, EmptyModule, Configuration())
        Message.serializer().serialize(encoder, message)
        val expected = BsonDocument.parse("""{"___type":"StringMessage", "message":"msg"}""")
        assertEquals(expected, document)

        val reader = BsonDocumentReader(document)
        val decoder = BsonFlexibleDecoder(reader, EmptyModule, Configuration())
        val parsed = Message.serializer().deserialize(decoder)
        assertEquals(message, parsed)
    }
}