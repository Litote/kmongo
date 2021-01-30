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
import org.litote.kmongo.MongoOperator.ne
import org.litote.kmongo.model.Coordinate
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class DistinctTest : AllCategoriesKMongoBaseTest<Friend>() {

    val wallStreetAvenue = "22 Wall Street Avenue"

    @Test
    fun distinctOnStringEntities() {
        col.insertMany(listOf(Friend("John", wallStreetAvenue),
                Friend("Smith", wallStreetAvenue),
                Friend("Peter", "24 Wall Street Avenue")))
        val l = col.distinct<String>("address").toList()
        assertEquals(2, l.size)
        assertTrue (l.contains(wallStreetAvenue) && l.contains("24 Wall Street Avenue"))
    }

    @Test
    fun distinctOnIntegerEntities() {
        col.insertMany(listOf(Friend("John", Coordinate(1, 2)),
                Friend("Smith", Coordinate(1, 2)),
                Friend("Peter", Coordinate(125, 72))))
        val l = col.distinct<Int>("coordinate.lat").toList()
        assertEquals(2, l.size)
        assertTrue (l.contains(1) && l.contains(125))
    }

    @Test
    fun distinctOnTypedProperty() {
        col.insertMany(listOf(Friend("John", Coordinate(1, 2)),
                Friend("Smith", Coordinate(1, 2)),
                Friend("Peter", Coordinate(125, 72))))
        val l = col.distinct<Coordinate>("coordinate").toList()
        assertEquals(2, l.size)
        assertTrue (l.contains(Coordinate(1, 2)) && l.contains(Coordinate(125, 72)))
    }

    @Test
    fun distinctWithQuery() {
        col.insertMany(listOf(Friend("John", Coordinate(1, 2)),
                Friend("Smith", Coordinate(125, 72)),
                Friend(null, Coordinate(125, 72))))
        val l = col.distinct<Coordinate>("coordinate", "{name:{$ne:'John'}}").toList()
        assertEquals(1, l.size)
        assertEquals (Coordinate(125, 72), l.first())
    }

    @Test
    fun distinctWithParameterizedQuery() {
        col.insertMany(listOf(Friend("John", Coordinate(1, 2)),
                Friend("Peter", Coordinate(3, 4))))
        val l = col.distinct<Coordinate>("coordinate", "{name:'Peter'}").toList()
        assertEquals(1, l.size)
        assertEquals (Coordinate(3, 4), l.first())
    }

}