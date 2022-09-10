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

package org.litote.kmongo.rxjava2

import com.mongodb.ReadPreference
import com.mongodb.reactivestreams.client.MongoDatabase
import io.reactivex.Completable
import io.reactivex.Maybe
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.defaultCollectionName

/**
 * Executes the given command in the context of the current database with the given read preference.
 *
 * @param command        the command to be run
 * @param readPreference the {@link com.mongodb.ReadPreference} to be used when executing the command
 * @param <TResult>      the type of the class to use instead of {@code Document}.
 */
inline fun <reified TResult : Any> MongoDatabase.runCommand(
    command: String,
    readPreference: ReadPreference
): Maybe<TResult> {
    return runCommand(KMongoUtil.toBson(command), readPreference, TResult::class.java).maybe()
}

/**
 * Executes the given command in the context of the current database with the given read preference.
 *
 * @param command        the command to be run
 * @param <TResult>      the type of the class to use instead of {@code Document}.
 */
inline fun <reified TResult : Any> MongoDatabase.runCommand(command: String): Maybe<TResult> {
    return runCommand(command, readPreference)
}

/**
 * Drops this collection from the Database.
 *
 * @mongodb.driver.manual reference/command/drop/ Drop Collection
 */
inline fun <reified T : Any> MongoDatabase.dropCollection(): Completable =
    dropCollection(defaultCollectionName(T::class))

/**
 * Drops this collection from the Database.
 *
 * @mongodb.driver.manual reference/command/drop/ Drop Collection
 */
fun MongoDatabase.dropCollection(collectionName: String): Completable {
    return completableResult { getCollection(collectionName).drop() }
}