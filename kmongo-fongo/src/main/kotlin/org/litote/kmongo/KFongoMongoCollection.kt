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

import com.github.fakemongo.junit.FongoRule
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import org.bson.conversions.Bson

/**
 * A custom collection class to customize behaviour of Fongo.
 */
internal class KFongoMongoCollection<T>(val col: MongoCollection<T>) : MongoCollection<T> by col {

    override fun createIndex(keys: Bson, indexOptions: IndexOptions): String {
        return col.createIndex(
                keys,
                indexOptions.apply {
                    //simulate index name generation
                    if (name == null) {
                        name(FongoRule.randomName())
                    }
                }
        )
    }
}