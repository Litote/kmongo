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
import com.mongodb.MongoClientSettings
import com.mongodb.MongoClientSettings.getDefaultCodecRegistry
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.codecs.configuration.CodecRegistry
import org.litote.kmongo.service.ClassMappingType

/**
 * Main object used to create a [MongoClient](https://api.mongodb.com/java/current/com/mongodb/MongoClient.html) instance.
 */
object KMongo {

    /**
     * Creates an instance based on a (single) mongodb node (localhost, default port).
     *
     * @return the mongo client
     */
    fun createClient(): MongoClient = createClient("mongodb://localhost")

    /**
     * Creates a Mongo instance.

     * @param connectionString the connection string
     * @return the mongo client
     */
    fun createClient(connectionString: String): MongoClient = createClient(ConnectionString(connectionString))

    /**
     * Creates a Mongo described by a URI. If only one address is used it will only connect to that node, otherwise it will discover all
     * nodes.

     * @param connectionString the connection uri
     * @return the mongo client
     * @throws MongoException if there is a failure
     */
    fun createClient(connectionString: ConnectionString): MongoClient =
        createClient(
            MongoClientSettings.builder().applyConnectionString(connectionString).build()
        )

    /**
     * Creates a Mongo described by a URI. If only one address is used it will only connect to that node, otherwise it will discover all
     * nodes.

     * @param settings the settings
     * @return the mongo client
     * @throws MongoException if there is a failure
     */
    fun createClient(settings: MongoClientSettings): MongoClient = MongoClients.create(
        MongoClientSettings.builder(settings).codecRegistry(configureRegistry(settings.codecRegistry)).build()
    )

    internal fun configureRegistry(codecRegistry: CodecRegistry = getDefaultCodecRegistry()): CodecRegistry {
        return ClassMappingType.codecRegistry(codecRegistry)
    }
}