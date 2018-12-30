package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Long
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class InternalDataClass_Deserializer :
        StdDeserializer<InternalDataClass>(InternalDataClass::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(InternalDataClass::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): InternalDataClass {
        with(p) {
            var _l_: Long? = null
            var _l_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "l" -> {
                            _l_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Long::class.java);
                            _l_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_l_set)
                    InternalDataClass(l = _l_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_l_set)
                    map[parameters.getValue("l")] = _l_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<InternalDataClass> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                InternalDataClass::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("l" to primaryConstructor.findParameterByName("l")!!) }
    }
}
