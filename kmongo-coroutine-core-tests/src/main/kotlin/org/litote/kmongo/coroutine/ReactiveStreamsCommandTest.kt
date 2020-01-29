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

package org.litote.kmongo.coroutine

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class ReactiveStreamsCommandTest : KMongoReactiveStreamsCoroutineBaseTest<Friend>() {

    @Serializable
    class LocationResult(val results: List<Location>)

    @Serializable
    class Location(var dis: Double = 0.toDouble(), var obj: NestedLocation? = null) {

        val name: String
            get() = obj?.name ?: ""
    }

    @Serializable
    class NestedLocation(var name: String? = null)

    @Test
    fun canRunACommand() = runBlocking {
        val document = database.runCommand<Document>("{ ping: 1 }") ?: throw AssertionError("Document must not null!")
        assertEquals(1.0, document["ok"])
    }

    @Test
    fun canRunACommandWithParameter() = runBlocking {
        col.insertOne("{test:1}")

        val friends = "friend"

        val document = database.runCommand<Document>("{ count: '$friends' }") ?: throw AssertionError("Document must not null!")
        assertEquals(1, document.get("n"))
    }

    @Test
    fun canRunAGeoNearCommand() = runBlocking {
        col.createIndex("{loc:'2d'}")
        col.insertOne("{loc:{lat:48.690833,lng:9.140556}, name:'Paris'}")
        val document = database.runCommand<LocationResult>(
            "{ geoNear : 'friend', near : [48.690,9.140], spherical: true}"
        ) ?: throw AssertionError("Document must not null!")

        val locations = document.results
        assertEquals(1, locations.size)
        assertEquals(1.732642945641585E-5, locations.first().dis)
        assertEquals("Paris", locations.first().name)
    }

    @Test
    fun canRunAnEmptyResultCommand() = runBlocking {
        col.createIndex("{loc:'2d'}")

        val document = database.runCommand<LocationResult>(
            "{ geoNear : 'friend', near : [48.690,9.140], spherical: true}"
        ) ?: throw AssertionError("Document must not null!")

        assertTrue(document.results.isEmpty())
    }

}