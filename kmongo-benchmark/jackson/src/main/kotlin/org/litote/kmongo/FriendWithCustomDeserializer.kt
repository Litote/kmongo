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
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

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