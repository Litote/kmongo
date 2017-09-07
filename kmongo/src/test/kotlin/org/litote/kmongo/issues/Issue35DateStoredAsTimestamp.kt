package org.litote.kmongo.issues

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.junit.Test
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.issues.Issue35DateStoredAsTimestamp.TestWrapper
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.test.assertEquals

/**
 *
 */
class Issue35DateStoredAsTimestamp : KMongoBaseTest<TestWrapper>() {

    data class TestData(val l: Long = 0, @JsonProperty("r") val d: LocalDateTime)

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "type")
    @JsonSubTypes(
            JsonSubTypes.Type(value = Impl::class, name = "test")
    )
    abstract class Abstract(val type: String)

    data class Impl(val any: AnyValueWrapper) : Abstract("test")

    data class TestWrapper(val w: Abstract)


    @JsonDeserialize(using = AnyValueDeserializer::class)
    data class AnyValueWrapper(val klass: Class<*>, val value: Any?)

    internal class AnyValueDeserializer : JsonDeserializer<AnyValueWrapper>() {

        fun JsonParser.fieldNameWithValueReady(): String? {
            if (currentToken == JsonToken.END_OBJECT) {
                return null
            }
            val firstToken = nextToken()
            if (firstToken == JsonToken.END_OBJECT) {
                return null
            }
            val fieldName = currentName
            nextToken()
            return fieldName
        }

        fun JsonParser.checkEndToken() {
            if (currentToken != JsonToken.END_OBJECT) {
                nextToken()
                checkEndToken()
            }
        }

        override fun deserialize(jp: JsonParser, context: DeserializationContext): AnyValueWrapper? {
            var fieldName = jp.fieldNameWithValueReady()
            if (fieldName != null) {
                val classValue = jp.readValueAs(Class::class.java)!!
                fieldName = jp.fieldNameWithValueReady()
                if (fieldName != null) {
                    val value = jp.readValueAs(classValue)!!
                    jp.checkEndToken()
                    return AnyValueWrapper(classValue, value)
                } else {
                    return AnyValueWrapper(classValue, null)
                }
            }
            return null
        }

    }


    override fun getDefaultCollectionClass(): KClass<TestWrapper> {
        return TestWrapper::class
    }

    @Test
    fun testSaveAndLoad() {
        val d = LocalDateTime.now()
        col.insertOne(TestWrapper(Impl(AnyValueWrapper(TestData::class.java, TestData(1222L, d)))))
        assertEquals(d, ((col.findOne("{}")!!.w as Impl).any.value as TestData).d)
    }


}