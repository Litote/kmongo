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

package org.litote.kmongo

import com.mongodb.ConnectionString
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.IMongodConfig
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION
import de.flapdoodle.embed.process.runtime.Network
import org.bson.BsonDocument
import org.bson.Document

/**
 *
 */
internal object StandaloneEmbeddedMongo {

    var port = Network.getFreeServerPort()
    var config: IMongodConfig = MongodConfigBuilder()
        .version(PRODUCTION)
        .net(Net(port, Network.localhostIsIPv6()))
        .build()

    val mongodProcess: MongodProcess by lazy {
        createInstance()
    }

    fun connectionString(commandExecutor: (String, BsonDocument, (Document?, Throwable?) -> Unit) -> Unit): ConnectionString =
        ConnectionString(
            "mongodb://${mongodProcess.host}"
        )

    private fun createInstance(): MongodProcess {
        return MongodStarter.getInstance(EmbeddedMongoLog.embeddedConfig).prepare(config).start()
    }
}