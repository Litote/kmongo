/*
 * Copyright (C) 2016 Litote
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

import com.mongodb.client.model.Filters
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class FlapdoodleRuleTest {

    data class Friend(val name: String, val _id: String? = null)

    @Rule @JvmField
    val rule = FlapdoodleRule.rule<Friend>(true)

    @Test
    fun testRandomRule() {
        val friend = Friend("bob")
        rule.col.insertOne(friend)
        assertEquals(friend, rule.col.findOneAndDelete(Filters.eq("_id", friend._id)))
    }
}