/*
 * Copyright (C) 2016 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.litote.kmongo.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.bson.BsonTimestamp
import org.bson.types.Binary
import org.bson.types.MaxKey
import org.bson.types.MinKey
import org.bson.types.ObjectId
import org.litote.kmongo.jackson.BsonModule.Companion.mongoZoneId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date

internal class ExtendedJsonModule : SimpleModule() {

    internal object ObjectIdExtendedJsonSerializer : JsonSerializer<ObjectId>() {
        override fun serialize(value: ObjectId, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("\$oid", value.toHexString())
            gen.writeEndObject()
        }
    }

    internal object BinaryExtendedJsonSerializer : JsonSerializer<Binary>() {

        override fun serialize(obj: Binary, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeBinaryField("\$binary", obj.data)
            jsonGenerator.writeStringField("\$type", Integer.toHexString(obj.type.toInt()).toUpperCase())
            jsonGenerator.writeEndObject()
        }
    }

    internal object BsonTimestampExtendedJsonSerializer : JsonSerializer<BsonTimestamp>() {

        override fun serialize(obj: BsonTimestamp, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeFieldName("\$timestamp")
            jsonGenerator.writeStartObject()
            jsonGenerator.writeNumberField("t", obj.getTime())
            jsonGenerator.writeNumberField("i", obj.getInc())
            jsonGenerator.writeEndObject()
            jsonGenerator.writeEndObject()
        }
    }

    internal object MaxKeyExtendedJsonSerializer : JsonSerializer<MaxKey>() {

        override fun serialize(obj: MaxKey, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeNumberField("\$maxKey", 1)
            jsonGenerator.writeEndObject()
        }
    }

    internal object MinKeyExtendedJsonSerializer : JsonSerializer<MinKey>() {

        override fun serialize(obj: MinKey, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeNumberField("\$minKey", 1)
            jsonGenerator.writeEndObject()
        }
    }

    internal abstract class TemporalExtendedJsonSerializer<T> : JsonSerializer<T>() {

        override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartObject()
            gen.writeNumberField("\$date", epochMillis(value))
            gen.writeEndObject()
        }

        abstract fun epochMillis(temporal: T): Long
    }

    internal object DateExtendedJsonSerializer : TemporalExtendedJsonSerializer<Date>() {

        override fun epochMillis(temporal: Date): Long
                = temporal.time
    }

    internal object CalendarExtendedJsonSerializer : TemporalExtendedJsonSerializer<Calendar>() {

        override fun epochMillis(temporal: Calendar): Long
                = temporal.timeInMillis
    }

    internal object InstantExtendedJsonSerializer : TemporalExtendedJsonSerializer<Instant>() {

        override fun epochMillis(temporal: Instant): Long
                = temporal.toEpochMilli()
    }

    internal object ZonedDateTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<ZonedDateTime>() {

        override fun epochMillis(temporal: ZonedDateTime): Long
                = InstantExtendedJsonSerializer.epochMillis(temporal.toInstant())
    }

    internal object OffsetDateTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<OffsetDateTime>() {

        override fun epochMillis(temporal: OffsetDateTime): Long
                = InstantExtendedJsonSerializer.epochMillis(temporal.toInstant())
    }

    internal object LocalDateExtendedJsonSerializer : TemporalExtendedJsonSerializer<LocalDate>() {

        override fun epochMillis(temporal: LocalDate): Long
                = ZonedDateTimeExtendedJsonSerializer.epochMillis(temporal.atStartOfDay(mongoZoneId))
    }

    internal object LocalDateTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<LocalDateTime>() {

        override fun epochMillis(temporal: LocalDateTime): Long
                = ZonedDateTimeExtendedJsonSerializer.epochMillis(temporal.atZone(mongoZoneId))
    }

    internal object LocalTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<LocalTime>() {

        override fun epochMillis(temporal: LocalTime): Long
                = LocalDateTimeExtendedJsonSerializer.epochMillis(temporal.atDate(LocalDate.ofEpochDay(0)))
    }

    internal object OffsetTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<OffsetTime>() {

        override fun epochMillis(temporal: OffsetTime): Long
                = OffsetDateTimeExtendedJsonSerializer.epochMillis(temporal.atDate(LocalDate.ofEpochDay(0)))
    }

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        context.appendAnnotationIntrospector(KMongoAnnotationIntrospector.INTROSPECTOR)
    }

    init {
        addSerializer(ObjectId::class.java, ObjectIdExtendedJsonSerializer)
        addSerializer(Binary::class.java, BinaryExtendedJsonSerializer)
        addSerializer(BsonTimestamp::class.java, BsonTimestampExtendedJsonSerializer)
        addSerializer(MaxKey::class.java, MaxKeyExtendedJsonSerializer)
        addSerializer(MinKey::class.java, MinKeyExtendedJsonSerializer)

        addSerializer(Date::class.java, DateExtendedJsonSerializer)
        addSerializer(Calendar::class.java, CalendarExtendedJsonSerializer)
        addSerializer(Instant::class.java, InstantExtendedJsonSerializer)
        addSerializer(ZonedDateTime::class.java, ZonedDateTimeExtendedJsonSerializer)
        addSerializer(OffsetDateTime::class.java, OffsetDateTimeExtendedJsonSerializer)
        addSerializer(LocalDate::class.java, LocalDateExtendedJsonSerializer)
        addSerializer(LocalDateTime::class.java, LocalDateTimeExtendedJsonSerializer)
        addSerializer(LocalTime::class.java, LocalTimeExtendedJsonSerializer)
        addSerializer(OffsetTime::class.java, OffsetTimeExtendedJsonSerializer)
    }
}


