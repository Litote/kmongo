/*
 * Copyright (C) 2016/2022 Litote
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
import java.io.Closeable
import java.util.ServiceLoader

/**
 * Provides a MongoClient class. Common interface for sync and async driver.
 * This object is mainly used for tests.
 */
object MongoClientProvider {

    /**
     * Create a new client with the given connection string.
     *
     * @param connectionString the settings
     * @return the client
     */
    inline fun <reified T : Closeable> createMongoClient(connectionString: ConnectionString): T {
        return ServiceLoader
            .load(MongoClientProviderService::class.java)
            .map { it.createMongoClient(connectionString) }
            .filterIsInstance<T>()
            .first()
    }

}