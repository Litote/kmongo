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

package org.litote.kmongo.issues

import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.issues.Issue70FailToAccessTheLastFieldInADataClass.Account
import org.litote.kmongo.save
import kotlin.test.assertEquals

/**
 *
 */
class Issue70FailToAccessTheLastFieldInADataClass : AllCategoriesKMongoBaseTest<Account>() {

    data class Account(
        var email: String,
        var subscriptionEnds: Long,
        val servers: MutableList<Long> = mutableListOf(),
        var isPatreon: Boolean
    )

    @Test
    fun `findOne test successfully a boolean`() {
        val a = Account("test@test.org", 1, isPatreon = true)
        col.save(a)
        val result = col.findOne(Account::isPatreon eq true)
        assertEquals(a, result)
    }
}