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
import de.flapdoodle.embed.mongo.config.Storage
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.embed.process.runtime.Network
import de.flapdoodle.reverse.TransitionWalker
import de.flapdoodle.reverse.transitions.Start
import org.bson.BsonArray
import org.bson.BsonBoolean
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.BsonString
import org.bson.Document

/**
 *
 */
internal class ReplicaSetEmbeddedMongo(private val version: IFeatureAwareVersion) {

    private val ports = Network.getFreeServerPorts(Network.getLocalHost(), 3)
    private val storage = Storage.of("kmongo", 5000)

    private val mongodProcesses: List<TransitionWalker.ReachedState<RunningMongodProcess>> by lazy {
        createInstance()
    }

    fun connectionString(commandExecutor: (String, BsonDocument, (Document?, Throwable?) -> Unit) -> Unit): ConnectionString {
        val host = mongodProcesses[0].current().serverAddress.toString()
        val conf = BsonDocument("_id", BsonString("kmongo"))
            .apply {
                put("protocolVersion", BsonInt32(1))
                put("version", BsonInt32(1))
                put(
                    "members",
                    BsonArray(
                        mongodProcesses.mapIndexed { i, p ->
                            val s = BsonDocument("_id", BsonInt32(i))
                            s.put("host", BsonString(p.current().serverAddress.toString()))
                            s
                        })
                )
            }
        val initCommand = BsonDocument("replSetInitiate", conf)
        val reconfigCommand = BsonDocument("replSetReconfig", conf).apply {
            put("force", BsonBoolean(true))
        }


        try {
            fun reconfigCallback(first: Boolean = false): (Document?, Throwable?) -> Unit =
                { _, t ->
                    if (first || t != null) {
                        Thread.sleep(100)
                        commandExecutor.invoke(host, reconfigCommand, reconfigCallback())
                    }
                }
            commandExecutor.invoke(
                host,
                initCommand
            ) { result, t ->
                reconfigCallback(true).invoke(result, t)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mongodProcesses.run {
            ConnectionString(
                "mongodb://${first().current().serverAddress},${get(1).current().serverAddress},${get(2).current().serverAddress}/?replicaSet=kmongo&uuidRepresentation=standard"
            )
        }
    }

    private fun createInstance(): List<TransitionWalker.ReachedState<RunningMongodProcess>> =
        ports.map {
            val immutableMongodArguments =
                MongodArguments.defaults().withUseSmallFiles(true).withUseNoJournal(false).withReplication(storage)
            Mongod.instance()
                .withMongodArguments(Start.to(MongodArguments::class.java).initializedWith(immutableMongodArguments))
                .configLogs()
                .withNet(
                    Start.to(Net::class.java).initializedWith(
                        Net.builder().bindIp("127.0.0.1").port(it).isIpv6(Network.localhostIsIPv6()).build()
                    )
                )
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