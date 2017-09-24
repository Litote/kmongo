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
import com.mongodb.MongoClient
import com.mongodb.MongoClient.getDefaultCodecRegistry
import com.mongodb.MongoClientOptions
import com.mongodb.ServerAddress
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.configuration.CodecRegistry

/**
 * only used for test
 */
internal class SyncMongoClientProviderService : MongoClientProviderService<MongoClient> {

    private fun codec(): CodecRegistry =
            fromRegistries(getDefaultCodecRegistry(), ClassMappingType.codecRegistry())

    override fun createMongoClient(): MongoClient {
        error("unsupported")
    }

    override fun createMongoClient(connectionString: String): MongoClient {
        return MongoClient(
                ServerAddress(connectionString),
                MongoClientOptions.builder().codecRegistry(codec()).build()
        )
    }

    override fun createMongoClient(connectionString: ConnectionString): MongoClient {
        error("unsupported")
    }
}