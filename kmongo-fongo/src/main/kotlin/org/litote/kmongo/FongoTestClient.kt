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
import com.mongodb.MongoClient.getDefaultCodecRegistry
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.litote.kmongo.util.KMongoConfiguration


/**
 *
 */
internal object FongoTestClient {

    val fongo = Fongo(
            "test",
            Fongo.DEFAULT_SERVER_VERSION,
            fromRegistries(getDefaultCodecRegistry(),
                    fromProviders(KMongoConfiguration.jacksonCodecProvider))
    )
}