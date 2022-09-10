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

package org.litote.kmongo.coroutine

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.excludeId
import org.litote.kmongo.fields
import org.litote.kmongo.findValue
import org.litote.kmongo.include
import org.litote.kmongo.insertOne
import org.litote.kmongo.model.Coordinate
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class ReactiveStreamProjectionTest : KMongoReactiveStreamsCoroutineBaseTest<Friend>() {

    @Serializable
    data class FriendWithNameOnly(val name: String)

    @Test
    fun `projection works as expected`() = runBlocking {
        col.bulkWrite(
            insertOne(Friend("Joe")),
            insertOne(Friend("Bob"))
        )
        val result: Iterable<String> =
            col.withDocumentClass<Document>()
                .find()
                .descendingSort(Friend::name)
                .projection(fields(include(Friend::name), excludeId()))
                .toList()
                .map{ it.getString(Friend::name.name) }

        assertEquals(
            listOf("Joe", "Bob"),
            result
        )
    }

    @Test
    fun `multi fields projection works as expected`() = runBlocking {
        col.insertOne(Friend("Joe", Coordinate(1, 2)))
        val (name, lat, lng) =
            col.withDocumentClass<Document>()
                .find()
                .descendingSort(Friend::name)
                .projection(
                    fields(
                        include(
                            Friend::name,
                            Friend::coordinate / Coordinate::lat,
                            Friend::coordinate / Coordinate::lng
                        ), excludeId()
                    )
                )
                .first()!!
                .let {
                    Triple(
                        it.getString("name"),
                        it.findValue<Int>("coordinate.lat"),
                        it.findValue(Friend::coordinate / Coordinate::lng)
                    )
                }

        assertEquals("Joe", name)
        assertEquals(1, lat)
        assertEquals(2, lng)
    }

    @Test
    fun `projection without mandatory field is ok if not retrieved in projection`()= runBlocking {
        col.bulkWrite(
            insertOne(Friend("Joe")),
            insertOne(Friend("Bob"))
        )
        val result: Iterable<String> =
            col.withDocumentClass<FriendWithNameOnly>()
                .find()
                .descendingSort(FriendWithNameOnly::name)
                .projection(FriendWithNameOnly::name)
                .toList()
                .map { it.name }

        assertEquals(
            listOf("Joe", "Bob"),
            result
        )
    }

    @Test
    fun `single projection is ok`() = runBlocking {
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
    fun `single multi fields projection is ok`() = runBlocking {
        col.insertOne(Friend("Joe", Coordinate(2, 2)))
        val result: Int? = col.projection(Friend::coordinate / Coordinate::lat).first()

        assertEquals(
            2,
            result
        )
    }

    @Test
    fun `pair projection is ok`() = runBlocking {
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
    fun `pair multi fields projection is ok`() = runBlocking {
        col.insertOne(Friend("Joe", Coordinate(1, 2)))
        val (lat, lng) = col.projection(
            Friend::coordinate / Coordinate::lat,
            Friend::coordinate / Coordinate::lng
        ).first()!!

        assertEquals(1, lat)
        assertEquals(2, lng)
    }

    @Test
    fun `triple projection is ok`() = runBlocking {
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