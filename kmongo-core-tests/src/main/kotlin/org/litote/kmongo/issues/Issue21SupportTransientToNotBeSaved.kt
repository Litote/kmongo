/*
 * Copyright (C) 2016/2020 Litote
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

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.save
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertFalse

/**
 *
 */
class Issue21SupportTransientToNotBeSaved :
    AllCategoriesKMongoBaseTest<Issue21SupportTransientToNotBeSaved.Activity>() {

    @Serializable
    data class Activity(
        var activity: String,
        @ContextualSerialization
        var reference: Any? = null
    ) {
        @kotlinx.serialization.Transient
        @Transient
        var transactionId: Int = 0
    }

    @Test
    fun testSave() {
        col.save(Activity("a"))
        assertFalse(col.withDocumentClass<Document>().findOne()!!.contains("transactionId"))
    }

}