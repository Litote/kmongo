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

import com.mongodb.client.model.Collation
import com.mongodb.client.model.MapReduceAction.REPLACE
import com.mongodb.reactivestreams.client.MapReducePublisher
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.CoreCategory
import org.litote.kmongo.EMPTY_BSON
import java.util.concurrent.TimeUnit

/**
 *
 */
@Category(CoreCategory::class)
class CoroutineMapReducePublisherTest {

    val publisher: MapReducePublisher<*> = mock()
    val coroutine = publisher.coroutine

    @Test
    fun filter() {
        whenever(publisher.filter(EMPTY_BSON)).thenReturn(publisher)
        coroutine.filter(EMPTY_BSON)
        verify(publisher).filter(EMPTY_BSON)
    }

    @Test
    fun sort() {
        whenever(publisher.sort(EMPTY_BSON)).thenReturn(publisher)
        coroutine.sort(EMPTY_BSON)
        verify(publisher).sort(EMPTY_BSON)
    }

    @Test
    fun scope() {
        whenever(publisher.scope(EMPTY_BSON)).thenReturn(publisher)
        coroutine.scope(EMPTY_BSON)
        verify(publisher).scope(EMPTY_BSON)
    }

    @Test
    fun jsMode() {
        whenever(publisher.jsMode(true)).thenReturn(publisher)
        coroutine.jsMode(true)
        verify(publisher).jsMode(true)
    }

    @Test
    fun verbose() {
        whenever(publisher.verbose(true)).thenReturn(publisher)
        coroutine.verbose(true)
        verify(publisher).verbose(true)
    }

    @Test
    fun sharded() {
        whenever(publisher.sharded(true)).thenReturn(publisher)
        coroutine.sharded(true)
        verify(publisher).sharded(true)
    }

    @Test
    fun nonAtomic() {
        whenever(publisher.nonAtomic(true)).thenReturn(publisher)
        coroutine.nonAtomic(true)
        verify(publisher).nonAtomic(true)
    }

    @Test
    fun bypassDocumentValidation() {
        whenever(publisher.bypassDocumentValidation(true)).thenReturn(publisher)
        coroutine.bypassDocumentValidation(true)
        verify(publisher).bypassDocumentValidation(true)
    }

    @Test
    fun collectionName() {
        whenever(publisher.collectionName("name")).thenReturn(publisher)
        coroutine.collectionName("name")
        verify(publisher).collectionName("name")
    }

    @Test
    fun action() {
        whenever(publisher.action(REPLACE)).thenReturn(publisher)
        coroutine.action(REPLACE)
        verify(publisher).action(REPLACE)
    }

    @Test
    fun databaseName() {
        whenever(publisher.databaseName("name")).thenReturn(publisher)
        coroutine.databaseName("name")
        verify(publisher).databaseName("name")
    }

    @Test
    fun finalizeFunction() {
        whenever(publisher.finalizeFunction("name")).thenReturn(publisher)
        coroutine.finalizeFunction("name")
        verify(publisher).finalizeFunction("name")
    }

    @Test
    fun maxTime() {
        whenever(publisher.maxTime(1, TimeUnit.MILLISECONDS)).thenReturn(publisher)
        coroutine.maxTime(1, TimeUnit.MILLISECONDS)
        verify(publisher).maxTime(1, TimeUnit.MILLISECONDS)
    }

    @Test
    fun collation() {
        val collation = Collation.builder().build()
        whenever(publisher.collation(collation)).thenReturn(publisher)
        coroutine.collation(collation)
        verify(publisher).collation(collation)
    }

    @Test
    fun batchSize() {
        whenever(publisher.batchSize(1)).thenReturn(publisher)
        coroutine.batchSize(1)
        verify(publisher).batchSize(1)
    }

    @Test
    fun limit() {
        whenever(publisher.limit(1)).thenReturn(publisher)
        coroutine.limit(1)
        verify(publisher).limit(1)
    }


    @Test
    fun first() {
        whenever(publisher.first()).thenReturn(DoNothingPublisher())
        runBlocking {
            coroutine.first()
        }
        verify(publisher).first()
    }

    @Test
    fun toCollection() {
        whenever(publisher.toCollection()).thenReturn(DoNothingPublisher())
        runBlocking {
            coroutine.toCollection()
        }
        verify(publisher).toCollection()
    }
}