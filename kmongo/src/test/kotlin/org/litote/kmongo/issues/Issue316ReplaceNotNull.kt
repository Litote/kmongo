/*
 * Copyright (C) 2016/2021 Litote
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

import org.junit.Test
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.exists
import org.litote.kmongo.json
import org.litote.kmongo.or
import org.litote.kmongo.setValue
import org.litote.kmongo.unset

/**
 *
 */
class Issue316ReplaceNotNull {

    class MyClass(val someId:String, val someOtherId: String, val nestedObject:NestedClass)
    class NestedClass(val yetAnotherId: String)

    @Test
    fun `test insert and load`() {
        val filter = and(
            or(
                MyClass::someId eq "someId",
                MyClass::someOtherId eq "someOtherId",
                MyClass::nestedObject / NestedClass::yetAnotherId eq "yetAnotherId"
            ),
            MyClass::nestedObject exists true
        )
        println(filter.json)
    }

    class Data(val field1:Int, val field2:Int)

    @Test
    fun `test insert and load2`() {
        val filter = combine(unset( Data::field1,), setValue(Data::field2 , 2))
        println(filter.json)
    }

}