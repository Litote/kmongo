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

package org.litote.kmongo

import com.mongodb.MongoClient
import com.mongodb.MongoClient.getDefaultCodecRegistry
import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientURI
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.litote.kmongo.jackson.ObjectMapperFactory.jacksonCodecProvider

/**
 * Main object used to create a [MongoClient] instance.
 */
object KMongo {

    /**
     * Creates an instance based on a (single) mongodb node (localhost, default port).
     *
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     */
    fun mongoClient(objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = mongoClient(ServerAddress(), objectMappingCodecProvider)

    /**
     * Creates a Mongo instance based on a (single) mongodb node.

     * @param host server to connect to in format host[:port]
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     */
    fun mongoClient(host: String, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = mongoClient(ServerAddress(host), objectMappingCodecProvider)

    /**
     * Creates a Mongo instance based on a (single) mongodb node (default port).

     * @param host    server to connect to in format host[:port]
     * @param options default query options
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     */
    fun mongoClient(host: String, options: MongoClientOptions, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = mongoClient(ServerAddress(host), options, objectMappingCodecProvider)

    /**
     * Creates a Mongo instance based on a (single) mongodb node.

     * @param host the database's host address
     * @param port the port on which the database is running
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     */
    fun mongoClient(host: String, port: Int, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = mongoClient(ServerAddress(host, port), objectMappingCodecProvider)

    /**
     * Creates a Mongo instance based on a (single) mongodb node

     * @param addr the database address
     * @param objectMappingCodecProvider the object mapping codec provider
     * @see com.mongodb.ServerAddress
     * @return the mongo client
     */
    fun mongoClient(addr: ServerAddress, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = mongoClient(addr, MongoClientOptions.Builder().build(), objectMappingCodecProvider)

    /**
     * Creates a Mongo instance based on a (single) mongodb node and a list of credentials

     * @param addr            the database address
     * @param credentialsList the list of credentials used to authenticate all connections
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun mongoClient(addr: ServerAddress, credentialsList: List<MongoCredential>, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = mongoClient(addr, credentialsList, MongoClientOptions.Builder().build(), objectMappingCodecProvider)

    /**
     * Creates a Mongo instance based on a (single) mongo node using a given ServerAddress and default options.

     * @param addr    the database address
     * @param options default options
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun mongoClient(addr: ServerAddress, options: MongoClientOptions, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = MongoClient(addr, createOptions(objectMappingCodecProvider, options))

    /**
     * Creates a Mongo instance based on a (single) mongo node using a given ServerAddress and default options.

     * @param addr            the database address
     * @param credentialsList the list of credentials used to authenticate all connections
     * @param options         default options
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun mongoClient(addr: ServerAddress, credentialsList: List<MongoCredential>, options: MongoClientOptions, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = MongoClient(addr, credentialsList, createOptions(objectMappingCodecProvider, options))

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
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun mongoClient(seeds: List<ServerAddress>, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = mongoClient(seeds, MongoClientOptions.Builder().build(), objectMappingCodecProvider)

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
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun mongoClient(seeds: List<ServerAddress>, credentialsList: List<MongoCredential>, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = mongoClient(seeds, credentialsList, MongoClientOptions.Builder().build(), objectMappingCodecProvider)

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
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun mongoClient(seeds: List<ServerAddress>, options: MongoClientOptions, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = MongoClient(seeds, createOptions(objectMappingCodecProvider, options))

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
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     *
     * @see com.mongodb.ServerAddress
     */
    fun mongoClient(seeds: List<ServerAddress>, credentialsList: List<MongoCredential>, options: MongoClientOptions, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = MongoClient(seeds, credentialsList, createOptions(objectMappingCodecProvider, options))

    /**
     * Creates a Mongo described by a URI. If only one address is used it will only connect to that node, otherwise it will discover all
     * nodes.

     * @param uri the URI
     * @param objectMappingCodecProvider the object mapping codec provider
     * @return the mongo client
     * @throws MongoException if theres a failure
     */
    fun mongoClient(uri: MongoClientURI, objectMappingCodecProvider: CodecProvider = jacksonCodecProvider): MongoClient
            = MongoClient(MongoClientURI(uri.uri,
            MongoClientOptions.builder(uri.options)
                    .codecRegistry(createRegistry(objectMappingCodecProvider, uri.options.codecRegistry))))

    private fun createOptions(objectMappingCodecProvider: CodecProvider, clientOptions: MongoClientOptions): MongoClientOptions
            = MongoClientOptions.builder(clientOptions).codecRegistry(
            createRegistry(objectMappingCodecProvider, clientOptions.codecRegistry)).build()

    private fun createRegistry(objectMappingCodecProvider: CodecProvider, codecRegistry: CodecRegistry = getDefaultCodecRegistry())
            = fromRegistries(codecRegistry, fromProviders(objectMappingCodecProvider))
}