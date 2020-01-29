/*
 * Copyright (C) 2016/2020 Litote
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

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

object FriendCodec : Codec<FriendWithCustomCodec> {
    override fun getEncoderClass(): Class<FriendWithCustomCodec> = FriendWithCustomCodec::class.java

    override fun encode(writer: BsonWriter, value: FriendWithCustomCodec, encoderContext: EncoderContext) {
        TODO("not implemented")
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): FriendWithCustomCodec {
        reader.readStartDocument()

        var id: ObjectId? = null
        var name: String? = null
        var address: String? = null
        var coordinate: CoordinateWithCustomCodec? = null
        var gender: Gender? = null

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val field = reader.readName()
            when (field) {
                "_id" -> id = reader.readObjectId()
                "name" -> name = reader.readString()
                "address" -> address = reader.readString()
                "coordinate" -> coordinate = decoderContext.decodeWithChildContext(CoordinateCodec, reader)
                "gender" -> gender = reader.readString()?.let { Gender.valueOf(it) }
                else -> reader.skipValue()
            }
        }

        reader.readEndDocument()
        return FriendWithCustomCodec(id, name, address, coordinate, gender)
    }
}

data class FriendWithCustomCodec(
    @BsonId
    val id: ObjectId? = null,
    val name: String? = null,
    val address: String? = null,
    val coordinate: CoordinateWithCustomCodec? = null,
    val gender: Gender? = null
)

object FriendWithBuddiesCodec : Codec<FriendWithCustomCodecWithBuddies> {
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
        var coordinate: CoordinateWithCustomCodec? = null
        var gender: Gender? = null
        var buddies: List<FriendWithCustomCodecWithBuddies> = emptyList()

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val field = reader.readName()
            when (field) {
                "_id" -> id = reader.readObjectId()
                "name" -> name = reader.readString()
                "address" -> address = reader.readString()
                "coordinate" -> coordinate = decoderContext.decodeWithChildContext(CoordinateCodec, reader)
                "gender" -> gender = reader.readString()?.let { Gender.valueOf(it) }
                "buddies" -> {
                    reader.readStartArray()

                    val list = mutableListOf<FriendWithCustomCodecWithBuddies>()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        list.add(decoderContext.decodeWithChildContext(FriendWithBuddiesCodec, reader))
                    }

                    reader.readEndArray()
                    buddies = list
                }
                else -> reader.skipValue()
            }
        }

        reader.readEndDocument()
        return FriendWithCustomCodecWithBuddies(id, name, address, coordinate, gender, buddies)
    }
}

data class FriendWithCustomCodecWithBuddies(
    @BsonId
    val id: ObjectId? = null,
    val name: String? = null,
    val address: String? = null,
    val coordinate: CoordinateWithCustomCodec? = null,
    val gender: Gender? = null,
    val buddies: List<FriendWithCustomCodecWithBuddies> = emptyList()
)

object CoordinateCodec : Codec<CoordinateWithCustomCodec> {
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
}

data class CoordinateWithCustomCodec(val lat: Int, val lng: Int)