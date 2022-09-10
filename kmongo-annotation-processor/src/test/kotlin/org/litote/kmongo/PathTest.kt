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

package org.litote.kmongo

import org.junit.Test
import org.litote.kmongo.model.SimpleReferenced2Data
import org.litote.kmongo.model.SubData2_
import org.litote.kmongo.model.TestData_
import org.litote.kmongo.model.other.SimpleReferencedData
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class PathTest {

    @Test
    fun testSimplePropertyPath() {
        assertEquals("date", TestData_.Date.name)
        assertEquals("referenced", TestData_.Referenced.name)
        assertEquals(
            "referenced.pojo2.price",
            TestData_.Referenced.pojo2.price.name
        )
        assertEquals(
            "set.pojo2.price",
            TestData_.Set.pojo2.price.name
        )
        assertEquals(
            "referenced.pojo.referenced.version",
            TestData_.Referenced.pojo.referenced.version.name
        )
        assertEquals(
            "referenced.subPojo.s",
            TestData_.Referenced.subPojo.s.name
        )

        assertEquals(
            "referenced.subPojo.referenced.version",
            TestData_.Referenced.subPojo.referenced.version.name
        )
        assertEquals(
            "a1",
            SubData2_.A1.name
        )

        assertEquals(
            "set.\$.version",
            TestData_.Set.posOp.version.name
        )
        //check compilation
        if (false) {
            (TestData_.Set.posOp eq SimpleReferencedData()).json
            TestData_.Date eq null
        }

        assertEquals(
            "map.id",
            TestData_.Map.keyProjection("id".toId()).name
        )

        //check compilation
        if (false)
            (TestData_.Map2.keyProjection(Locale.ENGLISH) eq SimpleReferenced2Data()).json

        assertEquals(
            "set.$.labels.zh_CN",
            TestData_.Set.posOp.labels.keyProjection(Locale.CHINA).name
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