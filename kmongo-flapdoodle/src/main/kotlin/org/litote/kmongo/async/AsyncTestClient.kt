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

import com.mongodb.async.client.MongoClient
import org.litote.kmongo.EmbeddedMongo.mongodProcess
import org.litote.kmongo.service.MongoClientProvider

/**
 *
 */
internal object AsyncTestClient {

    val instance: MongoClient by lazy {
        createClient(mongodProcess.config.net().port)
    }

    private fun createClient(port: Int): MongoClient {
        return MongoClientProvider.createMongoClient<MongoClient>("mongodb://127.0.0.1:$port")
    }
}