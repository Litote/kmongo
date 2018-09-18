/*
 * Copyright (C) 2017 Litote
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

import org.bson.Document
import org.litote.kmongo.model.Friend
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class ProjectionTest : AllCategoriesKMongoBaseTest<Friend>() {

    //TODO
    //projectionWithoutId
    //Document extension methods
    //singleProjection

    @Test
    fun `projection works as expected`() {
        col.bulkWrite(
            insertOne(Friend("Joe")),
            insertOne(Friend("Bob"))
        )
        val result: Iterable<String> =
            col.withDocumentClass<Document>()
                .find()
                .descendingSort(Friend::name)
                .projection(fields(include(Friend::name), excludeId()))
                .map { it.getString(Friend::name.name) }
                .toList()

        assertEquals(
            listOf("Joe", "Bob"),
            result
        )
    }
}