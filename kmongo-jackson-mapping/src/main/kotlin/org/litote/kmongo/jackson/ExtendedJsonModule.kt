/*
 * Copyright (C) 2016/2022 Litote
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
@file:OptIn(ExperimentalTime::class)

package org.litote.kmongo.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlinx.datetime.atDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import org.bson.BsonTimestamp
import org.bson.types.Binary
import org.bson.types.MaxKey
import org.bson.types.MinKey
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdTransformer
import org.litote.kmongo.id.jackson.IdKeySerializer
import org.litote.kmongo.util.KotlinxDatetimeLoader
import org.litote.kmongo.util.PatternUtil
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant as KTXInstant
import kotlinx.datetime.LocalDate as KTXLocalDate
import kotlinx.datetime.LocalDateTime as KTXLocalDateTime
import kotlinx.datetime.LocalTime as KTXLocalTime
import kotlinx.datetime.TimeZone as KTXTimeZone

internal class ExtendedJsonModule : SimpleModule() {

    object ObjectIdExtendedJsonSerializer : JsonSerializer<ObjectId>() {
        override fun serialize(value: ObjectId, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("\$oid", value.toHexString())
            gen.writeEndObject()
        }
    }

    object BinaryExtendedJsonSerializer : JsonSerializer<Binary>() {

        override fun serialize(obj: Binary, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeBinaryField("\$binary", obj.data)
            jsonGenerator.writeStringField("\$type", Integer.toHexString(obj.type.toInt()).uppercase())
            jsonGenerator.writeEndObject()
        }
    }

    object BsonTimestampExtendedJsonSerializer : JsonSerializer<BsonTimestamp>() {

        override fun serialize(
            obj: BsonTimestamp,
            jsonGenerator: JsonGenerator,
            serializerProvider: SerializerProvider
        ) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeFieldName("\$timestamp")
            jsonGenerator.writeStartObject()
            jsonGenerator.writeNumberField("t", obj.getTime())
            jsonGenerator.writeNumberField("i", obj.getInc())
            jsonGenerator.writeEndObject()
            jsonGenerator.writeEndObject()
        }
    }

    object MaxKeyExtendedJsonSerializer : JsonSerializer<MaxKey>() {

        override fun serialize(obj: MaxKey, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeNumberField("\$maxKey", 1)
            jsonGenerator.writeEndObject()
        }
    }

    object MinKeyExtendedJsonSerializer : JsonSerializer<MinKey>() {

        override fun serialize(obj: MinKey, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeNumberField("\$minKey", 1)
            jsonGenerator.writeEndObject()
        }
    }

    abstract class TemporalExtendedJsonSerializer<T> : JsonSerializer<T>() {

        override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartObject()
            gen.writeNumberField("\$date", epochMillis(value))
            gen.writeEndObject()
        }

        abstract fun epochMillis(temporal: T): Long
    }

    object DateExtendedJsonSerializer : TemporalExtendedJsonSerializer<Date>() {

        override fun epochMillis(temporal: Date): Long = temporal.time
    }

    object CalendarExtendedJsonSerializer : TemporalExtendedJsonSerializer<Calendar>() {

        override fun epochMillis(temporal: Calendar): Long = temporal.timeInMillis
    }

    object InstantExtendedJsonSerializer : TemporalExtendedJsonSerializer<Instant>() {

        override fun epochMillis(temporal: Instant): Long = temporal.toEpochMilli()
    }

    object ZonedDateTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<ZonedDateTime>() {

        override fun epochMillis(temporal: ZonedDateTime): Long =
            InstantExtendedJsonSerializer.epochMillis(temporal.toInstant())
    }

    object OffsetDateTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<OffsetDateTime>() {

        override fun epochMillis(temporal: OffsetDateTime): Long =
            InstantExtendedJsonSerializer.epochMillis(temporal.toInstant())
    }

    object LocalDateExtendedJsonSerializer : TemporalExtendedJsonSerializer<LocalDate>() {

        override fun epochMillis(temporal: LocalDate): Long =
            ZonedDateTimeExtendedJsonSerializer.epochMillis(temporal.atStartOfDay(UTC))
    }

    object LocalDateTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<LocalDateTime>() {

        override fun epochMillis(temporal: LocalDateTime): Long =
            ZonedDateTimeExtendedJsonSerializer.epochMillis(temporal.atZone(UTC))
    }

    object LocalTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<LocalTime>() {

        override fun epochMillis(temporal: LocalTime): Long =
            LocalDateTimeExtendedJsonSerializer.epochMillis(temporal.atDate(LocalDate.ofEpochDay(0)))
    }

    object OffsetTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<OffsetTime>() {

        override fun epochMillis(temporal: OffsetTime): Long =
            OffsetDateTimeExtendedJsonSerializer.epochMillis(temporal.atDate(LocalDate.ofEpochDay(0)))
    }

    object KTXInstantExtendedJsonSerializer : TemporalExtendedJsonSerializer<KTXInstant>() {

        override fun epochMillis(temporal: KTXInstant): Long = temporal.toEpochMilliseconds()
    }

    object KTXInstantExtendedJsonSerializer2 : TemporalExtendedJsonSerializer<kotlin.time.Instant>() {

        override fun epochMillis(temporal: kotlin.time.Instant): Long = temporal.toEpochMilliseconds()
    }

    object KTXLocalDateExtendedJsonSerializer : TemporalExtendedJsonSerializer<KTXLocalDate>() {

        override fun epochMillis(temporal: KTXLocalDate): Long =
            KTXInstantExtendedJsonSerializer2.epochMillis(temporal.atStartOfDayIn(KTXTimeZone.UTC))
    }

    object KTXLocalDateTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<KTXLocalDateTime>() {

        override fun epochMillis(temporal: KTXLocalDateTime): Long =
            KTXInstantExtendedJsonSerializer2.epochMillis(temporal.toInstant(KTXTimeZone.UTC))
    }

    object KTXLocalTimeExtendedJsonSerializer : TemporalExtendedJsonSerializer<KTXLocalTime>() {

        override fun epochMillis(temporal: KTXLocalTime): Long =
            KTXInstantExtendedJsonSerializer2.epochMillis(temporal.atDate(KTXLocalDate.fromEpochDays(0)).toInstant(KTXTimeZone.UTC))
    }

    object IdSerializer : JsonSerializer<Id<*>>() {

        override fun serialize(id: Id<*>, generator: JsonGenerator, provider: SerializerProvider) {
            generator.writeObject(IdTransformer.unwrapId(id))
        }
    }

    object BigDecimalSerializer : JsonSerializer<BigDecimal>() {

        override fun serialize(bigDecimal: BigDecimal, generator: JsonGenerator, provider: SerializerProvider) {
            generator.writeStartObject()
            generator.writeStringField("\$numberDecimal", bigDecimal.toString())
            generator.writeEndObject()
        }
    }

    private object PatternSerializer : JsonSerializer<Pattern>() {
        override fun serialize(obj: Pattern, gen: JsonGenerator, serializerProvider: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("\$regex", obj.pattern())
            gen.writeStringField("\$options", PatternUtil.getOptionsAsString(obj))
            gen.writeEndObject()
        }
    }

    private object RegexSerializer : JsonSerializer<Regex>() {
        override fun serialize(obj: Regex, gen: JsonGenerator, serializerProvider: SerializerProvider) {
            PatternSerializer.serialize(obj.toPattern(), gen, serializerProvider)
        }
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
        addSerializer(BigDecimal::class.java, BigDecimalSerializer)

        addSerializer(Id::class.java, IdSerializer)
        addKeySerializer(Id::class.java, IdKeySerializer())

        addSerializer(Date::class.java, DateExtendedJsonSerializer)
        addSerializer(Calendar::class.java, CalendarExtendedJsonSerializer)
        addSerializer(Instant::class.java, InstantExtendedJsonSerializer)
        addSerializer(ZonedDateTime::class.java, ZonedDateTimeExtendedJsonSerializer)
        addSerializer(OffsetDateTime::class.java, OffsetDateTimeExtendedJsonSerializer)
        addSerializer(LocalDate::class.java, LocalDateExtendedJsonSerializer)
        addSerializer(LocalDateTime::class.java, LocalDateTimeExtendedJsonSerializer)
        addSerializer(LocalTime::class.java, LocalTimeExtendedJsonSerializer)
        addSerializer(OffsetTime::class.java, OffsetTimeExtendedJsonSerializer)

        KotlinxDatetimeLoader.loadKotlinxDateTime({
            addSerializer(KTXInstant::class.java, KTXInstantExtendedJsonSerializer)
            addSerializer(KTXLocalDate::class.java, KTXLocalDateExtendedJsonSerializer)
            addSerializer(KTXLocalDateTime::class.java, KTXLocalDateTimeExtendedJsonSerializer)
            addSerializer(KTXLocalTime::class.java, KTXLocalTimeExtendedJsonSerializer)
        }, {})

        addSerializer(Pattern::class.java, PatternSerializer)
        addSerializer(Regex::class.java, RegexSerializer)
    }
}


