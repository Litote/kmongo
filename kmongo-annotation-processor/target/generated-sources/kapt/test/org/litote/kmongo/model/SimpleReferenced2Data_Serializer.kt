package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class SimpleReferenced2Data_Serializer :
        StdSerializer<SimpleReferenced2Data>(SimpleReferenced2Data::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(SimpleReferenced2Data::class.java, this)

    override fun serialize(
        value: SimpleReferenced2Data,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("price")
        val _price_ = value.price
        gen.writeNumber(_price_)
        gen.writeEndObject()
    }
}
