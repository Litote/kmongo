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

package org.litote.kmongo.rxjava2

import org.junit.Test
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.model.Coordinate
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class ReactiveStreamsDistinctTest : KMongoReactiveStreamsRxBaseTest<Friend>() {

    val wallStreetAvenue = "22 Wall Street Avenue"

    @Test
    fun distinctOnStringEntities() {
        col.insertMany(
            listOf(
                Friend("John", wallStreetAvenue),
                Friend("Smith", wallStreetAvenue),
                Friend("Peter", "24 Wall Street Avenue")
            )
        ).blockingAwait()

        val list = col.distinct<String>("address")
            .toObservable().blockingIterable().toList()

        assertEquals(2, list.size)
        assertTrue(list.contains(wallStreetAvenue) && list.contains("24 Wall Street Avenue"))
    }

    @Test
    fun distinctOnIntegerEntities() {
        col.insertMany(
            listOf(
                Friend("John", Coordinate(1, 2)),
                Friend("Smith", Coordinate(1, 2)),
                Friend("Peter", Coordinate(125, 72))
            )
        ).blockingAwait()

        val list = col.distinct<Int>("coordinate.lat")
            .toObservable().blockingIterable().toList()

        assertEquals(2, list.size)
        assertTrue(list.contains(1) && list.contains(125))
    }

    @Test
    fun distinctOnTypedProperty() {

        col.insertMany(
            listOf(
                Friend("John", Coordinate(1, 2)),
                Friend("Smith", Coordinate(1, 2)),
                Friend("Peter", Coordinate(125, 72))
            )
        ).blockingAwait()

        val list = col.distinct<Coordinate>("coordinate")
            .toObservable().blockingIterable().toList()
        assertEquals(2, list.size)
        assertTrue(list.contains(Coordinate(1, 2)) && list.contains(Coordinate(125, 72)))
    }

    @Test
    fun distinctWithQuery() {
        col.insertMany(
            listOf(
                Friend("John", Coordinate(1, 2)),
                Friend("Smith", Coordinate(125, 72)),
                Friend(null, Coordinate(125, 72))
            )
        ).blockingAwait()

        val list = col.distinct<Coordinate>("coordinate", "{name:{${MongoOperator.ne}:'John'}}")
            .toObservable().blockingIterable().toList()

        assertEquals(1, list.size)
        assertEquals(Coordinate(125, 72), list.first())
    }

    @Test
    fun distinctWithParameterizedQuery() {
        col.insertMany(
            listOf(
                Friend("John", Coordinate(1, 2)),
                Friend("Peter", Coordinate(3, 4))
            )
        ).blockingAwait()
        val list = col.distinct<Coordinate>("coordinate", "{name:'Peter'}")
            .toObservable().blockingIterable().toList()
        assertEquals(1, list.size)
        assertEquals(Coordinate(3, 4), list.first())
    }

}