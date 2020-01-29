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
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.id.ObjectIdGenerator.newObjectId
import org.litote.kmongo.id.ObjectIdToStringGenerator.newStringId
import org.litote.kmongo.id.StringId
import org.litote.kmongo.id.WrappedObjectId
import org.litote.kmongo.issues.Issue103CantSerializeStringId.Data
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertEquals

/**
 *
 */
class Issue103CantSerializeStringId : AllCategoriesKMongoBaseTest<Data>() {

    @Serializable
    data class Data(@ContextualSerialization val _id: StringId<Data> = newStringId())
    @Serializable
    data class Data2(@ContextualSerialization val _id: WrappedObjectId<Data> = newObjectId())

    @Test
    fun `deserializing directly StringId is ok`() {
        val data = Data()
        col.insertOne(data)
        assertEquals(data, col.findOne())
    }

    @Test
    fun `deserializing directly WrappedObjectId is ok`() {
        val data = Data2()
        val c = col.withDocumentClass<Data2>()
        c.insertOne(data)
        assertEquals(data, c.findOne())
    }
}