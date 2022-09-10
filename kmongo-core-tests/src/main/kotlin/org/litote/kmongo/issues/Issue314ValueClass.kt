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

import org.bson.codecs.pojo.annotations.BsonId
import org.junit.Ignore
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.newId
import kotlin.test.assertNotNull

data class Account(@BsonId val id: Id<Account>, val email: EmailAddress)

@JvmInline
value class EmailAddress(val value: String)


/**
 *
 */
class Issue314ValueClass : AllCategoriesKMongoBaseTest<Account>() {

    @Test
    @Ignore
    fun `test insert and load`() {
        col.insertOne(Account(newId(), EmailAddress("a")))
        val doc = col.findOne(Account::email eq EmailAddress("a"))
        assertNotNull(doc)
    }
}