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

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Serializable
data class TestData(@SerialName("_id") @Contextual val id: ObjectId = ObjectId(), val value: String)

/**
 *
 */
class Issue354KotlinIdFieldIgnored : AllCategoriesKMongoBaseTest<TestData>() {

    @Test
    fun testSerialization() {
        val test = TestData(value = "Foo")

        col.insertOne(test)
        val test2 = col.findOne()

        assertEquals(test, test2)

        val doc = col.withDocumentClass<Document>().findOne()
        assertEquals(test.id, doc?.get("_id"))
        assertNull(doc?.get("id"))
    }
}