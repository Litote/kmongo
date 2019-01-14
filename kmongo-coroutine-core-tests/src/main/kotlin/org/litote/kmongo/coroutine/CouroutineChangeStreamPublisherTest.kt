/*
 * Copyright (C) 2017/2018 Litote
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
import com.mongodb.client.model.changestream.FullDocument.UPDATE_LOOKUP
import com.mongodb.reactivestreams.client.ChangeStreamPublisher
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.bson.BsonDocument
import org.bson.BsonTimestamp
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 *
 */
class CoroutineChangeStreamPublisherTest {

    val publisher: ChangeStreamPublisher<*> = mock()
    val coroutine = publisher.coroutine

    @Test
    fun fullDocument() {
        whenever(publisher.fullDocument(UPDATE_LOOKUP)).thenReturn(publisher)
        coroutine.fullDocument(UPDATE_LOOKUP)
        verify(publisher).fullDocument(UPDATE_LOOKUP)
    }

    @Test
    fun resumeAfter() {
        whenever(publisher.resumeAfter(BsonDocument())).thenReturn(publisher)
        coroutine.resumeAfter(BsonDocument())
        verify(publisher).resumeAfter(BsonDocument())
    }

    @Test
    fun startAtOperationTime() {
        val timestamp = BsonTimestamp()
        whenever(publisher.startAtOperationTime(timestamp)).thenReturn(publisher)
        coroutine.startAtOperationTime(timestamp)
        verify(publisher).startAtOperationTime(timestamp)
    }

    @Test
    fun maxAwaitTime() {
        whenever(publisher.maxAwaitTime(1, TimeUnit.MILLISECONDS)).thenReturn(publisher)
        coroutine.maxAwaitTime(1, TimeUnit.MILLISECONDS)
        verify(publisher).maxAwaitTime(1, TimeUnit.MILLISECONDS)
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
    fun first() {
        whenever(publisher.first()).thenReturn(DoNothingPublisher())
        runBlocking {
            coroutine.first()
        }
        verify(publisher).first()
    }

    @Test
    fun withDocumentClass() {
        whenever(publisher.withDocumentClass<String>(String::class.java)).thenReturn(DoNothingPublisher())
        runBlocking {
            coroutine.withDocumentClass<String>()
        }
        verify(publisher).withDocumentClass<String>(String::class.java)
    }
}