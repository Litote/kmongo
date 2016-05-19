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

package org.litote.kmongo.async

import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.async.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class DeleteTest : KMongoAsyncBaseTest<Friend>() {

    @Test
    fun canDeleteASpecificDocument() {
        col.insertMany(listOf(Friend("John"), Friend("Peter")), { r, t ->
            col.deleteOne("{name:'John'}", { r, t ->
                col.find().toList { list, throwable ->
                    asyncTest {
                        assertEquals(1, list!!.size)
                        assertEquals("Peter", list.first().name)
                    }
                }
            })
        })
    }

    @Test
    fun canDeleteByObjectId() {
        col.insertOne("{ _id:{$oid:'47cc67093475061e3d95369d'}, name:'John'}", { r, t ->
            col.deleteOne(ObjectId("47cc67093475061e3d95369d"), { r, t ->
                col.count({ c, t ->
                    asyncTest {
                        assertEquals(0, c)
                    }
                })
            })
        })
    }

    @Test
    fun canRemoveAll() {
        col.insertMany(listOf(Friend("John"), Friend("Peter")), { r, t ->
            col.deleteMany("{}", { r, t ->
                col.count({ c, t ->
                    asyncTest {
                        assertEquals(0, c)
                    }
                })
            })
        })
    }
}