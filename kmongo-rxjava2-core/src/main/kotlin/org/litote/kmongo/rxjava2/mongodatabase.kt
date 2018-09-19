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

package org.litote.kmongo.rxjava2

import com.mongodb.ReadPreference
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.CreateViewOptions
import io.reactivex.Completable
import io.reactivex.Maybe
import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.defaultCollectionName


/**
 * Executes the given command in the context of the current database with a read preference of [ReadPreference.primary].

 * @param command     the command to be run
 * @param <TResult>   the type of the class to use instead of `Document`.
 * @return TResult object result of the command
 */
inline fun <reified TResult : Any> MongoDatabase.runCommand(command: Bson): Maybe<TResult> {
    return maybeResult { runCommand(command, TResult::class.java, it) }
}

/**
 * Executes the given command in the context of the current database with the given read preference.

 * @param command        the command to be run
 * @param readPreference the [com.mongodb.ReadPreference] to be used when executing the command
 * @param <TResult>      the type of the class to use instead of `Document`.
 * @return TResult object result of the command
 */
inline fun <reified TResult : Any> MongoDatabase.runCommand(
    command: Bson,
    readPreference: ReadPreference
): Maybe<TResult> {
    return maybeResult { runCommand(command, readPreference, TResult::class.java, it) }
}

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
    return maybeResult { runCommand(KMongoUtil.toBson(command), readPreference, TResult::class.java, it) }
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
 * Drops this database.

 * @mongodb.driver.manual reference/command/dropDatabase/#dbcmd.dropDatabase Drop database
 */
fun MongoDatabase.drop(): Completable {
    return completableResult { this.drop(it) }
}

/**
 * Create a new collection with the given name.

 * @param collectionName the name for the new collection to create
 * @mongodb.driver.manual reference/command/create Create Command
 */
fun MongoDatabase.createCollection(collectionName: String): Completable {
    return completableResult { this.createCollection(collectionName, it) }
}

/**
 * Create a new collection with the selected options

 * @param collectionName the name for the new collection to create
 * @param options        various options for creating the collection
 * @mongodb.driver.manual reference/command/create Create Command
 */
fun MongoDatabase.createCollection(collectionName: String, options: CreateCollectionOptions): Completable {
    return completableResult { this.createCollection(collectionName, options, it) }
}

/**
 * Gets a collection.
 *
 * @param <T>            the default target type of the collection to return
 *                       - the name of the collection is determined by [defaultCollectionName]
 * @return the collection
 * @see defaultCollectionName
 */
@Deprecated("use same function with org.litote.kmongo.async package - will be removed in 4.0")
inline fun <reified T : Any> MongoDatabase.getCollection(): MongoCollection<T> =
    getCollection(defaultCollectionName(T::class), T::class.java)


/**
 * Gets a collection.
 *
 * @param <T>            the default target type of the collection to return
 *                       - the name of the collection is determined by [defaultCollectionName]
 * @return the collection
 * @see defaultCollectionName
 */
@Deprecated("use same function with org.litote.kmongo.async package - will be removed in 4.0")
inline fun <reified T : Any> MongoDatabase.getCollectionOfName(name: String): MongoCollection<T> =
    getCollection(name, T::class.java)

/**
 * Creates a view with the given name, backing collection/view name, and aggregation pipeline that defines the view.

 * @param viewName the name of the view to create
 * @param viewOn   the backing collection/view for the view
 * @param pipeline the pipeline that defines the view
 * @mongodb.driver.manual reference/command/create Create Command
 */
fun MongoDatabase.createView(viewName: String, viewOn: String, pipeline: List<Bson>): Completable {
    return completableResult { this.createView(viewName, viewOn, pipeline, it) }
}

/**
 * Creates a view with the given name, backing collection/view name, aggregation pipeline, and options that defines the view.

 * @param viewName the name of the view to create
 * @param viewOn   the backing collection/view for the view
 * @param pipeline the pipeline that defines the view
 * @param createViewOptions various options for creating the view
 * @mongodb.driver.manual reference/command/create Create Command
 */
fun MongoDatabase.createView(
    viewName: String,
    viewOn: String,
    pipeline: List<Bson>,
    createViewOptions: CreateViewOptions
): Completable {
    return completableResult { this.createView(viewName, viewOn, pipeline, createViewOptions, it) }
}

/**
 * Drops this collection from the Database.
 *
 * @mongodb.driver.manual reference/command/drop/ Drop Collection
 */
inline fun <reified T : Any> MongoDatabase.dropCollection() = dropCollection(defaultCollectionName(T::class))

/**
 * Drops this collection from the Database.
 *
 * @mongodb.driver.manual reference/command/drop/ Drop Collection
 */
fun MongoDatabase.dropCollection(collectionName: String): Completable {
    return completableResult { getCollection(collectionName).drop(it) }
}