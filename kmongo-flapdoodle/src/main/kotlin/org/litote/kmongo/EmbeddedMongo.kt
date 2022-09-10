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
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion
import org.bson.BsonDocument
import org.bson.Document

internal val MongodProcess.host get() = "127.0.0.1:${config.net().port}"

/**
 * Flapdoodle wrapper.
 */
internal class EmbeddedMongo(private val version: IFeatureAwareVersion) {

    private val standalone = System.getProperty("kmongo.flapdoodle.replicaset") != "true"

    fun connectionString(commandExecutor: (String, BsonDocument, (Document?, Throwable?) -> Unit) -> Unit): ConnectionString =
        if (standalone) {
            StandaloneEmbeddedMongo(version).connectionString(commandExecutor)
        } else {
            ReplicaSetEmbeddedMongo(version).connectionString(commandExecutor)
        }


}