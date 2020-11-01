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

package org.litote.kmongo.reactor

import com.mongodb.ReadPreference
import com.mongodb.reactor.client.ReactorMongoDatabase
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.defaultCollectionName
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Executes the given command in the context of the current database with the given read preference.
 *
 * @param command        the command to be run
 * @param readPreference the {@link com.mongodb.ReadPreference} to be used when executing the command
 * @param <TResult>      the type of the class to use instead of {@code Document}.
 */
inline fun <reified TResult : Any> ReactorMongoDatabase.runCommand(
    command: String,
    readPreference: ReadPreference
): Mono<TResult> {
    return runCommand(KMongoUtil.toBson(command), readPreference, TResult::class.java).toMono()
}

/**
 * Executes the given command in the context of the current database with the given read preference.
 *
 * @param command        the command to be run
 * @param <TResult>      the type of the class to use instead of {@code Document}.
 */
inline fun <reified TResult : Any> ReactorMongoDatabase.runCommand(command: String): Mono<TResult> {
    return runCommand(command, readPreference)
}

/**
 * Drops this collection from the Database.
 *
 * @mongodb.driver.manual reference/command/drop/ Drop Collection
 */
inline fun <reified T : Any> ReactorMongoDatabase.dropCollection(): Mono<Void> = dropCollection(defaultCollectionName(T::class))

/**
 * Drops this collection from the Database.
 *
 * @mongodb.driver.manual reference/command/drop/ Drop Collection
 */
fun ReactorMongoDatabase.dropCollection(collectionName: String): Mono<Void> = emptyResult { getCollection(collectionName).drop() }
