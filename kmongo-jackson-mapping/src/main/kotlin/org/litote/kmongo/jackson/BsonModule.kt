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

import com.fasterxml.jackson.core.Base64Variants
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_FLOAT
import com.fasterxml.jackson.core.JsonToken.VALUE_STRING
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.node.ValueNode
import de.undercouch.bson4jackson.BsonConstants
import de.undercouch.bson4jackson.BsonParser
import de.undercouch.bson4jackson.types.Decimal128
import org.bson.BsonTimestamp
import org.bson.types.Binary
import org.bson.types.MaxKey
import org.bson.types.MinKey
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdTransformer
import org.litote.kmongo.id.jackson.IdKeyDeserializer
import org.litote.kmongo.id.jackson.IdKeySerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.BinaryExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.BsonTimestampExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.CalendarExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.DateExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.InstantExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.LocalDateExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.LocalDateTimeExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.LocalTimeExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.MaxKeyExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.MinKeyExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.ObjectIdExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.OffsetDateTimeExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.OffsetTimeExtendedJsonSerializer
import org.litote.kmongo.jackson.ExtendedJsonModule.ZonedDateTimeExtendedJsonSerializer
import org.litote.kmongo.jackson.KMongoBsonFactory.KMongoBsonGenerator
import org.litote.kmongo.projection
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.reflect.KProperty


internal class BsonModule : SimpleModule() {

    private object ObjectIdBsonSerializer : JsonSerializer<ObjectId>() {

        override fun serialize(objectId: ObjectId, gen: JsonGenerator, serializerProvider: SerializerProvider) {
            if (gen is KMongoBsonGenerator) {
                gen.writeObjectId(objectId)
            } else {
                ObjectIdExtendedJsonSerializer.serialize(objectId, gen, serializerProvider)
            }
        }
    }

    private object ObjectIdBsonDeserializer : JsonDeserializer<ObjectId>() {

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ObjectId {
            return if (p is BsonParser) {
                p.embeddedObject as ObjectId
            } else {
                val tree: TreeNode = p.codec.readTree(p)
                if (tree is POJONode) {
                    tree.pojo as ObjectId
                } else {
                    ObjectId((tree.get("\$oid") as JsonNode).textValue())
                }
            }
        }
    }

    private object BsonTimestampBsonSerializer : JsonSerializer<BsonTimestamp>() {

        override fun serialize(obj: BsonTimestamp, gen: JsonGenerator, serializerProvider: SerializerProvider) {
            if (gen is KMongoBsonGenerator) {
                gen.writeBsonTimestamp(obj)
            } else {
                BsonTimestampExtendedJsonSerializer.serialize(obj, gen, serializerProvider)
            }
        }
    }

    private object BsonTimestampBsonDeserializer : JsonDeserializer<BsonTimestamp>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): BsonTimestamp {
            val tree = jp.codec.readTree<TreeNode>(jp)
            if (tree.isObject) {
                val timestamp = tree.get("\$timestamp")
                val time = (timestamp.get("t") as ValueNode).asInt()
                val inc = (timestamp.get("i") as ValueNode).asInt()
                return BsonTimestamp(time, inc)
            } else if (tree is POJONode) {
                return tree.pojo as BsonTimestamp
            } else {
                return ctxt.handleUnexpectedToken(BsonTimestamp::class.java, jp) as BsonTimestamp
            }
        }
    }

    private object BinaryBsonSerializer : JsonSerializer<Binary>() {

        override fun serialize(obj: Binary, gen: JsonGenerator, serializerProvider: SerializerProvider) {
            if (gen is KMongoBsonGenerator) {
                gen.writeBinary(obj)
            } else {
                BinaryExtendedJsonSerializer.serialize(obj, gen, serializerProvider)
            }
        }
    }

    private object BinaryBsonDeserializer : JsonDeserializer<Binary>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Binary {
            val tree = jp.codec.readTree<TreeNode>(jp)
            if (tree.isObject) {
                val binary = Base64Variants.MIME_NO_LINEFEEDS.decode((tree.get("\$binary") as ValueNode).asText())
                val type = Integer.valueOf((tree.get("\$type") as ValueNode).asText().toLowerCase(), 16)!!.toByte()
                return Binary(type, binary)
            } else if (tree is POJONode) {
                return tree.pojo as Binary
            } else if (tree is BinaryNode) {
                return Binary(tree.binaryValue())
            } else {
                return ctxt.handleUnexpectedToken(ObjectId::class.java, jp) as Binary
            }
        }
    }

    private object MaxKeyBsonSerializer : JsonSerializer<MaxKey>() {

        override fun serialize(obj: MaxKey, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            if (jsonGenerator is KMongoBsonGenerator) {
                jsonGenerator.writeMaxKey()
            } else {
                MaxKeyExtendedJsonSerializer.serialize(obj, jsonGenerator, serializerProvider)
            }
        }
    }

    private object MaxKeyBsonDeserializer : JsonDeserializer<MaxKey>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): MaxKey {
            val tree = jp.codec.readTree<TreeNode>(jp)
            if (tree.isObject) {
                val value = (tree.get("\$maxKey") as ValueNode).asInt()
                if (value == 1) {
                    return MaxKey()
                }
                return ctxt.handleUnexpectedToken(MaxKey::class.java, jp) as MaxKey
            } else if (tree is POJONode) {
                return tree.pojo as MaxKey
            } else if (tree is TextNode) {
                return MaxKey()
            } else {
                return ctxt.handleUnexpectedToken(MaxKey::class.java, jp) as MaxKey
            }
        }
    }

    private object MinKeyBsonSerializer : JsonSerializer<MinKey>() {

        override fun serialize(obj: MinKey, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            if (jsonGenerator is KMongoBsonGenerator) {
                jsonGenerator.writeMinKey()
            } else {
                MinKeyExtendedJsonSerializer.serialize(obj, jsonGenerator, serializerProvider)
            }
        }
    }

    private object MinKeyBsonDeserializer : JsonDeserializer<MinKey>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): MinKey {
            val tree = jp.codec.readTree<TreeNode>(jp)
            if (tree.isObject) {
                val value = (tree.get("\$minKey") as ValueNode).asInt()
                if (value == 1) {
                    return MinKey()
                }
                return ctxt.handleUnexpectedToken(MinKey::class.java, jp) as MinKey
            } else if (tree is POJONode) {
                return tree.pojo as MinKey
            } else if (tree is TextNode) {
                return MinKey()
            } else {
                return ctxt.handleUnexpectedToken(MinKey::class.java, jp) as MinKey
            }
        }
    }

    private abstract class TemporalBsonSerializer<T> : JsonSerializer<T>() {

        override fun serialize(value: T, gen: JsonGenerator, serializerProvider: SerializerProvider) {
            if (gen is KMongoBsonGenerator) {
                gen.writeDateTime(date(value))
            } else {
                DateExtendedJsonSerializer.serialize(date(value), gen, serializerProvider)
            }
        }

        fun date(temporal: T): Date = Date(epochMillis(temporal))

        abstract fun epochMillis(temporal: T): Long
    }

    private object CalendarBsonSerializer : TemporalBsonSerializer<Calendar>() {
        override fun epochMillis(temporal: Calendar): Long = CalendarExtendedJsonSerializer.epochMillis(temporal)
    }

    private object ZonedDateTimeBsonSerializer : TemporalBsonSerializer<ZonedDateTime>() {

        override fun epochMillis(temporal: ZonedDateTime): Long =
            ZonedDateTimeExtendedJsonSerializer.epochMillis(temporal)
    }

    private object OffsetDateTimeBsonSerializer : TemporalBsonSerializer<OffsetDateTime>() {

        override fun epochMillis(temporal: OffsetDateTime): Long =
            OffsetDateTimeExtendedJsonSerializer.epochMillis(temporal)
    }

    private object LocalDateBsonSerializer : TemporalBsonSerializer<LocalDate>() {

        override fun epochMillis(temporal: LocalDate): Long = LocalDateExtendedJsonSerializer.epochMillis(temporal)
    }

    private object LocalTimeBsonSerializer : TemporalBsonSerializer<LocalTime>() {

        override fun epochMillis(temporal: LocalTime): Long = LocalTimeExtendedJsonSerializer.epochMillis(temporal)
    }

    private object OffsetTimeBsonSerializer : TemporalBsonSerializer<OffsetTime>() {

        override fun epochMillis(temporal: OffsetTime): Long = OffsetTimeExtendedJsonSerializer.epochMillis(temporal)
    }

    private object InstantBsonSerializer : TemporalBsonSerializer<Instant>() {

        override fun epochMillis(temporal: Instant): Long = InstantExtendedJsonSerializer.epochMillis(temporal)
    }

    private object LocalDateTimeBsonSerializer : TemporalBsonSerializer<LocalDateTime>() {

        override fun epochMillis(temporal: LocalDateTime): Long =
            LocalDateTimeExtendedJsonSerializer.epochMillis(temporal)
    }

    private abstract class TemporalBsonDeserializer<T> : JsonDeserializer<T>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): T {
            return toObject(jp.embeddedObject.run {
                //sometimes the date is a timestamp ( see https://github.com/Litote/kmongo/issues/35 )
                if (this == null) {
                    Date(jp.longValue)
                } else {
                    this as Date
                }
            })
        }

        abstract fun toObject(date: Date): T
    }

    private object CalendarBsonDeserializer : TemporalBsonDeserializer<Calendar>() {

        override fun toObject(date: Date): Calendar =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                time = date
            }
    }

    private object ZonedDateTimeBsonDeserializer : TemporalBsonDeserializer<ZonedDateTime>() {

        override fun toObject(date: Date): ZonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), UTC)
    }

    private object OffsetDateTimeBsonDeserializer : TemporalBsonDeserializer<OffsetDateTime>() {

        override fun toObject(date: Date): OffsetDateTime = OffsetDateTime.ofInstant(date.toInstant(), UTC)
    }

    private object LocalDateTimeBsonDeserializer : TemporalBsonDeserializer<LocalDateTime>() {

        override fun toObject(date: Date): LocalDateTime = LocalDateTime.ofInstant(date.toInstant(), UTC)
    }

    private object LocalDateBsonDeserializer : TemporalBsonDeserializer<LocalDate>() {

        override fun toObject(date: Date): LocalDate = LocalDateTimeBsonDeserializer.toObject(date).toLocalDate()
    }

    private object LocalTimeBsonDeserializer : TemporalBsonDeserializer<LocalTime>() {

        override fun toObject(date: Date): LocalTime = LocalDateTimeBsonDeserializer.toObject(date).toLocalTime()
    }

    private object OffsetTimeBsonDeserializer : TemporalBsonDeserializer<OffsetTime>() {

        override fun toObject(date: Date): OffsetTime = OffsetDateTimeBsonDeserializer.toObject(date).toOffsetTime()
    }

    private object InstantBsonDeserializer : TemporalBsonDeserializer<Instant>() {

        override fun toObject(date: Date): Instant = date.toInstant()
    }

    private object IdBsonSerializer : JsonSerializer<Id<*>>() {

        override fun serialize(id: Id<*>, generator: JsonGenerator, provider: SerializerProvider) {
            IdTransformer.unwrapId(id).also {
                when (it) {
                    is String -> generator.writeString(it)
                    is ObjectId -> ObjectIdBsonSerializer.serialize(it, generator, provider)
                    else -> error("unsupported id type $id")
                }
            }
        }
    }

    private object IdBsonDeserializer : JsonDeserializer<Id<*>>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Id<*> {
            return if (jp.currentToken == VALUE_STRING) {
                IdTransformer.wrapId(jp.valueAsString)
            } else {
                IdTransformer.wrapId(jp.embeddedObject)
            }
        }
    }

    private object BigDecimalBsonSerializer : JsonSerializer<BigDecimal>() {

        override fun serialize(decimal: BigDecimal, generator: JsonGenerator, provider: SerializerProvider) {
            generator.writeNumber(decimal)
        }
    }

    private object BigDecimalBsonDeserializer : JsonDeserializer<BigDecimal>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): BigDecimal {
            return if (jp.currentToken == VALUE_NUMBER_FLOAT) {
                BigDecimal(jp.doubleValue)
            } else {
                (jp.embeddedObject as Decimal128).bigDecimalValue()
            }
        }
    }

    private object Decimal128BsonSerializer : JsonSerializer<Decimal128>() {

        override fun serialize(decimal: Decimal128, generator: JsonGenerator, provider: SerializerProvider) {
            BigDecimalBsonSerializer.serialize(decimal.bigDecimalValue(), generator, provider)
        }
    }

    private object Decimal128BsonDeserializer : JsonDeserializer<Decimal128>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Decimal128 {
            return Decimal128(BigDecimalBsonDeserializer.deserialize(jp, ctxt))
        }
    }

    private object KPropertySerializer : JsonSerializer<KProperty<*>>() {

        override fun serialize(property: KProperty<*>, generator: JsonGenerator, provider: SerializerProvider) {
            generator.writeString(property.projection)
        }

        override fun acceptJsonFormatVisitor(visitor: JsonFormatVisitorWrapper, type: JavaType) {
            visitor.expectStringFormat(type)
        }
    }

    private object ZoneIdBsonSerializer : JsonSerializer<ZoneId>() {
        override fun serialize(obj: ZoneId, gen: JsonGenerator, serializerProvider: SerializerProvider) {
            gen.writeString(obj.toString())
        }
    }

    private object ZoneIdBsonDeserializer : JsonDeserializer<ZoneId>() {
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): ZoneId {
            return ZoneId.of(jp.valueAsString)
        }
    }

    private object BsonDateDeserializer : JsonDeserializer<Date>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Date {
            if (jp is BsonParser) {
                if (jp.currentToken != JsonToken.VALUE_EMBEDDED_OBJECT || jp.currentBsonType != BsonConstants.TYPE_DATETIME) {
                    throw ctxt.handleUnexpectedToken(Date::class.java, jp) as Throwable
                }
                return jp.embeddedObject as Date
            } else {
                return jp.embeddedObject?.let { it as Date } ?: Date(jp.longValue)
            }
        }
    }

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        context.appendAnnotationIntrospector(KMongoAnnotationIntrospector.INTROSPECTOR)
    }

    init {
        addSerializer(ObjectId::class.java, ObjectIdBsonSerializer)
        addDeserializer(ObjectId::class.java, ObjectIdBsonDeserializer)
        addSerializer(Binary::class.java, BinaryBsonSerializer)
        addDeserializer(Binary::class.java, BinaryBsonDeserializer)
        addSerializer(BsonTimestamp::class.java, BsonTimestampBsonSerializer)
        addDeserializer(BsonTimestamp::class.java, BsonTimestampBsonDeserializer)
        addSerializer(MaxKey::class.java, MaxKeyBsonSerializer)
        addDeserializer(MaxKey::class.java, MaxKeyBsonDeserializer)
        addSerializer(MinKey::class.java, MinKeyBsonSerializer)
        addDeserializer(MinKey::class.java, MinKeyBsonDeserializer)
        addSerializer(BigDecimal::class.java, BigDecimalBsonSerializer)
        addDeserializer(BigDecimal::class.java, BigDecimalBsonDeserializer)
        addSerializer(Decimal128::class.java, Decimal128BsonSerializer)
        addDeserializer(Decimal128::class.java, Decimal128BsonDeserializer)

        addSerializer(Id::class.java, IdBsonSerializer)
        addDeserializer(Id::class.java, IdBsonDeserializer)
        addKeySerializer(Id::class.java, IdKeySerializer())
        addKeyDeserializer(Id::class.java, IdKeyDeserializer())

        addSerializer(Instant::class.java, InstantBsonSerializer)
        addSerializer(ZonedDateTime::class.java, ZonedDateTimeBsonSerializer)
        addSerializer(OffsetDateTime::class.java, OffsetDateTimeBsonSerializer)
        addSerializer(LocalDate::class.java, LocalDateBsonSerializer)
        addSerializer(LocalDateTime::class.java, LocalDateTimeBsonSerializer)
        addSerializer(LocalTime::class.java, LocalTimeBsonSerializer)
        addSerializer(OffsetTime::class.java, OffsetTimeBsonSerializer)
        addSerializer(Calendar::class.java, CalendarBsonSerializer)
        addDeserializer(Instant::class.java, InstantBsonDeserializer)
        addDeserializer(ZonedDateTime::class.java, ZonedDateTimeBsonDeserializer)
        addDeserializer(OffsetDateTime::class.java, OffsetDateTimeBsonDeserializer)
        addDeserializer(LocalDate::class.java, LocalDateBsonDeserializer)
        addDeserializer(LocalDateTime::class.java, LocalDateTimeBsonDeserializer)
        addDeserializer(LocalTime::class.java, LocalTimeBsonDeserializer)
        addDeserializer(OffsetTime::class.java, OffsetTimeBsonDeserializer)
        addDeserializer(Calendar::class.java, CalendarBsonDeserializer)
        addSerializer(ZoneId::class.java, ZoneIdBsonSerializer)
        addDeserializer(ZoneId::class.java, ZoneIdBsonDeserializer)
        addDeserializer(Date::class.java, BsonDateDeserializer);

        addSerializer(KProperty::class.java, KPropertySerializer)
    }
}
