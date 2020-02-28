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

package org.litote.kmongo.jackson

import org.bson.BsonDocumentReader
import org.bson.codecs.DecoderContext
import org.junit.Test
import org.litote.kmongo.MongoOperator.numberDecimal
import org.litote.kmongo.MongoOperator.numberDouble
import org.litote.kmongo.MongoOperator.numberLong
import org.litote.kmongo.bson
import org.litote.kmongo.service.ClassMappingType
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.assertEquals

class Issue147OtherTypeSupportForBigDecimal {

    data class Data(val d: BigDecimal)

    @Test
    fun testDeserializingInt() {
        val bson = "{d:1}".bson
        val data = ClassMappingType.coreCodecRegistry().get(Data::class.java).decode(
            BsonDocumentReader(bson), DecoderContext.builder().build()
        )
        assertEquals(1.toBigDecimal(), data.d)
    }

    @Test
    fun testDeserializingLong() {
        val bson = "{d:{$numberLong:\"1\"}}".bson
        val data = ClassMappingType.coreCodecRegistry().get(Data::class.java).decode(
            BsonDocumentReader(bson), DecoderContext.builder().build()
        )
        assertEquals(1.toBigDecimal(), data.d)
    }

    @Test
    fun testDeserializingFloat() {
        val bson = "{d:1.3}".bson
        val data = ClassMappingType.coreCodecRegistry().get(Data::class.java).decode(
            BsonDocumentReader(bson), DecoderContext.builder().build()
        )
        assertEquals(1.3.toBigDecimal(), data.d.setScale(1, RoundingMode.FLOOR))
    }

    @Test
    fun testDeserializingDouble() {
        val bson = "{d:{$numberDouble:\"1\"}}".bson
        val data = ClassMappingType.coreCodecRegistry().get(Data::class.java).decode(
            BsonDocumentReader(bson), DecoderContext.builder().build()
        )
        assertEquals(1.toBigDecimal(), data.d)
    }

    @Test
    fun testDeserializingDecimal128() {
        val bson = "{d:{$numberDecimal:\"1\"}}".bson
        val data = ClassMappingType.coreCodecRegistry().get(Data::class.java).decode(
            BsonDocumentReader(bson), DecoderContext.builder().build()
        )
        assertEquals(1.toBigDecimal(), data.d)
    }

    @Test
    fun testDeserializingString() {
        val bson = "{d:\"1.134\"}".bson
        val data = ClassMappingType.coreCodecRegistry().get(Data::class.java).decode(
            BsonDocumentReader(bson), DecoderContext.builder().build()
        )
        assertEquals(1.134.toBigDecimal(), data.d)
    }
}