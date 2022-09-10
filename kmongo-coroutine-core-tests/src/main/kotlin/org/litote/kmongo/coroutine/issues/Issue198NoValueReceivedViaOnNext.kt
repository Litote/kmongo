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

package org.litote.kmongo.coroutine.issues

import com.mongodb.WriteConcern
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.litote.kmongo.coroutine.KMongoReactiveStreamsCoroutineBaseTest
import org.litote.kmongo.coroutine.abortTransactionAndAwait
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class Issue198NoValueReceivedViaOnNext : KMongoReactiveStreamsCoroutineBaseTest<Friend>() {

    @Test
    fun saveTwoObjectsInSameTransaction() {
        runBlocking {
            //if the collection does not pre-exist, the multi-document transaction fails
            //see also https://docs.mongodb.com/manual/core/transactions-production-consideration/#acquiring-locks
            database.withWriteConcern(WriteConcern.MAJORITY).createCollection(col.collection.namespace.collectionName)

            mongoClient.startSession().use { clientSession ->
                clientSession.startTransaction()
                col.insertOne(clientSession, Friend("Bob"))
                col.insertOne(clientSession, Friend("Joe"))
                clientSession.commitTransactionAndAwait()
                assertEquals(2, col.countDocuments())
                col.deleteMany()
            }
        }
    }

    @Test
    fun abortTwoObjectsInSameTransaction() {
        runBlocking {
            //if the collection does not pre-exist, the multi-document transaction fails
            //see also https://docs.mongodb.com/manual/core/transactions-production-consideration/#acquiring-locks
            database.withWriteConcern(WriteConcern.MAJORITY).createCollection(col.collection.namespace.collectionName)

            mongoClient.startSession().use { clientSession ->
                clientSession.startTransaction()
                col.insertOne(clientSession, Friend("Bob"))
                col.insertOne(clientSession, Friend("Joe"))
                clientSession.abortTransactionAndAwait()
                assertEquals(0, col.countDocuments())
                col.deleteMany()
            }
        }
    }
}