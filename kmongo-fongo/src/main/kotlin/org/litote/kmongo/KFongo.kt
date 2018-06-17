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

import com.github.fakemongo.Fongo
import com.github.fakemongo.junit.FongoRule
import com.mongodb.MongoClient.getDefaultCodecRegistry
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.service.ClassMappingType


/**
 * To retrieve Fongo databases.
 */
object KFongo {

    internal val fongo = Fongo(
        "test",
        Fongo.DEFAULT_SERVER_VERSION,
        ClassMappingType.codecRegistry(getDefaultCodecRegistry())
    )

    fun getDatabase(dbName: String = FongoRule.randomName()): MongoDatabase =
        KFongoMongoDatabase(fongo.getDatabase(dbName))
}