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

package org.litote.kmongo.issues

import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOneById
import org.litote.kmongo.model.Friend
import org.litote.kmongo.save
import kotlin.test.assertEquals

/**
 *
 */
class Issue16GetIdPropertyAndMap : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun testSerializeAndDeserializeDocumentWithId() {
        val id = ObjectId("58ed213ca00a936d64541a1d")
        val thing = Document("_id", id).append("name", "test this")
        database.getCollection("test").apply {
            save(thing)
            assertEquals(thing, findOneById(id))
            save(thing)
            assertEquals(thing, findOneById(id))
        }
    }

}