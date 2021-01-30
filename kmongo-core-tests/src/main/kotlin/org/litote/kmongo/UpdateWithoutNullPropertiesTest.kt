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

import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class UpdateWithoutNullPropertiesTest : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun canPartiallyUpdateWithAnOtherDocumentWithSameIdWithoutNullProperties() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend)
        val preexistingDocument = Friend(friend._id!!, "Johnny")
        col.updateOne("{name:'John'}", preexistingDocument, updateOnlyNotNullProperties = true)
        val r = col.findOne("{name:'Johnny'}")
        assertEquals("Johnny", r!!.name)
        assertEquals("123 Wall Street", r.address)
        assertEquals(friend._id, r._id)
    }

    @Test
    fun canPartiallyUpdateWithANewDocumentWithoutNullProperties() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend)
        val newDocument = Friend("Johnny")
        col.updateOne("{name:'John'}", newDocument, updateOnlyNotNullProperties = true)
        val r = col.findOne("{name:'Johnny'}")
        assertEquals("Johnny", r!!.name)
        assertEquals("123 Wall Street", r.address)
    }
}