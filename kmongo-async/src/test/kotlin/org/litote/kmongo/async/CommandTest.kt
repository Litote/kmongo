/*
 * Copyright (C) 2016 Litote
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

import org.bson.Document
import org.junit.Test
import org.litote.kmongo.model.Friend
import org.litote.kmongo.util.KMongoUtil
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class CommandTest : KMongoAsyncBaseTest<Friend>() {

    class LocationResult(val results: List<Location>)

    class Location(var dis: Double = 0.toDouble(), var obj: NestedLocation? = null) {

        val name: String
            get() = obj?.name ?: ""
    }

    class NestedLocation(var name: String? = null)

    @Test
    fun canRunACommand() {
        database.runCommand<Document>("{ ping: 1 }", {
            r, t ->
            asyncTest {
                assertEquals(1.0, r!!.get("ok"))
            }
        })
    }

    @Test
    fun canRunACommandWithParameter() {
        col.insertOne("{test:1}", { r, t ->
            val friends = "friend"
            database.runCommand<Document>("{ count: '$friends' }", { r, t ->
                asyncTest {
                    assertEquals(1, r!!.get("n"))
                }
            })
        })
    }

    @Test
    fun canRunAGeoNearCommand() {
        col.createIndex(KMongoUtil.toBson("{loc:'2d'}"), { r, t ->
            col.insertOne("{loc:{lat:48.690833,lng:9.140556}, name:'Paris'}", { r, t ->
                database.runCommand<LocationResult>("{ geoNear : 'friend', near : [48.690,9.140], spherical: true}", { r, t ->
                    asyncTest {
                        val locations = r!!.results
                        assertEquals(1, locations.size)
                        assertEquals(1.732642945641585E-5, locations.first().dis)
                        assertEquals("Paris", locations.first().name)
                    }
                })
            })
        })
    }

    @Test
    fun canRunAnEmptyResultCommand() {
        col.createIndex(KMongoUtil.toBson("{loc:'2d'}"), { r, t ->
            database.runCommand<LocationResult>("{ geoNear : 'friend', near : [48.690,9.140], spherical: true}", { r, t ->
                asyncTest {
                    assertTrue (r!!.results.isEmpty())
                }
            })
        })
    }

}