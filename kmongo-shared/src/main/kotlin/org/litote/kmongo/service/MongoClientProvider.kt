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

package org.litote.kmongo.service

import com.mongodb.ConnectionString
import java.util.ServiceLoader

/**
 * Provides a MongoClient class. Common interface for sync and async driver.
 */
object MongoClientProvider {

    private val mongoClientProvider
            by lazy {
                ServiceLoader.load(MongoClientProviderService::class.java).iterator().next()
            }

    /**
     * Creates a new client with the default connection string "mongodb://localhost".
     *
     * @return the client
     */
    fun <T> createMongoClient(): T {
        @Suppress("UNCHECKED_CAST")
        return mongoClientProvider.createMongoClient() as T
    }

    /**
     * Create a new client with the given connection string.
     *
     * @param connectionString the connection
     * @return the client
     */
    fun <T> createMongoClient(connectionString: String): T {
        @Suppress("UNCHECKED_CAST")
        return mongoClientProvider.createMongoClient(connectionString) as T
    }

    /**
     * Create a new client with the given connection string.
     *
     * @param connectionString the settings
     * @return the client
     */
    fun <T> createMongoClient(connectionString: ConnectionString): T {
        @Suppress("UNCHECKED_CAST")
        return mongoClientProvider.createMongoClient(connectionString) as T
    }
}