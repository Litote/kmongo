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

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.save
import java.util.Collections
import kotlin.test.assertEquals

/**
 *
 */
class Issue136WriteObjectRef : AllCategoriesKMongoBaseTest<Issue136WriteObjectRef.Data>() {

    class Data(val entries: List<Entry>)

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "uri")
    class Entry() {

        constructor(uri: String) : this() {
            this.uri = uri
        }

        lateinit var uri: String

        @JsonIdentityReference(alwaysAsId = true)
        var contains: MutableSet<Entry> = Collections.synchronizedSet(HashSet())

    }

    @Test
    fun saveWithWriteObjectRefDoesNotFail() {
        val entry = Entry("http://litote.org/kmongo")
        col.save(Data(listOf(entry, entry)))
        val data2 = col.findOne()
        assertEquals(2, data2!!.entries.size)
        assertEquals(entry.uri, data2.entries.first().uri)
        assertEquals(entry.uri, data2.entries.last().uri)
    }
}