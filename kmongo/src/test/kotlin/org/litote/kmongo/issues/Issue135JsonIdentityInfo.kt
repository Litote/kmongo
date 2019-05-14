/*
 * Copyright (C) 2017/2019 Litote
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
import org.litote.kmongo.issues.Issue135JsonIdentityInfo.Entry
import org.litote.kmongo.save
import java.util.Collections
import kotlin.test.assertEquals

/**
 *
 */
class Issue135JsonIdentityInfo : AllCategoriesKMongoBaseTest<Entry>() {

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "uri")
    class Entry() {

        constructor(uri: String) : this() {
            this.uri = uri
        }

        lateinit var uri: String

        @JsonIdentityReference(alwaysAsId = true)
        var contains: MutableSet<Entry> = Collections.synchronizedSet(HashSet())

    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "uri")
    class OriginalEntry(val uri: String) {
        @JsonIdentityReference(alwaysAsId = true)
        var contains: MutableSet<OriginalEntry> = Collections.synchronizedSet(HashSet())
    }

    @Test
    fun saveDoesNotFail() {
        val entry = Entry("http://litote.org/kmongo")
        col.save(entry)
        assertEquals(entry.uri, col.findOne()?.uri)
    }

}