package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class TestData_Serializer : StdSerializer<TestData>(TestData::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(TestData::class.java, this)

    override fun serialize(
        value: TestData,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("set")
        val _set_ = value.set
        serializers.defaultSerializeValue(_set_, gen)
        gen.writeFieldName("list")
        val _list_ = value.list
        serializers.defaultSerializeValue(_list_, gen)
        gen.writeFieldName("name")
        val _name_ = value.name
        if(_name_ == null) { gen.writeNull() } else {gen.writeString(_name_)}
        gen.writeFieldName("date")
        val _date_ = value.date
        if(_date_ == null) { gen.writeNull() } else {serializers.defaultSerializeValue(_date_, gen)}
        gen.writeFieldName("referenced")
        val _referenced_ = value.referenced
        if(_referenced_ == null) { gen.writeNull() } else
                {serializers.defaultSerializeValue(_referenced_, gen)}
        gen.writeFieldName("map")
        val _map_ = value.map
        serializers.defaultSerializeValue(_map_, gen)
        gen.writeFieldName("map2")
        val _map2_ = value.map2
        serializers.defaultSerializeValue(_map2_, gen)
        gen.writeFieldName("nullableFloat")
        val _nullableFloat_ = value.nullableFloat
        if(_nullableFloat_ == null) { gen.writeNull() } else {gen.writeNumber(_nullableFloat_)}
        gen.writeFieldName("nullableBoolean")
        val _nullableBoolean_ = value.nullableBoolean
        if(_nullableBoolean_ == null) { gen.writeNull() } else {gen.writeBoolean(_nullableBoolean_)}
        gen.writeFieldName("privateData")
        val _privateData_ =
                org.litote.kreflect.findPropertyValue<org.litote.kmongo.model.TestData,kotlin.String>(value,
                "privateData")
        gen.writeString(_privateData_)
        gen.writeFieldName("id")
        val _id_ = value.id
        serializers.defaultSerializeValue(_id_, gen)
        gen.writeFieldName("byteArray")
        val _byteArray_ = value.byteArray
        if(_byteArray_ == null) { gen.writeNull() } else
                {serializers.defaultSerializeValue(_byteArray_, gen)}
        gen.writeEndObject()
    }
}
