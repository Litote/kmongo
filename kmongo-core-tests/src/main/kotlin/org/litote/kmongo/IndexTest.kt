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

package org.litote.kmongo

import com.mongodb.client.model.IndexOptions
import org.junit.Test
import org.litote.kmongo.model.Friend
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty
import kotlin.test.assertEquals

/**
 *
 */
class IndexTest : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun `ensureIndex works even if an incompatible index is already present`() {
        //first add a ttl index
        col.ensureIndex(Friend::creationDate, indexOptions = IndexOptions().expireAfter(1, TimeUnit.DAYS))
        //then create the same index with an other ttl
        col.ensureIndex(Friend::creationDate, indexOptions = IndexOptions().expireAfter(50, TimeUnit.SECONDS))
        //ensure
        assertEquals(
            50,
            col.listIndexes().first { (it.getString("name") == "creationDate_1") }.getLong("expireAfterSeconds")
        )
    }

    @Test
    fun `orderBy index compiles as expected`() {
        col.ensureIndex(orderBy(mapOf(Friend::name to true, Friend::tags to false)))
    }
}