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

import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.model.Friend
import reactor.kotlin.core.publisher.toFlux
import kotlin.test.assertEquals

/**
 *
 */
class DeleteTest : KMongoReactorBaseTest<Friend>() {

    @Test
    fun canDeleteASpecificDocument() {
        col.insertMany(listOf(Friend("John"), Friend("Peter"))).blockLast()
        col.deleteOne("{name:'John'}").blockLast()
        val list = col.find().toFlux().collectList().block()!!
        assertEquals(1, list.size)
        assertEquals("Peter", list.first().name)
    }

    @Test
    fun canDeleteByObjectId() {
        col.insertOne("{ _id:{$oid:'47cc67093475061e3d95369d'}, name:'John'}").block()
        col.deleteOneById(ObjectId("47cc67093475061e3d95369d")).block()
        val count = col.countDocuments().block()
        assertEquals(0, count)
    }

    @Test
    fun canRemoveAll() {
        col.insertMany(listOf(Friend("John"), Friend("Peter"))).blockLast()
        col.deleteMany("{}").block()
        val count = col.countDocuments().block()
        assertEquals(0, count)
    }
}
