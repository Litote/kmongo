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

package org.litote.kmongo.reactor

import com.mongodb.DuplicateKeyException
import com.mongodb.MongoWriteException
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 *
 */
class InsertTest : KMongoReactorBaseTest<Friend>() {

    @Test
    fun canInsertNewDocument() {
        val friend = Friend("Jane", "45 rue de la Paix")
        col.insert(friend).block()
        val insertedFriend = col.findOne("{name:'Jane'}").block() ?: throw AssertionError("Value must not null!")
        assertEquals("Jane", insertedFriend.name)
    }

    @Test
    fun cannotInsertSameId() {
        val existingId = ObjectId()
        col.insertOne(Friend("James", "45 avenue Foch", existingId)).block()!!
        assertFailsWith<MongoWriteException> {
            col.insert(Friend("Martin", "12 avenue Montaigne", existingId)).block()
        }
        val insertedFriend = col.findOneById(existingId).block() ?: throw AssertionError("Value must not null!")
        assertEquals("James", insertedFriend.name)
    }
}
