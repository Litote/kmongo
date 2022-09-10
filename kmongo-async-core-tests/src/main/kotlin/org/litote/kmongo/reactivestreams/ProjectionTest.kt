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

package org.litote.kmongo.reactivestreams

import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.result.InsertOneResult
import org.junit.Test
import org.litote.kmongo.ascending
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.insertOne
import org.litote.kmongo.model.Coordinate
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class ProjectionTest : KMongoReactiveStreamsBaseTest<Friend>() {

    @Test
    fun `single projection is ok`() {
        col.bulkWrite(
            insertOne(Friend("Joe")),
            insertOne(Friend("Bob"))
        ).listenSingle { _: BulkWriteResult?, _: Throwable? ->

            col.projection(Friend::name, Friend::name eq "Joe")
                .listenList { list, _ ->
                    assertEquals(
                        listOf("Joe"),
                        list
                    )
                }

            col.projection(Friend::name, options = { it.sort(ascending(Friend::name)) })
                .listenList { list, _ ->
                    asyncTest {
                        assertEquals(
                            listOf("Bob", "Joe"),
                            list
                        )
                    }
                }
        }
    }

    @Test
    fun `single multi fields projection is ok`() {
        col.insertOne(Friend("Joe", Coordinate(2, 2))).listenSingle { _: InsertOneResult?, _: Throwable? ->
            col.projection(Friend::coordinate / Coordinate::lat).first().listenSingle { r, _ ->
                asyncTest {
                    assertEquals(
                        2,
                        r
                    )
                }
            }
        }
    }

    @Test
    fun `pair projection is ok`() {
        col.bulkWrite(
            insertOne(Friend("Joe", coordinate = Coordinate(1, 2))),
            insertOne(Friend("Bob", coordinate = Coordinate(3, 4)))
        ).listenSingle { _: BulkWriteResult?, _: Throwable? ->

            col.projection(Friend::name, Friend::coordinate, Friend::name eq "Joe").first().listenSingle { r, _ ->
                asyncTest {
                    assertEquals(
                        "Joe" to Coordinate(1, 2),
                        r
                    )
                }
            }
        }
    }

    @Test
    fun `pair multi fields projection is ok`() {
        col.insertOne(Friend("Joe", Coordinate(1, 2))).listenSingle { _: InsertOneResult?, _: Throwable? ->
            col.projection(
                Friend::coordinate / Coordinate::lat,
                Friend::coordinate / Coordinate::lng
            ).first().listenSingle { r, _ ->
                asyncTest {
                    assertEquals(1, r!!.first)
                    assertEquals(2, r.second)
                }
            }
        }
    }

    @Test
    fun `triple projection is ok`() {
        col.bulkWrite(
            insertOne(Friend("Joe", "Here", coordinate = Coordinate(1, 2), tags = listOf("t1"))),
            insertOne(Friend("Bob", "Here", coordinate = Coordinate(3, 4), tags = listOf("t2")))
        ).listenSingle { _: BulkWriteResult?, _: Throwable? ->

            col.projection(
                Friend::name,
                Friend::coordinate,
                Friend::tags,
                Friend::name eq "Joe"
            ).first().listenSingle { r, _ ->
                asyncTest {
                    assertEquals(
                        Triple("Joe", Coordinate(1, 2), listOf("t1")),
                        r
                    )
                }
            }
        }
    }
}