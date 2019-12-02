/*
 * Copyright (C) 2017/2019 Litote
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

package org.litote.kmongo.serialization

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.FlexibleDecoder
import com.github.jershell.kbson.ObjectIdSerializer
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.bson.AbstractBsonReader.State
import org.bson.BsonTimestamp
import org.bson.BsonType
import org.bson.types.Binary
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.IdTransformer
import org.litote.kmongo.id.ObjectIdGenerator
import org.litote.kmongo.projection
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.reflect.KProperty

/**
 *
 */
abstract class TemporalExtendedJsonSerializer<T> : KSerializer<T> {

    override val descriptor: SerialDescriptor = StringDescriptor.withName(javaClass.simpleName)

    /**
     * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * represented by this <tt>Temporal</tt> object.
     *
     * @return  the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *          represented by this date.
     */
    abstract fun epochMillis(temporal: T): Long

    abstract fun instantiate(date: Long): T

    override fun serialize(encoder: Encoder, obj: T) {
        encoder as BsonEncoder
        encoder.encodeDateTime(epochMillis(obj))
    }

    override fun deserialize(decoder: Decoder): T {
        return when (decoder) {
            is FlexibleDecoder -> {
                instantiate(
                    when (decoder.reader.currentBsonType) {
                        BsonType.STRING -> decoder.decodeString().toLong()
                        BsonType.DATE_TIME -> decoder.reader.readDateTime()
                        else -> throw SerializationException("Unsupported ${decoder.reader.currentBsonType} reading date")
                    }
                )
            }
            else -> throw SerializationException("Unknown decoder type")
        }
    }
}

//@Serializer(forClass = Calendar::class)
object CalendarSerializer : TemporalExtendedJsonSerializer<Calendar>() {

    override fun epochMillis(temporal: Calendar): Long = temporal.timeInMillis

    override fun instantiate(date: Long): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            time = Date(date)
        }
}

//@Serializer(forClass = Instant::class)
object InstantSerializer : TemporalExtendedJsonSerializer<Instant>() {

    override fun epochMillis(temporal: Instant): Long = temporal.toEpochMilli()

    override fun instantiate(date: Long): Instant = Instant.ofEpochMilli(date)
}

//@Serializer(forClass = ZonedDateTime::class)
object ZonedDateTimeSerializer : TemporalExtendedJsonSerializer<ZonedDateTime>() {

    override fun epochMillis(temporal: ZonedDateTime): Long =
        InstantSerializer.epochMillis(temporal.toInstant())

    override fun instantiate(date: Long): ZonedDateTime =
        ZonedDateTime.ofInstant(InstantSerializer.instantiate(date), ZoneOffset.UTC)
}

//@Serializer(forClass = OffsetDateTime::class)
object OffsetDateTimeSerializer : TemporalExtendedJsonSerializer<OffsetDateTime>() {

    override fun epochMillis(temporal: OffsetDateTime): Long =
        InstantSerializer.epochMillis(temporal.toInstant())

    override fun instantiate(date: Long): OffsetDateTime =
        OffsetDateTime.ofInstant(InstantSerializer.instantiate(date), ZoneOffset.UTC)
}

//@Serializer(forClass = LocalDate::class)
object LocalDateSerializer : TemporalExtendedJsonSerializer<LocalDate>() {

    override fun epochMillis(temporal: LocalDate): Long =
        ZonedDateTimeSerializer.epochMillis(temporal.atStartOfDay(ZoneOffset.UTC))

    override fun instantiate(date: Long): LocalDate =
        LocalDateTimeSerializer.instantiate(date).toLocalDate()
}

//@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeSerializer : TemporalExtendedJsonSerializer<LocalDateTime>() {

    override fun epochMillis(temporal: LocalDateTime): Long =
        ZonedDateTimeSerializer.epochMillis(temporal.atZone(ZoneOffset.UTC))

    override fun instantiate(date: Long): LocalDateTime =
        LocalDateTime.ofInstant(InstantSerializer.instantiate(date), ZoneOffset.UTC)
}

//@Serializer(forClass = LocalTime::class)
object LocalTimeSerializer : TemporalExtendedJsonSerializer<LocalTime>() {

    override fun epochMillis(temporal: LocalTime): Long =
        LocalDateTimeSerializer.epochMillis(temporal.atDate(LocalDate.ofEpochDay(0)))

    override fun instantiate(date: Long): LocalTime =
        LocalDateTimeSerializer.instantiate(date).toLocalTime()
}

//@Serializer(forClass = OffsetTime::class)
object OffsetTimeSerializer : TemporalExtendedJsonSerializer<OffsetTime>() {

    override fun epochMillis(temporal: OffsetTime): Long =
        OffsetDateTimeSerializer.epochMillis(temporal.atDate(LocalDate.ofEpochDay(0)))

    override fun instantiate(date: Long): OffsetTime =
        OffsetDateTimeSerializer.instantiate(date).toOffsetTime()
}

//@Serializer(forClass = BsonTimestamp::class)
object BsonTimestampSerializer : TemporalExtendedJsonSerializer<BsonTimestamp>() {

    override fun epochMillis(temporal: BsonTimestamp): Long =
        temporal.value

    override fun instantiate(date: Long): BsonTimestamp = BsonTimestamp(date)
}

//@Serializer(forClass = Binary::class)
object BinarySerializer : KSerializer<Binary> {

    override val descriptor: SerialDescriptor = StringDescriptor.withName("BinarySerializer")
    override fun deserialize(decoder: Decoder): Binary =
        Binary((decoder as FlexibleDecoder).reader.readBinaryData().data)

    override fun serialize(encoder: Encoder, obj: Binary) {
        (encoder as BsonEncoder).encodeByteArray(obj.data)
    }
}

//@Serializer(forClass = Locale::class)
object LocaleSerializer : KSerializer<Locale> {

    override val descriptor: SerialDescriptor = StringDescriptor.withName("LocaleSerializer")
    override fun deserialize(decoder: Decoder): Locale = Locale.forLanguageTag(decoder.decodeString())

    override fun serialize(encoder: Encoder, obj: Locale) {
        encoder.encodeString(obj.toLanguageTag())
    }
}

//@Serializer(forClass = KProperty::class)
object KPropertySerializer : KSerializer<KProperty<*>> {

    override val descriptor: SerialDescriptor = StringDescriptor.withName("KPropertySerializer")
    override fun deserialize(decoder: Decoder): KProperty<*> = error("KProperty deserialization is unsupported")

    override fun serialize(encoder: Encoder, obj: KProperty<*>) {
        encoder.encodeString(obj.projection)
    }
}

//@Serializer(forClass = Id::class)
internal class IdSerializer<T : Id<*>>(val shouldBeStringId: Boolean) : KSerializer<T> {

    override val descriptor: SerialDescriptor = StringDescriptor.withName("IdSerializer")

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): T =
        IdTransformer.wrapId(deserializeObjectId(decoder as FlexibleDecoder)) as T

    private fun deserializeObjectId(decoder: FlexibleDecoder): Any {
        return if (decoder.reader.state == State.NAME) {
            val keyId = decoder.reader.readName()
            if (shouldBeStringId || IdGenerator.defaultGenerator != ObjectIdGenerator) keyId else ObjectId(keyId)
        } else {
            when (decoder.reader.currentBsonType) {
                BsonType.STRING -> decoder.decodeString()
                BsonType.OBJECT_ID -> decoder.reader.readObjectId()
                else -> throw SerializationException("Unsupported ${decoder.reader.currentBsonType} reading object id")
            }
        }
    }

    override fun serialize(encoder: Encoder, obj: T) {
        IdTransformer.unwrapId(obj).also {
            when (it) {
                is String -> encoder.encodeString(it)
                is ObjectId -> ObjectIdSerializer.serialize(encoder, it)
                else -> error("unsupported id type $obj")
            }
        }
    }

}