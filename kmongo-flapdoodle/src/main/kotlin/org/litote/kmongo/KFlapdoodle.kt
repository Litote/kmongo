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

package org.litote.kmongo

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KFlapdoodle.mongoClient
import org.litote.kmongo.service.MongoClientProvider

/**
 * Main KFlapoodle object - to access sync [mongoClient].
 */
object KFlapdoodle {

    val mongoClient: MongoClient by lazy {
        MongoClientProvider.createMongoClient(connectionString)
    }

    val connectionString : ConnectionString =  EmbeddedMongo.connectionString { host, command, callback ->
        try {
            callback(
                MongoClientProvider
                    .createMongoClient<MongoClient>(ConnectionString("mongodb://$host"))
                    .getDatabase("admin")
                    .runCommand(command),
                null
            )
        } catch (e: Exception) {
            callback(null, e)
        }
    }

    fun getDatabase(dbName: String = "test"): MongoDatabase = mongoClient.getDatabase(dbName)

}