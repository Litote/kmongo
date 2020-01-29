/*
 * Copyright (C) 2016/2020 Litote
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
import com.mongodb.MongoClient
import com.mongodb.MongoClient.getDefaultCodecRegistry
import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientURI
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import org.bson.codecs.configuration.CodecRegistry
import org.litote.kmongo.service.ClassMappingType

/**
 * Main object used to create a [MongoClient](https://api.mongodb.com/java/current/com/mongodb/MongoClient.html) instance.
 */
object KMongo {

    /**
     * Creates an instance based on a (single) mongodb node (localhost, default port).
     *
     * @return the mongo client
     */
    fun createClient(): MongoClient = createClient(ServerAddress())

    /**
     * Creates a Mongo instance based on a (single) mongodb node.

     * @param host server to connect to in format host(:port)
     * @return the mongo client
     */
    fun createClient(host: String): MongoClient = createClient(ServerAddress(host))

    /**
     * Creates a Mongo instance based on a (single) mongodb node (default port).

     * @param host    server to connect to in format host(:port)
     * @param options default query options
     * @return the mongo client
     */
    fun createClient(host: String, options: MongoClientOptions): MongoClient =
        createClient(ServerAddress(host), options)

    /**
     * Creates a Mongo instance based on a (single) mongodb node.

     * @param host the database's host address
     * @param port the port on which the database is running
     * @return the mongo client
     */
    fun createClient(host: String, port: Int): MongoClient = createClient(ServerAddress(host, port))

    /**
     * Creates a Mongo instance based on a (single) mongodb node.

     * @param connectionString the settings
     * @return the mongo client
     */
    fun createClient(connectionString: ConnectionString): MongoClient =
        createClient(MongoClientURI(connectionString.toString()))

    /**
     * Creates a Mongo instance based on a (single) mongodb node

     * @param addr the database address
     * @see com.mongodb.ServerAddress
     * @return the mongo client
     */
    fun createClient(addr: ServerAddress): MongoClient = createClient(addr, MongoClientOptions.Builder().build())

    /**
     * Creates a Mongo instance based on a (single) mongodb node and a list of credentials

     * @param addr            the database address
     * @param credentialsList the list of credentials used to authenticate all connections
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun createClient(addr: ServerAddress, credentialsList: List<MongoCredential>): MongoClient =
        createClient(addr, credentialsList, MongoClientOptions.Builder().build())

    /**
     * Creates a Mongo instance based on a (single) mongo node using a given ServerAddress and default options.

     * @param addr    the database address
     * @param options default options
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun createClient(addr: ServerAddress, options: MongoClientOptions): MongoClient =
        MongoClient(addr, configureOptions(options))

    /**
     * Creates a Mongo instance based on a (single) mongo node using a given ServerAddress and default options.

     * @param addr            the database address
     * @param credentialsList the list of credentials used to authenticate all connections
     * @param options         default options
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun createClient(
        addr: ServerAddress,
        credentialsList: List<MongoCredential>,
        options: MongoClientOptions
    ): MongoClient = MongoClient(addr, credentialsList, configureOptions(options))

    /**
     *
     * Creates a Mongo based on a list of replica set members or a list of mongos. It will find all members (the master will be used by
     * default). If you pass in a single server in the list, the driver will still function as if it is a replica set. If you have a
     * standalone server, use the Mongo(ServerAddress) constructor.

     *
     * If this is a list of mongos servers, it will pick the closest (lowest ping time) one to send all requests to, and automatically
     * fail over to the next server if the closest is down.

     * @param seeds Put as many servers as you can in the list and the system will figure out the rest.  This can either be a list of mongod
     *              servers in the same replica set or a list of mongos servers in the same sharded cluster.
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun createClient(seeds: List<ServerAddress>): MongoClient =
        createClient(seeds, MongoClientOptions.Builder().build())

    /**
     *
     * Creates a Mongo based on a list of replica set members or a list of mongos. It will find all members (the master will be used by
     * default). If you pass in a single server in the list, the driver will still function as if it is a replica set. If you have a
     * standalone server, use the Mongo(ServerAddress) constructor.

     *
     * If this is a list of mongos servers, it will pick the closest (lowest ping time) one to send all requests to, and automatically
     * fail over to the next server if the closest is down.

     * @param seeds           Put as many servers as you can in the list and the system will figure out the rest.  This can either be a list
     *                        of mongod servers in the same replica set or a list of mongos servers in the same sharded cluster.
     *
     * @param credentialsList the list of credentials used to authenticate all connections
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun createClient(seeds: List<ServerAddress>, credentialsList: List<MongoCredential>): MongoClient =
        createClient(seeds, credentialsList, MongoClientOptions.Builder().build())

    /**
     *
     * Creates a Mongo based on a list of replica set members or a list of mongos. It will find all members (the master will be used by
     * default). If you pass in a single server in the list, the driver will still function as if it is a replica set. If you have a
     * standalone server, use the Mongo(ServerAddress) constructor.

     *
     * If this is a list of mongos servers, it will pick the closest (lowest ping time) one to send all requests to, and automatically
     * fail over to the next server if the closest is down.

     * @param seeds   Put as many servers as you can in the list and the system will figure out the rest.  This can either be a list of
     *                mongod servers in the same replica set or a list of mongos servers in the same sharded cluster.
     *
     * @param options default options
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun createClient(seeds: List<ServerAddress>, options: MongoClientOptions): MongoClient =
        MongoClient(seeds, configureOptions(options))

    /**
     *
     * Creates a Mongo based on a list of replica set members or a list of mongos. It will find all members (the master will be used by
     * default). If you pass in a single server in the list, the driver will still function as if it is a replica set. If you have a
     * standalone server, use the Mongo(ServerAddress) constructor.

     *
     * If this is a list of mongos servers, it will pick the closest (lowest ping time) one to send all requests to, and automatically
     * fail over to the next server if the closest is down.

     * @param seeds           Put as many servers as you can in the list and the system will figure out the rest.  This can either be a list
     *                        of mongod servers in the same replica set or a list of mongos servers in the same sharded cluster.
     *
     * @param credentialsList the list of credentials used to authenticate all connections
     * @param options         default options
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun createClient(
        seeds: List<ServerAddress>,
        credentialsList: List<MongoCredential>,
        options: MongoClientOptions
    ): MongoClient = MongoClient(seeds, credentialsList, configureOptions(options))

    /**
     * Creates a Mongo described by a URI. If only one address is used it will only connect to that node, otherwise it will discover all
     * nodes.

     * @param uri the URI
     * @return the mongo client
     * @throws MongoException if theres a failure
     */
    fun createClient(uri: MongoClientURI): MongoClient = MongoClient(
        MongoClientURI(
            uri.uri,
            MongoClientOptions.builder(uri.options).codecRegistry(configureRegistry(uri.options.codecRegistry))
        )
    )

    private fun configureOptions(clientOptions: MongoClientOptions): MongoClientOptions =
        MongoClientOptions.builder(clientOptions).codecRegistry(configureRegistry(clientOptions.codecRegistry)).build()

    internal fun configureRegistry(codecRegistry: CodecRegistry = getDefaultCodecRegistry()): CodecRegistry {
        return ClassMappingType.codecRegistry(codecRegistry)
    }
}