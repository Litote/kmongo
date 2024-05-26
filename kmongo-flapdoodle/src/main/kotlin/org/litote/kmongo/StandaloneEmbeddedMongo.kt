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

package org.litote.kmongo

import com.mongodb.ConnectionString
import de.flapdoodle.embed.mongo.commands.MongodArguments
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.embed.process.runtime.Network
import de.flapdoodle.reverse.TransitionWalker
import de.flapdoodle.reverse.transitions.Start
import org.bson.BsonDocument
import org.bson.Document


/**
 *
 */
internal class StandaloneEmbeddedMongo(private val version: IFeatureAwareVersion) {

    private val port = Network.getFreeServerPort()
    private val immutableNet = Net.builder().bindIp("127.0.0.1").port(port).isIpv6(Network.localhostIsIPv6()).build()

    val mongodProcess: TransitionWalker.ReachedState<RunningMongodProcess> by lazy {
        createInstance()
    }

    fun connectionString(commandExecutor: (String, BsonDocument, (Document?, Throwable?) -> Unit) -> Unit): ConnectionString =
        ConnectionString(
            "mongodb://${mongodProcess.current().serverAddress}"
        )

    private fun createInstance(): TransitionWalker.ReachedState<RunningMongodProcess> {
        val immutableMongodArguments = MongodArguments.defaults()
        return Mongod.instance()
            .withMongodArguments(Start.to(MongodArguments::class.java).initializedWith(immutableMongodArguments))
            .withNet(Start.to(Net::class.java).initializedWith(immutableNet))
            .configLogs()
            .start(version)
            .apply {
                Runtime.getRuntime().addShutdownHook(object : Thread() {
                    override fun run() {
                        close()
                    }
                })
            }
    }
}