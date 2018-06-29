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

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.async.client.MongoClient
import com.mongodb.async.client.MongoClients
import org.litote.kmongo.service.ClassMappingType

/**
 *  Main object used to create a [MongoClient](https://api.mongodb.com/java/current/com/mongodb/async/client/MongoClient.html) instance.
 */
object KMongo {

    /**
     * Creates a new client with the default connection string "mongodb://localhost".
     *
     * @return the client
     */
    fun createClient(): MongoClient = createClient(ConnectionString("mongodb://localhost"))

    /**
     * Create a new client with the given client settings.
     *
     * @param settings the settings
     * @return the client
     */
    fun createClient(settings: MongoClientSettings): MongoClient {
        val codecRegistry = ClassMappingType.codecRegistry(settings.codecRegistry)
        return MongoClients.create(
            MongoClientSettings.builder(settings)
                .codecRegistry(codecRegistry).build()
        )
    }

    /**
     * Create a new client with the given connection string.
     *
     * @param connectionString the connection
     * @return the client
     */
    fun createClient(connectionString: String): MongoClient = createClient(ConnectionString(connectionString))

    /**
     * Create a new client with the given connection string.
     *
     * @param connectionString the settings
     * @return the client
     */
    fun createClient(connectionString: ConnectionString): MongoClient {
        return createClient(
            MongoClientSettings
                .builder()
                .applyConnectionString(connectionString)
                .build()
        )
    }
}