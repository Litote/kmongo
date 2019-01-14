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

import com.mongodb.CursorType
import com.mongodb.client.model.Collation
import com.mongodb.reactivestreams.client.FindPublisher
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.litote.kmongo.EMPTY_BSON
import java.util.concurrent.TimeUnit

/**
 *
 */
class CoroutineFindPublisherTest {

    val publisher: FindPublisher<*> = mock()
    val coroutine = publisher.coroutine

    @Test
    fun noCursorTimeout() {
        whenever(publisher.noCursorTimeout(true)).thenReturn(publisher)
        coroutine.noCursorTimeout(true)
        verify(publisher).noCursorTimeout(true)
    }

    @Test
    fun oplogReplay() {
        whenever(publisher.oplogReplay(true)).thenReturn(publisher)
        coroutine.oplogReplay(true)
        verify(publisher).oplogReplay(true)
    }

    @Test
    fun partial() {
        whenever(publisher.partial(true)).thenReturn(publisher)
        coroutine.partial(true)
        verify(publisher).partial(true)
    }

    @Test
    fun showRecordId() {
        whenever(publisher.showRecordId(true)).thenReturn(publisher)
        coroutine.showRecordId(true)
        verify(publisher).showRecordId(true)
    }

    @Test
    fun returnKey() {
        whenever(publisher.returnKey(true)).thenReturn(publisher)
        coroutine.returnKey(true)
        verify(publisher).returnKey(true)
    }

    @Test
    fun cursorType() {
        whenever(publisher.cursorType(CursorType.NonTailable)).thenReturn(publisher)
        coroutine.cursorType(CursorType.NonTailable)
        verify(publisher).cursorType(CursorType.NonTailable)
    }

    @Test
    fun hint() {
        whenever(publisher.hint(EMPTY_BSON)).thenReturn(publisher)
        coroutine.hint(EMPTY_BSON)
        verify(publisher).hint(EMPTY_BSON)
    }

    @Test
    fun max() {
        whenever(publisher.max(EMPTY_BSON)).thenReturn(publisher)
        coroutine.max(EMPTY_BSON)
        verify(publisher).max(EMPTY_BSON)
    }

    @Test
    fun min() {
        whenever(publisher.min(EMPTY_BSON)).thenReturn(publisher)
        coroutine.min(EMPTY_BSON)
        verify(publisher).min(EMPTY_BSON)
    }

    @Test
    fun comment() {
        whenever(publisher.comment("a")).thenReturn(publisher)
        coroutine.comment("a")
        verify(publisher).comment("a")
    }

    @Test
    fun filter() {
        whenever(publisher.filter(EMPTY_BSON)).thenReturn(publisher)
        coroutine.filter(EMPTY_BSON)
        verify(publisher).filter(EMPTY_BSON)
    }

    @Test
    fun projection() {
        whenever(publisher.projection(EMPTY_BSON)).thenReturn(publisher)
        coroutine.projection(EMPTY_BSON)
        verify(publisher).projection(EMPTY_BSON)
    }

    @Test
    fun sort() {
        whenever(publisher.sort(EMPTY_BSON)).thenReturn(publisher)
        coroutine.sort(EMPTY_BSON)
        verify(publisher).sort(EMPTY_BSON)
    }

    @Test
    fun maxAwaitTime() {
        whenever(publisher.maxAwaitTime(1, TimeUnit.MILLISECONDS)).thenReturn(publisher)
        coroutine.maxAwaitTime(1, TimeUnit.MILLISECONDS)
        verify(publisher).maxAwaitTime(1, TimeUnit.MILLISECONDS)
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
    fun skip() {
        whenever(publisher.skip(1)).thenReturn(publisher)
        coroutine.skip(1)
        verify(publisher).skip(1)
    }

    @Test
    fun first() {
        whenever(publisher.first()).thenReturn(DoNothingPublisher())
        runBlocking {
            coroutine.first()
        }
        verify(publisher).first()
    }
}