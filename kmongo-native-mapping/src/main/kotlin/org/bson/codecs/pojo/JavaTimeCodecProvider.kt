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

package org.bson.codecs.pojo

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DateCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.ZonedDateTime.ofInstant
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.reflect.KClass

/**
 *
 */
internal object JavaTimeCodecProvider : CodecProvider {

    private val dateCodec = DateCodec()

    private abstract class JavaTimeCodec<T : Any>(val kClass: KClass<T>) : Codec<T> {

        override fun getEncoderClass(): Class<T> {
            return kClass.java
        }

        fun date(temporal: T): Date = Date(epochMillis(temporal))

        abstract fun epochMillis(temporal: T): Long

        abstract fun toTemporal(date: Date): T

        override fun encode(writer: BsonWriter, value: T, encoderContext: EncoderContext) {
            dateCodec.encode(writer, date(value), encoderContext)
        }

        override fun decode(reader: BsonReader, decoderContext: DecoderContext): T {
            return toTemporal(dateCodec.decode(reader, decoderContext))
        }
    }

    private abstract class AbstractCalendarCodec<T : Calendar>(kClass: KClass<T>) : JavaTimeCodec<T>(kClass) {

        override fun epochMillis(temporal: T): Long {
            val cloned = (temporal.clone() as Calendar)
            cloned.timeZone = TimeZone.getTimeZone("UTC")
            return cloned.timeInMillis
        }

        override fun toTemporal(date: Date): T =
            getInstance(date).apply {
                time = date
            }

        abstract fun getInstance(date: Date): T
    }

    private object CalendarCodec : AbstractCalendarCodec<Calendar>(Calendar::class) {

        override fun getInstance(date: Date): Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    }

    private object GregorianCalendarCodec : AbstractCalendarCodec<GregorianCalendar>(GregorianCalendar::class) {

        override fun getInstance(date: Date): GregorianCalendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
    }


    private object InstantCodec : JavaTimeCodec<Instant>(Instant::class) {

        override fun epochMillis(temporal: Instant): Long = temporal.toEpochMilli()

        override fun toTemporal(date: Date): Instant = date.toInstant()
    }

    private object ZonedDateTimeCodec : JavaTimeCodec<ZonedDateTime>(ZonedDateTime::class) {

        override fun epochMillis(temporal: ZonedDateTime): Long = temporal.toInstant().toEpochMilli()

        override fun toTemporal(date: Date): ZonedDateTime = ofInstant(date.toInstant(), UTC)
    }

    private object OffsetDateTimeCodec : JavaTimeCodec<OffsetDateTime>(OffsetDateTime::class) {

        override fun epochMillis(temporal: OffsetDateTime): Long = temporal.toInstant().toEpochMilli()

        override fun toTemporal(date: Date): OffsetDateTime = OffsetDateTime.ofInstant(date.toInstant(), UTC)
    }

    private object OffsetTimeCodec : JavaTimeCodec<OffsetTime>(OffsetTime::class) {

        override fun epochMillis(temporal: OffsetTime): Long =
            OffsetDateTimeCodec.epochMillis(temporal.atDate(LocalDate.ofEpochDay(0)))

        override fun toTemporal(date: Date): OffsetTime = OffsetDateTimeCodec.toTemporal(date).toOffsetTime()
    }

    private object LocalDateTimeCodec : JavaTimeCodec<LocalDateTime>(LocalDateTime::class) {

        override fun epochMillis(temporal: LocalDateTime): Long = ZonedDateTimeCodec.epochMillis(temporal.atZone(UTC))

        override fun toTemporal(date: Date): LocalDateTime = LocalDateTime.ofInstant(date.toInstant(), UTC)
    }

    private object LocalDateCodec : JavaTimeCodec<LocalDate>(LocalDate::class) {

        override fun epochMillis(temporal: LocalDate): Long = ZonedDateTimeCodec.epochMillis(temporal.atStartOfDay(UTC))

        override fun toTemporal(date: Date): LocalDate = LocalDateTimeCodec.toTemporal(date).toLocalDate()
    }

    private object LocalTimeCodec : JavaTimeCodec<LocalTime>(LocalTime::class) {

        override fun epochMillis(temporal: LocalTime): Long =
            LocalDateTimeCodec.epochMillis(temporal.atDate(LocalDate.ofEpochDay(0)))

        override fun toTemporal(date: Date): LocalTime = LocalDateTimeCodec.toTemporal(date).toLocalTime()
    }

    private val codecs: Map<KClass<*>, JavaTimeCodec<*>> =
        try {
            listOf(
                CalendarCodec,
                GregorianCalendarCodec,
                InstantCodec,
                ZonedDateTimeCodec,
                OffsetDateTimeCodec,
                OffsetTimeCodec,
                LocalDateTimeCodec,
                LocalDateCodec,
                LocalTimeCodec
            )
        } catch (e: NoClassDefFoundError) {
            //jdk7 version
            listOf(
                CalendarCodec,
                GregorianCalendarCodec
            )
        }.map { it.kClass to it }.toMap()

    override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
        @Suppress("UNCHECKED_CAST")
        return codecs[clazz.kotlin] as Codec<T>?
    }
}