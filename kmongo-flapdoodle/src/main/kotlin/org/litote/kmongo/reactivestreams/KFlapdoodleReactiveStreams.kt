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

package org.litote.kmongo.reactivestreams

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClient
import org.litote.kmongo.EmbeddedMongo
import org.litote.kmongo.reactivestreams.KFlapdoodleReactiveStreams.mongoClient
import org.litote.kmongo.service.MongoClientProvider

/**
 * Async main KFlapoodle object - to access async [mongoClient].
 */
object KFlapdoodleReactiveStreams {

    val mongoClient: MongoClient by lazy {
        MongoClientProvider.createMongoClient<MongoClient>(
            EmbeddedMongo.connectionString { host, command, callback ->
                MongoClientProvider
                    .createMongoClient<MongoClient>(ConnectionString("mongodb://$host"))
                    .getDatabase("admin")
                    .runCommand(command)
                    .subscribe(SimpleSubscriber { callback.invoke(null, it) })
            }
        )
    }

}