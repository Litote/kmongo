package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Date
import java.util.Locale
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Float
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id
import org.litote.kmongo.model.other.SimpleReferencedData

internal class TestData_Deserializer : JsonDeserializer<TestData>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(TestData::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestData {
        with(p) {
            var _set_: MutableSet<SimpleReferencedData>? = null
            var _set_set : Boolean = false
            var _list_: MutableList<List<Boolean>>? = null
            var _list_set : Boolean = false
            var _name_: String? = null
            var _name_set : Boolean = false
            var _date_: Date? = null
            var _date_set : Boolean = false
            var _referenced_: SimpleReferencedData? = null
            var _referenced_set : Boolean = false
            var _map_: MutableMap<Id<Locale>, Set<String>>? = null
            var _map_set : Boolean = false
            var _map2_: MutableMap<Locale, SimpleReferenced2Data>? = null
            var _map2_set : Boolean = false
            var _nullableFloat_: Float? = null
            var _nullableFloat_set : Boolean = false
            var _nullableBoolean_: Boolean? = null
            var _nullableBoolean_set : Boolean = false
            var _privateData_: String? = null
            var _privateData_set : Boolean = false
            var _id_: Id<*>? = null
            var _id_set : Boolean = false
            var _byteArray_: ByteArray? = null
            var _byteArray_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "set" -> {
                            _set_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_set__reference);
                            _set_set = true
                            }
                    "list" -> {
                            _list_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_list__reference);
                            _list_set = true
                            }
                    "name" -> {
                            _name_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _name_set = true
                            }
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Date::class.java);
                            _date_set = true
                            }
                    "referenced" -> {
                            _referenced_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(SimpleReferencedData::class.java);
                            _referenced_set = true
                            }
                    "map" -> {
                            _map_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_map__reference);
                            _map_set = true
                            }
                    "map2" -> {
                            _map2_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_map2__reference);
                            _map2_set = true
                            }
                    "nullableFloat" -> {
                            _nullableFloat_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.floatValue;
                            _nullableFloat_set = true
                            }
                    "nullableBoolean" -> {
                            _nullableBoolean_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _nullableBoolean_set = true
                            }
                    "privateData" -> {
                            _privateData_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _privateData_set = true
                            }
                    "id" -> {
                            _id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_id__reference);
                            _id_set = true
                            }
                    "byteArray" -> {
                            _byteArray_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_byteArray__reference);
                            _byteArray_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_set_set && _list_set && _name_set && _date_set && _referenced_set && _map_set
                    && _map2_set && _nullableFloat_set && _nullableBoolean_set && _privateData_set
                    && _id_set && _byteArray_set)
                    TestData(set = _set_!!, list = _list_!!, name = _name_, date = _date_,
                            referenced = _referenced_, map = _map_!!, map2 = _map2_!!, nullableFloat
                            = _nullableFloat_, nullableBoolean = _nullableBoolean_, privateData =
                            _privateData_!!, id = _id_!!, byteArray = _byteArray_)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_set_set)
                    map[parameters.getValue("set")] = _set_
                    if(_list_set)
                    map[parameters.getValue("list")] = _list_
                    if(_name_set)
                    map[parameters.getValue("name")] = _name_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_
                    if(_referenced_set)
                    map[parameters.getValue("referenced")] = _referenced_
                    if(_map_set)
                    map[parameters.getValue("map")] = _map_
                    if(_map2_set)
                    map[parameters.getValue("map2")] = _map2_
                    if(_nullableFloat_set)
                    map[parameters.getValue("nullableFloat")] = _nullableFloat_
                    if(_nullableBoolean_set)
                    map[parameters.getValue("nullableBoolean")] = _nullableBoolean_
                    if(_privateData_set)
                    map[parameters.getValue("privateData")] = _privateData_
                    if(_id_set)
                    map[parameters.getValue("id")] = _id_
                    if(_byteArray_set)
                    map[parameters.getValue("byteArray")] = _byteArray_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<TestData> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { TestData::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("set" to primaryConstructor.findParameterByName("set")!!,
                "list" to primaryConstructor.findParameterByName("list")!!, "name" to
                primaryConstructor.findParameterByName("name")!!, "date" to
                primaryConstructor.findParameterByName("date")!!, "referenced" to
                primaryConstructor.findParameterByName("referenced")!!, "map" to
                primaryConstructor.findParameterByName("map")!!, "map2" to
                primaryConstructor.findParameterByName("map2")!!, "nullableFloat" to
                primaryConstructor.findParameterByName("nullableFloat")!!, "nullableBoolean" to
                primaryConstructor.findParameterByName("nullableBoolean")!!, "privateData" to
                primaryConstructor.findParameterByName("privateData")!!, "id" to
                primaryConstructor.findParameterByName("id")!!, "byteArray" to
                primaryConstructor.findParameterByName("byteArray")!!) }

        private val _set__reference: TypeReference<Set<SimpleReferencedData>> = object :
                TypeReference<Set<SimpleReferencedData>>() {}

        private val _list__reference: TypeReference<List<List<Boolean>>> = object :
                TypeReference<List<List<Boolean>>>() {}

        private val _map__reference: TypeReference<Map<Id<Locale>, Set<String>>> = object :
                TypeReference<Map<Id<Locale>, Set<String>>>() {}

        private val _map2__reference: TypeReference<Map<Locale, SimpleReferenced2Data>> = object :
                TypeReference<Map<Locale, SimpleReferenced2Data>>() {}

        private val _id__reference: TypeReference<Id<*>> = object : TypeReference<Id<*>>() {}

        private val _byteArray__reference: TypeReference<ByteArray> = object :
                TypeReference<ByteArray>() {}
    }
}
