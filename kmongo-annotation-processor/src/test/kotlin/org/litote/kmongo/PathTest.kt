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

import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class PathTest {

    @Test
    fun testSimplePropertyPath() {
        assertEquals("date", TestData_.date_.name)
        assertEquals("referenced", TestData_.referenced_.name)
        assertEquals(
            "referenced.pojo2.price",
            TestData_.referenced_.pojo2.price.name
        )
        assertEquals(
            "referenced.pojo.referenced.version",
            TestData_.referenced_.pojo.referenced.version.name
        )
        assertEquals(
            "referenced.subPojo.s",
            TestData_.referenced_.subPojo.s.name
        )

        assertEquals(
            "referenced.subPojo.referenced.version",
            TestData_.referenced_.subPojo.referenced.version.name
        )
        assertEquals(
            "a1",
            SubData2_.a1_.name
        )

        /*
          //TODO
        assertEquals(
        "test",
        SubData2_.test_.name
        )
         */
    }
}