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
import org.litote.kmongo.model.Coordinate
import org.litote.kmongo.model.Friend
import org.litote.kmongo.util.SingleProjection
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

    data class FriendWithNameOnly(val name: String)

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

    @Test
    fun `projection without mandatory field is ok if not retrieved in projection`() {
        col.bulkWrite(
            insertOne(Friend("Joe")),
            insertOne(Friend("Bob"))
        )
        val result: Iterable<String> =
            col.withDocumentClass<FriendWithNameOnly>()
                .find()
                .descendingSort(FriendWithNameOnly::name)
                .projection(FriendWithNameOnly::name)
                .map { it.name }
                .toList()

        assertEquals(
            listOf("Joe", "Bob"),
            result
        )
    }

    @Test
    fun `single projection is ok`() {
        col.bulkWrite(
            insertOne(Friend("Joe")),
            insertOne(Friend("Bob"))
        )
        val result: List<String?> =
            col.projection(Friend::name, Friend::name eq "Joe")
                .toList()

        assertEquals(
            listOf("Joe"),
            result
        )

        val result2: List<String?> =
            col.projection(Friend::name, options = { it.ascendingSort(Friend::name) })
                .toList()

        assertEquals(
            listOf("Bob", "Joe"),
            result2
        )

    }

    @Test
    fun `pair projection is ok`() {
        col.bulkWrite(
            insertOne(Friend("Joe", coordinate = Coordinate(1, 2))),
            insertOne(Friend("Bob", coordinate = Coordinate(3, 4)))
        )
        val result: Pair<String?, Coordinate?>? =
            col.projection(Friend::name, Friend::coordinate, Friend::name eq "Joe").first()

        assertEquals(
            "Joe" to Coordinate(1, 2),
            result
        )
    }

    @Test
    fun `triple projection is ok`() {
        col.bulkWrite(
            insertOne(Friend("Joe", "Here", coordinate = Coordinate(1, 2), tags = listOf("t1"))),
            insertOne(Friend("Bob", "Here", coordinate = Coordinate(3, 4), tags = listOf("t2")))
        )
        val result: Triple<String?, Coordinate?, List<String>?>? =
            col.projection(
                Friend::name,
                Friend::coordinate,
                Friend::tags,
                Friend::name eq "Joe"
            ).first()

        assertEquals(
            Triple("Joe", Coordinate(1, 2), listOf("t1")),
            result
        )
    }
}