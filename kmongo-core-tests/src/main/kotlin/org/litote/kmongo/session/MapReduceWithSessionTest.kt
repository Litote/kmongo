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

package org.litote.kmongo.session

import com.mongodb.client.ClientSession
import kotlinx.serialization.Serializable
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.mapReduce
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class MapReduceWithSessionTest : AllCategoriesKMongoBaseTest<Friend>() {

    lateinit var session: ClientSession

    @Before
    fun setup() {
        session = mongoClient.startSession()
    }

    @After
    fun tearDown() {
        session.close()
    }

    @Serializable
    data class KeyValue(val _id: String, val value: Double)

    @Test
    fun `mapReduce works as expected`() {
        col.insertMany(session, listOf(Friend("John"), Friend("Ernesto")))
        val sumNameLength = col.mapReduce<KeyValue>(
            session,
            """
                function() {
                       emit("name", this.name.length);
                   };
            """,
            """
                 function(name, l) {
                          return Array.sum(l);
                      };
                """
        ).first()

        assertEquals(11.0, sumNameLength?.value)
    }

}