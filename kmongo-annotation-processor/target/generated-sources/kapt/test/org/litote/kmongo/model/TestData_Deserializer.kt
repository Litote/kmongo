package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Date
import java.util.Locale
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Float
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id
import org.litote.kmongo.model.other.SimpleReferencedData

class TestData_Deserializer : StdDeserializer<TestData>(TestData::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(TestData::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestData {
        with(p) {
        var set: Set<SimpleReferencedData>? = null
        var list: List<List<Boolean>>? = null
        var name: String? = null
        var date: Date? = null
        var referenced: SimpleReferencedData? = null
        var map: Map<Id<Locale>, Set<String>>? = null
        var nullableFloat: Float? = null
        var nullableBoolean: Boolean? = null
        var privateData: String? = null
        var id: Id<*>? = null
        var byteArray: ByteArray? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "set" -> set = p.readValueAs(set_reference)
        "list" -> list = p.readValueAs(list_reference)
        "name" -> name = p.text
        "date" -> date = p.readValueAs(Date::class.java)
        "referenced" -> referenced = p.readValueAs(SimpleReferencedData::class.java)
        "map" -> map = p.readValueAs(map_reference)
        "nullableFloat" -> nullableFloat = p.floatValue
        "nullableBoolean" -> nullableBoolean = p.booleanValue
        "privateData" -> privateData = p.text
        "id" -> id = p.readValueAs(id_reference)
        "byteArray" -> byteArray = p.readValueAs(byteArray_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return TestData(set!!, list!!, name, date, referenced, map!!, nullableFloat, nullableBoolean, privateData!!, id!!, byteArray)
                }
    }
    companion object {
        val set_reference: TypeReference<Set<SimpleReferencedData>> =
                object : TypeReference<Set<SimpleReferencedData>>() {}

        val list_reference: TypeReference<List<List<Boolean>>> =
                object : TypeReference<List<List<Boolean>>>() {}

        val map_reference: TypeReference<Map<Id<Locale>, Set<String>>> =
                object : TypeReference<Map<Id<Locale>, Set<String>>>() {}

        val id_reference: TypeReference<Id<*>> = object : TypeReference<Id<*>>() {}

        val byteArray_reference: TypeReference<ByteArray> = object : TypeReference<ByteArray>() {}
    }
}
