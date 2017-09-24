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

import org.bson.Document
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOneById
import org.litote.kmongo.model.Friend
import org.litote.kmongo.save
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertEquals

/**
 *
 */
class Issue20NeedAWayToBypassDocumentValidationOnSave : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun testSaveNullValue() {
        val c = col.withDocumentClass<Document>()
        c.save(Document(mapOf("_id" to 42, "payload" to Document(mapOf("v" to null)))))
        assertEquals(Document(mapOf("_id" to 42, "payload" to Document(mapOf("v" to null)))), c.findOneById(42))
    }
}