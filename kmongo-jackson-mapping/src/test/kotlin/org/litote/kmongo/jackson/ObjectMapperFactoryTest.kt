/*
 * Copyright (C) 2017/2018 Litote
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

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import org.litote.kmongo.util.KMongoConfiguration
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class ObjectMapperFactoryTest {

    data class T(val set:Set<String>, val mutableSet: MutableSet<String>)

    @Test
    fun `Set is deserialized as LinkedHashSet`() {
        val t = KMongoConfiguration.extendedJsonMapper.readValue<T>("""{"set":["b","a"],"mutableSet":["e"]}""")
        assertEquals(setOf("b","a").toList(), t.set.toList())
        assertTrue { t.set is LinkedHashSet }
        assertTrue { t.mutableSet is LinkedHashSet }
    }
}