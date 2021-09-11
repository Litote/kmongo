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

package org.litote.kmongo.issues

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.UuidRepresentation
import org.junit.Test
import org.litote.kmongo.KFlapdoodle
import org.litote.kmongo.KMongo
import org.litote.kmongo.KMongoRootTest
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import java.util.UUID
import kotlin.test.assertEquals

@Serializable
data class UUIDContainer(val _id: @Contextual UUID)

class Issue245UuidRepresentation : KMongoRootTest() {

    @Test
    fun `test insert and load`() {
        lateinit var col: MongoCollection<UUIDContainer>
        try {
            val mongoClient: MongoClient = KMongo.createClient(
                MongoClientSettings.builder().applyConnectionString(KFlapdoodle.connectionString)
                    .uuidRepresentation(UuidRepresentation.STANDARD).build()
            )
            col = mongoClient.getDatabase("test").getCollection()
            val e = UUIDContainer(UUID.randomUUID())
            col.save(e)
            assertEquals(e, col.findOne())
        } finally {
            col.drop()
        }

    }
}