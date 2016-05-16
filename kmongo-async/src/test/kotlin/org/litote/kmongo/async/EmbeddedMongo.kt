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
import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.extract.UserTempNaming
import de.flapdoodle.embed.process.io.NullProcessor
import de.flapdoodle.embed.process.runtime.Network

/**
 *
 */
object EmbeddedMongo {

    val instance : MongoClient by lazy {
        createInstance()
    }

    private fun createInstance(): MongoClient {
        try {
            val mongoD = Command.MongoD
            val port = RandomPortNumberGenerator.pickAvailableRandomEphemeralPortNumber()

            val artifactStoreBuilder = ExtractedArtifactStoreBuilder()
            artifactStoreBuilder.defaults(mongoD)
            artifactStoreBuilder.executableNaming(UserTempNaming())

            val output = NullProcessor()
            val processOutput = ProcessOutput(output, output, output)

            val runtimeConfig = RuntimeConfigBuilder().defaults(mongoD).processOutput(processOutput).artifactStore(artifactStoreBuilder.build()).build()

            val network = Net(port, Network.localhostIsIPv6())
            val mongodConfig = MongodConfigBuilder().version(version).net(network).build()

            MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig).start()

            return createClient(port)

        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize Embedded Mongo instance: " + e, e)
        }

    }

    private fun createClient(port: Int): MongoClient {
        val mongo = KMongo.createClient("mongodb://127.0.0.1:$port")
        //mongo.setWriteConcern(WriteConcern.FSYNC_SAFE)
        return mongo
    }

    private val version: Version.Main
        get() {
            val version = System.getProperty("jongo.test.db.version") ?: return Version.Main.PRODUCTION
            return Version.Main.valueOf("V" + version.replace("\\.".toRegex(), "_"))
        }
}