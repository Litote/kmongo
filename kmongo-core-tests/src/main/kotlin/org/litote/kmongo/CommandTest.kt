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

import kotlinx.serialization.Serializable
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class CommandTest : AllCategoriesKMongoBaseTest<Friend>(oldestMongoTestVersion) {

    @Serializable
    class LocationResult(val results: List<Location>)

    @Serializable
    class Location(var dis: Double = 0.toDouble(), var obj: NestedLocation? = null) {

        val name: String
            get() = obj?.name ?: ""
    }

    @Serializable
    class NestedLocation(var name: String? = null)

    @Test
    fun canRunACommand() {
        val r = database.runCommand<Document>("{ ping: 1 }")
        assertEquals(1.0, r.get("ok"))
    }

    @Test
    fun canRunACommandWithParameter() {
        col.insertOne("{test:1}")
        val friends = "friend"
        val r = database.runCommand<Document>("{ count: '$friends' }")
        assertEquals(1, r.get("n"))
    }

}