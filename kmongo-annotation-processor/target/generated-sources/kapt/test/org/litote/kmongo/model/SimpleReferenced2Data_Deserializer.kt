package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Double
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class SimpleReferenced2Data_Deserializer : JsonDeserializer<SimpleReferenced2Data>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(SimpleReferenced2Data::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SimpleReferenced2Data {
        with(p) {
            var _price_: Double? = null
            var _price_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "price" -> {
                            _price_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _price_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
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
