package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Long
import org.litote.jackson.JacksonModuleServiceLoader

internal class InternalDataClass_Deserializer : StdDeserializer<InternalDataClass>(InternalDataClass::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(InternalDataClass::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): InternalDataClass {
        with(p) {
        var l: Long? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "l" -> l = p.readValueAs(Long::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return InternalDataClass(l!!) }
    }
    companion object
}
