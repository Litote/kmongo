/*
 * Copyright (C) 2017/2018 Litote
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

package org.litote.kmongo.coroutine

import com.mongodb.ReadPreference
import com.mongodb.reactivestreams.client.MongoDatabase
import com.mongodb.reactivestreams.client.Success
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.defaultCollectionName

/**
 * Executes the given command in the context of the current database with the given read preference.
 *
 * @param command        the command to be run
 * @param newReadPreference the {@link com.mongodb.ReadPreference} to be used when executing the command
 * @param <TResult>      the type of the class to use instead of {@code Document}.
 */
suspend inline fun <reified TResult : Any> MongoDatabase.runCommand(
    command: String,
    newReadPreference: ReadPreference = readPreference
): TResult? = runCommand(KMongoUtil.toBson(command), newReadPreference, TResult::class.java).awaitFirstOrNull()

/**
 * Drops this collection from the Database.
 *
 * @mongodb.driver.manual reference/command/drop/ Drop Collection
 */
suspend inline fun <reified T : Any> MongoDatabase.dropCollection(): Success =
    dropCollection(defaultCollectionName(T::class))

/**
 * Drops this collection from the Database.
 *
 * @mongodb.driver.manual reference/command/drop/ Drop Collection
 */
suspend fun MongoDatabase.dropCollection(collectionName: String): Success =
    getCollection(collectionName).drop().awaitSingle()