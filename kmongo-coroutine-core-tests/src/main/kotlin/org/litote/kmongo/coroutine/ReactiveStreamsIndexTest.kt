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

package org.litote.kmongo.coroutine

import com.mongodb.MongoCommandException
import com.mongodb.client.model.DropIndexOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.reactivestreams.client.MongoCollection
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.CoreCategory
import org.litote.kmongo.model.Friend

/**
 *
 */
@Category(CoreCategory::class)
class ReactiveStreamsIndexTest {

    val collection: MongoCollection<*> = mock()
    val coroutine = collection.coroutine

    @Test
    fun `ensureIndex with property calls createIndex`() {
        whenever(collection.createIndex(any(), any<IndexOptions>())).thenReturn(DoSinglePublisher("index"))
        runBlocking {
            coroutine.ensureIndex(Friend::name)
        }
        verify(collection).createIndex(any(), any<IndexOptions>())
    }

    @Test
    fun `ensureIndex with json call createIndex`() {
        whenever(collection.createIndex(any(), any<IndexOptions>())).thenReturn(DoSinglePublisher("index"))
        runBlocking {
            coroutine.ensureIndex("{name:1}")
        }
        verify(collection).createIndex(any(), any<IndexOptions>())
    }

    @Test
    fun `ensureIndex calls dropIndex if createIndex throw an exception`() {
        whenever(
            collection.createIndex(
                any(),
                any<IndexOptions>()
            )
        ).thenReturn(ErrorPublisher(mock<MongoCommandException>()))

        whenever(
            collection.dropIndex(
                any<Bson>(),
                any<DropIndexOptions>()
            )
        ).thenReturn(DoNothingPublisher())

        runBlocking {
            try {
                coroutine.ensureIndex(Friend::name)
            } catch (e: Exception) {
                //ignore
            }
        }
        verify(collection, times(2)).createIndex(any(), any<IndexOptions>())
        verify(collection).dropIndex(any<Bson>(), any<DropIndexOptions>())
    }
}