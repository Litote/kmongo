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

import kotlinx.serialization.Serializable
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.issues.Issue95NullDeserializedTo0.Task
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class Issue95NullDeserializedTo0 : AllCategoriesKMongoBaseTest<Task>() {

    @Serializable
    data class Task(val int: Int? = null, val double: Double = Double.NaN)

    @Test
    fun generateNullInt() {
        val t1 = Task()
        col.insertOne(t1)
        assertNull(col.findOne()!!.int)
        assertNull(col.withDocumentClass<Document>().findOne()!!["int"])
    }

    @Test
    fun generateNotPresentInt() {
        col.withDocumentClass<Document>().insertOne(Document())
        assertNull(col.findOne()!!.int)
        assertNull(col.withDocumentClass<Document>().findOne()!!["int"])
    }

    @Test
    fun generateNanDouble() {
        col.insertOne(Task())
        assertEquals(Double.NaN, col.findOne()!!.double)
        assertEquals(Double.NaN, col.withDocumentClass<Document>().findOne()!!["double"])
    }
}