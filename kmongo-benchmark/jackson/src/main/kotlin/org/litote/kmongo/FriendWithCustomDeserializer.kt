/*
 * Copyright (C) 2017 Litote
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

package org.litote.kmongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.litote.kmongo.util.KMongoCodecBase
import org.litote.kmongo.util.KMongoCodecProvider

class FriendDeserializer : JsonDeserializer<FriendWithCustomDeserializer>() {

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): FriendWithCustomDeserializer {
        with(p) {

            var id: ObjectId? = null
            var name: String? = null
            var address: String? = null
            var coordinate: CoordinateWithCustomDeserializer? = null
            var gender: Gender? = null

            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) {
                nextToken()
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                    break
                }
                val fieldName = currentName
                nextToken()
                when (fieldName) {
                    "_id" -> id = p.readValueAs(ObjectId::class.java)
                    "name" -> name = p.text
                    "address" -> address = p.text
                    "coordinate" -> coordinate = p.readValueAs(CoordinateWithCustomDeserializer::class.java)
                    "gender" -> gender = p.text?.let { Gender.valueOf(it) }
                    else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        p.skipChildren()
                    } else {
                        nextToken()
                    }
                }
            }

            nextToken()
            return FriendWithCustomDeserializer(id, name, address, coordinate, gender)
        }
    }
}

/**
 *
 */
@JsonDeserialize(using = FriendDeserializer::class)
data class FriendWithCustomDeserializer(
    @BsonId
    val id: ObjectId? = null,
    val name: String? = null,
    val address: String? = null,
    val coordinate: CoordinateWithCustomDeserializer? = null,
    val gender: Gender? = null
)

class FriendWithBuddiesDeserializer : JsonDeserializer<FriendWithBuddiesWithCustomDeserializer>() {

    companion object {
        val buddiesTypeReference = object : TypeReference<List<FriendWithBuddiesWithCustomDeserializer>>() {}
    }

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): FriendWithBuddiesWithCustomDeserializer {
        with(p) {

            var id: ObjectId? = null
            var name: String? = null
            var address: String? = null
            var coordinate: CoordinateWithCustomDeserializer? = null
            var gender: Gender? = null
            var buddies: List<FriendWithBuddiesWithCustomDeserializer> = emptyList()

            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) {
                nextToken()
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                    break
                }
                val fieldName = currentName
                nextToken()
                when (fieldName) {
                    "_id" -> id = p.readValueAs(ObjectId::class.java)
                    "name" -> name = p.text
                    "address" -> address = p.text
                    "coordinate" -> coordinate = p.readValueAs(CoordinateWithCustomDeserializer::class.java)
                    "gender" -> gender = p.text?.let { Gender.valueOf(it) }
                    "buddies" -> buddies = p.readValueAs(buddiesTypeReference)
                    else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        p.skipChildren()
                    } else {
                        nextToken()
                    }
                }
            }

            return FriendWithBuddiesWithCustomDeserializer(id, name, address, coordinate, gender, buddies)
        }
    }
}

@JsonDeserialize(using = FriendWithBuddiesDeserializer::class)
data class FriendWithBuddiesWithCustomDeserializer(
    @BsonId
    val id: ObjectId? = null,
    val name: String? = null,
    val address: String? = null,
    val coordinate: CoordinateWithCustomDeserializer? = null,
    val gender: Gender? = null,
    val buddies: List<FriendWithBuddiesWithCustomDeserializer> = emptyList()
)

class CoordinateDeserializer : JsonDeserializer<CoordinateWithCustomDeserializer>() {

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): CoordinateWithCustomDeserializer {
        with(p) {

            var lat: Int? = null
            var lng: Int? = null

            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) {
                nextToken()
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                    break
                }
                val fieldName = currentName
                nextToken()
                when (fieldName) {
                    "lat" -> lat = p.intValue
                    "lng" -> lng = p.intValue
                    else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        p.skipChildren()
                    } else {
                        nextToken()
                    }
                }
            }

            return CoordinateWithCustomDeserializer(lat!!, lng!!)
        }
    }
}


@JsonDeserialize(using = CoordinateDeserializer::class)
data class CoordinateWithCustomDeserializer(val lat: Int, val lng: Int)

class FriendWithBuddiesCodec(codecRegistryProvider: () -> (CodecRegistry)) :
    KMongoCodecBase<FriendWithCustomCodecWithBuddies>(codecRegistryProvider) {

    private val coordinateCodec: Codec<CoordinateWithCustomDeserializer>
            by lazy(LazyThreadSafetyMode.NONE) { codecRegistry.get(CoordinateWithCustomDeserializer::class.java) }

    override fun getEncoderClass(): Class<FriendWithCustomCodecWithBuddies> =
        FriendWithCustomCodecWithBuddies::class.java

    override fun encode(writer: BsonWriter, value: FriendWithCustomCodecWithBuddies, encoderContext: EncoderContext) {
        TODO("not implemented")
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): FriendWithCustomCodecWithBuddies {
        reader.readStartDocument()

        var id: ObjectId? = null
        var name: String? = null
        var address: String? = null
        var coordinate: CoordinateWithCustomDeserializer? = null
        var gender: Gender? = null
        var buddies: List<FriendWithCustomCodecWithBuddies>? = null

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val field = reader.readName()
            when (field) {
                "_id" -> id = reader.readObjectId()
                "name" -> name = reader.readString()
                "address" -> address = reader.readString()
                "coordinate" -> coordinate = decodeClass(coordinateCodec, reader, decoderContext)
                "gender" -> gender = reader.readString()?.let { Gender.valueOf(it) }
                "buddies" -> buddies = decodeList(this, reader, decoderContext)
                else -> reader.skipValue()
            }
        }

        reader.readEndDocument()
        return FriendWithCustomCodecWithBuddies(id, name, address, coordinate, gender, buddies ?: emptyList())
    }

    companion object : KMongoCodecProvider<FriendWithCustomCodecWithBuddies> {
        override fun codec(codecRegistryProvider: () -> (CodecRegistry)): Codec<FriendWithCustomCodecWithBuddies> {
            return FriendWithBuddiesCodec(codecRegistryProvider)
        }
    }
}

data class FriendWithCustomCodecWithBuddies(
    @BsonId
    val id: ObjectId? = null,
    val name: String? = null,
    val address: String? = null,
    val coordinate: CoordinateWithCustomDeserializer? = null,
    val gender: Gender? = null,
    val buddies: List<FriendWithCustomCodecWithBuddies> = emptyList()
)

class CoordinateCodec(codecRegistryProvider: () -> (CodecRegistry)) :
    KMongoCodecBase<CoordinateWithCustomCodec>(codecRegistryProvider) {

    override fun getEncoderClass(): Class<CoordinateWithCustomCodec> = CoordinateWithCustomCodec::class.java

    override fun encode(writer: BsonWriter, value: CoordinateWithCustomCodec, encoderContext: EncoderContext) {
        TODO("not implemented")
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): CoordinateWithCustomCodec {
        reader.readStartDocument()

        var lat: Int? = null
        var lng: Int? = null

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val field = reader.readName()
            when (field) {
                "lat" -> lat = reader.readInt32()
                "lng" -> lng = reader.readInt32()
                else -> reader.skipValue()
            }
        }

        reader.readEndDocument()
        return CoordinateWithCustomCodec(lat!!, lng!!)
    }

    companion object : KMongoCodecProvider<CoordinateWithCustomCodec> {
        override fun codec(codecRegistryProvider: () -> (CodecRegistry)): Codec<CoordinateWithCustomCodec> {
            return CoordinateCodec(codecRegistryProvider)
        }
    }
}

data class CoordinateWithCustomCodec(val lat: Int, val lng: Int)