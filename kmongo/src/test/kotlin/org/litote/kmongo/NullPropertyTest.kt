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

package org.litote.kmongo

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import junit.framework.Assert.assertFalse
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.NullPropertyTest.Test1

/**
 *
 */
class NullPropertyTest : AllCategoriesKMongoBaseTest<Test1>() {

    data class Test1(@JsonInclude(NON_NULL) val nullableProperty: String? = null)


    @Test
    fun testFindAndUpdate() {
        col.insertOne(Test1())
        assertFalse(col.withDocumentClass<Document>().findOne()!!.containsKey("nullableProperty"))
    }
}