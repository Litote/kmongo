package com.github.jershell.kbson

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.modules.serializersModuleOf
import kotlinx.serialization.withName
import org.bson.BsonType
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.util.Date

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class NonEncodeNull


@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName("DateSerializer")

    override fun serialize(encoder: Encoder, obj: Date) {
        encoder as BsonEncoder
        encoder.encodeDateTime(obj.time)
    }

    override fun deserialize(decoder: Decoder): Date {
        return when (decoder) {
            is FlexibleDecoder -> {
                Date(
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


@Serializer(forClass = BigDecimal::class)
object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("BigDecimalSerializer")

    override fun serialize(encoder: Encoder, obj: BigDecimal) {
        encoder as BsonEncoder
        encoder.encodeDecimal128(Decimal128(obj))
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return when (decoder) {
            is FlexibleDecoder -> {
                when (decoder.reader.currentBsonType) {
                    BsonType.STRING -> BigDecimal(decoder.decodeString())
                    BsonType.DECIMAL128 -> decoder.reader.readDecimal128().bigDecimalValue()
                    else -> throw SerializationException("Unsupported ${decoder.reader.currentBsonType} reading decimal128")
                }
            }
            else -> throw SerializationException("Unknown decoder type")
        }
    }
}


@Serializer(forClass = ByteArray::class)
object ByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("ByteArraySerializer")

    override fun serialize(encoder: Encoder, obj: ByteArray) {
        encoder as BsonEncoder
        encoder.encodeByteArray(obj)
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return when (decoder) {
            is FlexibleDecoder -> {
                decoder.reader.readBinaryData().data
            }
            else -> throw SerializationException("Unknown decoder type")
        }

    }
}


@Serializer(forClass = ObjectId::class)
object ObjectIdSerializer : KSerializer<ObjectId> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName("ObjectIdSerializer")

    override fun serialize(encoder: Encoder, obj: ObjectId) {
        encoder as BsonEncoder
        encoder.encodeObjectId(obj)
    }

    override fun deserialize(decoder: Decoder): ObjectId {
        return when (decoder) {
            is FlexibleDecoder -> {
                when (decoder.reader.currentBsonType) {
                    BsonType.STRING -> ObjectId(decoder.decodeString())
                    BsonType.OBJECT_ID -> decoder.reader.readObjectId()
                    else -> throw SerializationException("Unsupported ${decoder.reader.currentBsonType} reading object id")
                }
            }
            else -> throw SerializationException("Unknown decoder type")
        }

    }
}

val DefaultModule = serializersModuleOf(
    mapOf(
        ObjectId::class to ObjectIdSerializer,
        BigDecimal::class to BigDecimalSerializer,
        ByteArray::class to ByteArraySerializer,
        Date::class to DateSerializer
    )
)
