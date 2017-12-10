/*
 * Copyright (C) 2017 Litote
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

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOneById
import org.litote.kmongo.json
import org.litote.kmongo.withDocumentClass
import kotlin.reflect.KClass
import kotlin.test.assertEquals

/**
 *
 */
class Issue30DBObjectInsertException : AllCategoriesKMongoBaseTest<Issue30DBObjectInsertException.User>() {

    data class User(val _id: ObjectId,
                    val userId: String,
                    val account: DBObject)

    data class User2(val _id: ObjectId,
                     val userId: String,
                     val account: Document)

    data class Account(val test: String)

    override fun getDefaultCollectionClass(): KClass<User> {
        return User::class
    }

    @Test
    fun testSave() {
        val dbObject = BasicDBObject()
        dbObject.put("test", "t")
        dbObject.put("ob", BasicDBObject(mapOf("a" to 2)))
        val user = User(ObjectId(), "id", dbObject)
        col.insertOne(user)
        assertEquals(user, col.findOneById(user._id))
    }

    @Test
    fun testSave2() {
        val user = User2(ObjectId(), "id", Document.parse(Account("a").json))
        val c = col.withDocumentClass<User2>()
        c.insertOne(user)
        assertEquals(user, c.findOneById(user._id))
    }

}