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

import com.mongodb.client.MongoClient
import com.mongodb.client.model.Filters
import org.junit.Test
import org.litote.kmongo.model.Friend
import org.litote.kmongo.service.MongoClientProvider
import kotlin.test.assertEquals

/**
 *
 */
class StandaloneEmbeddedMongoTest {

    @Test
    fun testStandalone() {
        val friend = Friend("bob")
        val mongoClient: MongoClient = MongoClientProvider.createMongoClient(
            StandaloneEmbeddedMongo(defaultMongoTestVersion).connectionString { _, _, _ -> }
        )
        val col = mongoClient.getDatabase("test").getCollection("friend", Friend::class.java)
        col.insertOne(friend)
        assertEquals(friend, col.findOneAndDelete(Filters.eq("_id", friend._id!!)))
    }
}