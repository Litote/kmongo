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

package org.litote.kmongo.service

import com.mongodb.ConnectionString
import java.io.Closeable

/**
 * Provides a MongoClient class using [java.util.ServiceLoader].
 */
interface MongoClientProviderService<T : Closeable> {

    /**
     * Create a new client with the given connection string.
     *
     * @param connectionString the settings
     * @return the client
     */
    fun createMongoClient(connectionString: ConnectionString): T

}