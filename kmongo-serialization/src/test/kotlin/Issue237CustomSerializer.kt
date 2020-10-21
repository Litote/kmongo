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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import kotlin.test.assertEquals


object PointSerializer : KSerializer<Point> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Point", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Point) {
        encoder.encodeString(value.type)
    }

    override fun deserialize(decoder: Decoder): Point {
        val string = decoder.decodeString()
        return Point(string)
    }
}

@Serializable
data class Address(
    @Serializable(with = PointSerializer::class)
    val location: Point
)

data class Point(val type: String)

/**
 *
 */
class Issue237CustomSerializer : AllCategoriesKMongoBaseTest<Address>() {

    @Test
    fun insertAndLoad() {
        val value = Address(Point("p"))
        col.insertOne(value)
        assertEquals(value, col.findOne())
    }
}