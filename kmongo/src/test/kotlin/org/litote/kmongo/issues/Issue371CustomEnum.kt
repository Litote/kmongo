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

package org.litote.kmongo.issues

import com.fasterxml.jackson.annotation.JsonValue
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.eq
import org.litote.kmongo.from
import org.litote.kmongo.save
import kotlin.test.assertEquals

data class Stop(val type: StopType)

enum class StopType(@JsonValue val value: Int) {
    STATION(1),
    // ...
}

/**
 *
 */
class Issue371CustomEnum :
    AllCategoriesKMongoBaseTest<Stop>() {

    @Test
    fun `serialization and deserialization is ok`() {
        val s = Stop(StopType.STATION)
        col.save(s)
        val l = col.find(Stop::type from StopType.STATION.value).toList()
        assertEquals(listOf(s), l)
    }

    @Test
    fun `serialization and deserialization is really ok`() {
        val s = Stop(StopType.STATION)
        col.save(s)
        val l = col.find(Stop::type eq StopType.STATION).toList()
        assertEquals(listOf(s), l)
    }
}