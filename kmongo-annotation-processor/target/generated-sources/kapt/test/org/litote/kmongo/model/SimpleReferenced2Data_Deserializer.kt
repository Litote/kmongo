package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Double
import org.litote.jackson.JacksonModuleServiceLoader

class SimpleReferenced2Data_Deserializer : StdDeserializer<SimpleReferenced2Data>(SimpleReferenced2Data::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(SimpleReferenced2Data::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SimpleReferenced2Data {
        with(p) {
        var price: Double? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "price" -> price = p.readValueAs(Double::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return SimpleReferenced2Data(price!!) }
    }
    companion object
}
