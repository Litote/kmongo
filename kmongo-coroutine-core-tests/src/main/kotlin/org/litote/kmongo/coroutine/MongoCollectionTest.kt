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
package org.litote.kmongo.coroutine

import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.ClientSession
import com.mongodb.async.client.MongoCollection
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.litote.kmongo.CoreCategory
import org.litote.kmongo.model.Friend
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@Category(CoreCategory::class)
@RunWith(MockitoJUnitRunner::class)
class MongoCollectionTest {

    @Mock
    lateinit var collection: MongoCollection<Friend>

    @Mock
    lateinit var clientSession: ClientSession

    @Test
    fun `should drop collection`() = runBlocking {
        // Given
        whenever(collection.drop(any())).thenAnswer {
            (it.arguments.first() as SingleResultCallback<Void>).onResult(null, null)
        }

        // When
        collection.drop()

        // Then
        verify(collection).drop(any())
    }

    @Test
    fun `should drop collection during clientSession`() = runBlocking {
        // Given
        whenever(collection.drop(eq(clientSession), any())).thenAnswer {
            (it.arguments.last() as SingleResultCallback<Void>).onResult(null, null)
        }

        // When
        collection.drop(clientSession)

        // Then
        verify(collection).drop(eq(clientSession), any())
    }
}
