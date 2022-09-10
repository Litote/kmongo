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

package org.litote.kmongo.reactivestreams

import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import org.litote.kmongo.util.KMongoUtil.defaultCollectionName

//*******
//MongoDatabase extension methods
//*******

/**
 * Returns a [MongoDatabase] with a KMongo codec.
 */
fun MongoDatabase.withKMongo(): MongoDatabase =
    withCodecRegistry(KMongo.configureRegistry(codecRegistry))

/**
 * Gets a collection.
 *
 * @param collectionName the name of the collection to return
 * @param <T>            the default target type of the collection to return
 * @return the collection
 */
inline fun <reified T : Any> MongoDatabase.getCollectionOfName(collectionName: String): MongoCollection<T> =
    getCollection(collectionName, T::class.java)

/**
 * Gets a collection.
 *
 * @param <T>            the default target type of the collection to return
 *                       - the name of the collection is determined by [defaultCollectionName]
 * @return the collection
 * @see defaultCollectionName
 */
inline fun <reified T : Any> MongoDatabase.getCollection(): MongoCollection<T> =
    getCollection(defaultCollectionName(T::class), T::class.java)