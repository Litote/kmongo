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
import com.mongodb.async.client.MongoClient
import com.mongodb.async.client.MongoClientSettings
import com.mongodb.async.client.MongoClients
import com.mongodb.connection.ClusterSettings
import com.mongodb.connection.ConnectionPoolSettings
import com.mongodb.connection.ServerSettings
import com.mongodb.connection.SocketSettings
import com.mongodb.connection.SslSettings
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.litote.kmongo.jackson.JacksonCodecProvider
import org.litote.kmongo.jackson.ObjectMapperFactory

/**
 *
 */
object KMongo {

    fun jacksonCodecProvider(): CodecProvider
            = JacksonCodecProvider(ObjectMapperFactory.createBsonObjectMapper())

    /**
     * Creates a new client with the default connection string "mongodb://localhost".

     * @return the client
     */
    fun createClient(objectMappingCodecProvider: CodecProvider = jacksonCodecProvider()): MongoClient
            = createClient(ConnectionString("mongodb://localhost"), objectMappingCodecProvider)

    /**
     * Create a new client with the given client settings.

     * @param settings the settings
     * *
     * @return the client
     */
    fun createClient(settings: MongoClientSettings, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider()): MongoClient {
        val codecRegistry = CodecRegistries.fromRegistries(
                MongoClients.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(objectMappingCodecProvider))
        return MongoClients.create(
                MongoClientSettings.builder(settings)
                        .codecRegistry(codecRegistry).build())
    }

    /**
     * Create a new client with the given connection string.

     * @param connectionString the connection
     * *
     * @return the client
     */
    fun createClient(connectionString: String, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider()): MongoClient
            = createClient(ConnectionString(connectionString), objectMappingCodecProvider)

    /**
     * Create a new client with the given connection string.

     * @param connectionString the settings
     * *
     * @return the client
     */
    fun createClient(connectionString: ConnectionString, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider()): MongoClient {
        return createClient(MongoClientSettings.builder()
                .clusterSettings(ClusterSettings.builder().applyConnectionString(connectionString).build())
                .connectionPoolSettings(ConnectionPoolSettings.builder().applyConnectionString(connectionString).build())
                .serverSettings(ServerSettings.builder().build())
                .credentialList(connectionString.credentialList)
                .sslSettings(SslSettings.builder().applyConnectionString(connectionString).build())
                .socketSettings(SocketSettings.builder().applyConnectionString(connectionString).build()).build(),
                objectMappingCodecProvider)
    }
}