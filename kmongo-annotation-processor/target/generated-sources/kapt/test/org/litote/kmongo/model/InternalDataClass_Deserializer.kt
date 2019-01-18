package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Long
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class InternalDataClass_Deserializer : JsonDeserializer<InternalDataClass>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(InternalDataClass::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): InternalDataClass {
        with(p) {
            var _l_: Long? = null
            var _l_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "l" -> {
                            _l_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.longValue;
                            _l_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
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
