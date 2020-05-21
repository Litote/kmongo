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

package org.litote.kmongo.util

import org.bson.UuidRepresentation
import org.junit.Assert
import org.junit.Test
import org.litote.kmongo.util.KMongoJacksonFeature.setUUIDRepresentation
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 *
 */
class KMongoJacksonFeatureTest {

    @BeforeTest
    fun before() {
        KMongoConfiguration.resetConfiguration()
    }

    @AfterTest
    fun after() {
        KMongoConfiguration.resetConfiguration()
    }

    @Test
    fun `setUUIDRepresentation to uuidRepresentation#STANDARD has the expected behaviour `() {
        val testCode = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d0e0f")
        setUUIDRepresentation(UuidRepresentation.STANDARD)

        Assert.assertArrayEquals(
            byteArrayOf(16, 0, 0, 0, 4, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            KMongoConfiguration.bsonMapper.writeValueAsBytes(testCode)
        )

    }
}