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

package org.litote.kmongo.issues

import com.mongodb.client.MongoCollection
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.KMongoBaseTest
import kotlin.test.assertNotNull

/**
 * [Exception when generating _id of private class] https://github.com/Litote/kmongo/issues/1)
 */
class Issue1 {

    private data class PrivateClass(val _id: String? = null)

    private lateinit var col: MongoCollection<PrivateClass>

    @Before
    fun before() {
        KMongoBaseTest.startMongo()
        col = KMongoBaseTest.getCollection(PrivateClass::class)
    }

    @After
    fun after() {
        KMongoBaseTest.dropCollection(PrivateClass::class)
    }

    @Test
    fun testCanGeneratedId() {
        val p = PrivateClass()
        col.insertOne(p)
        assertNotNull(p._id)
    }

}