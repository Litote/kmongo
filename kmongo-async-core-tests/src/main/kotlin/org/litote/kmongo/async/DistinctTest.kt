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

package org.litote.kmongo.async

import org.junit.Test
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.model.Coordinate
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class DistinctTest : KMongoAsyncBaseTest<Friend>() {

    val wallStreetAvenue = "22 Wall Street Avenue"

    @Test
    fun distinctOnStringEntities() {
        col.insertMany(listOf(Friend("John", wallStreetAvenue),
                Friend("Smith", wallStreetAvenue),
                Friend("Peter", "24 Wall Street Avenue")), {
            _, _ ->
            col.distinct<String>("address").toList {
                r, _ ->
                asyncTest {
                    assertEquals(2, r!!.size)
                    assertTrue (r.contains(wallStreetAvenue) && r.contains("24 Wall Street Avenue"))
                }
            }
        })
    }

    @Test
    fun distinctOnIntegerEntities() {
        col.insertMany(listOf(Friend("John", Coordinate(1, 2)),
                Friend("Smith", Coordinate(1, 2)),
                Friend("Peter", Coordinate(125, 72))), {
            _, _ ->
            col.distinct<Int>("coordinate.lat").toList {
                r, _ ->
                asyncTest {
                    assertEquals(2, r!!.size)
                    assertTrue (r.contains(1) && r.contains(125))
                }
            }
        })
    }

    @Test
    fun distinctOnTypedProperty() {
        col.insertMany(listOf(Friend("John", Coordinate(1, 2)),
                Friend("Smith", Coordinate(1, 2)),
                Friend("Peter", Coordinate(125, 72))), {
            _, _ ->
            col.distinct<Coordinate>("coordinate").toList {
                r, _ ->
                asyncTest {
                    assertEquals(2, r!!.size)
                    assertTrue (r.contains(Coordinate(1, 2)) && r.contains(Coordinate(125, 72)))
                }
            }
        })
    }

    @Test
    fun distinctWithQuery() {
        col.insertMany(listOf(Friend("John", Coordinate(1, 2)),
                Friend("Smith", Coordinate(125, 72)),
                Friend(null, Coordinate(125, 72))), {
            _, _ ->
            col.distinct<Coordinate>("coordinate", "{name:{${MongoOperator.ne}:'John'}}").toList {
                r, _ ->
                asyncTest {
                    assertEquals(1, r!!.size)
                    assertEquals (Coordinate(125, 72), r.first())
                }
            }
        })
    }

    @Test
    fun distinctWithParameterizedQuery() {
        col.insertMany(listOf(Friend("John", Coordinate(1, 2)),
                Friend("Peter", Coordinate(3, 4))), {
            _, _ ->
            col.distinct<Coordinate>("coordinate", "{name:'Peter'}").toList {
                r, _ ->
                asyncTest {
                    assertEquals(1, r!!.size)
                    assertEquals (Coordinate(3, 4), r.first())
                }
            }
        })
    }

}