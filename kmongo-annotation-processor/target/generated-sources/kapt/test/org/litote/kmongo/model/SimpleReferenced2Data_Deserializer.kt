package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Double
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class SimpleReferenced2Data_Deserializer :
        StdDeserializer<SimpleReferenced2Data>(SimpleReferenced2Data::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(SimpleReferenced2Data::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SimpleReferenced2Data {
        with(p) {
            var _price_: Double? = null
            var _price_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "price" -> {
                            _price_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Double::class.java);
                            _price_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_price_set)
                    SimpleReferenced2Data(price = _price_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_price_set)
                    map[parameters.getValue("price")] = _price_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<SimpleReferenced2Data> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                SimpleReferenced2Data::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("price" to
                primaryConstructor.findParameterByName("price")!!) }
    }
}
