package org.litote.kmongo.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class InternalDataClass_Serializer :
        StdSerializer<InternalDataClass>(InternalDataClass::class.java), JacksonModuleServiceLoader
        {
    override fun module() = SimpleModule().addSerializer(InternalDataClass::class.java, this)

    override fun serialize(
        value: InternalDataClass,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("l")
        val _l_ = value.l
        gen.writeNumber(_l_)
        gen.writeEndObject()
    }
}
