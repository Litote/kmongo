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
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(org.litote.kmongo.model.other.SimpleReferencedData::class.java)
                ),
                true,
                null
                )
                .serialize(_set_, gen, serializers)
        gen.writeFieldName("list")
        val _list_ = value.list
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(kotlin.Boolean::class.java)
                )
                ),
                true,
                null
                )
                .serialize(_list_, gen, serializers)
        gen.writeFieldName("name")
        val _name_ = value.name
        if(_name_ == null) { gen.writeNull() } else {
                gen.writeString(_name_)
                }
        gen.writeFieldName("date")
        val _date_ = value.date
        if(_date_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_date_, gen)
                }
        gen.writeFieldName("referenced")
        val _referenced_ = value.referenced
        if(_referenced_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_referenced_, gen)
                }
        gen.writeFieldName("map")
        val _map_ = value.map
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(org.litote.kmongo.Id::class.java),
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                )
                ),
                true,
                null
                )
                .serialize(_map_, gen, serializers)
        gen.writeFieldName("map2")
        val _map2_ = value.map2
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(java.util.Locale::class.java),
                serializers.config.typeFactory.constructType(org.litote.kmongo.model.SimpleReferenced2Data::class.java)
                ),
                true,
                null
                )
                .serialize(_map2_, gen, serializers)
        gen.writeFieldName("nullableFloat")
        val _nullableFloat_ = value.nullableFloat
        if(_nullableFloat_ == null) { gen.writeNull() } else {
                gen.writeNumber(_nullableFloat_)
                }
        gen.writeFieldName("nullableBoolean")
        val _nullableBoolean_ = value.nullableBoolean
        if(_nullableBoolean_ == null) { gen.writeNull() } else {
                gen.writeBoolean(_nullableBoolean_)
                }
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
        if(_byteArray_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_byteArray_, gen)
                }
        gen.writeEndObject()
    }
}
