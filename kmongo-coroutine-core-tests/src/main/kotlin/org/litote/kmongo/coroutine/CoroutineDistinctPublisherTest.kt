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
import com.mongodb.reactivestreams.client.DistinctPublisher
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
class CoroutineDistinctPublisherTest {

    val publisher: DistinctPublisher<*> = mock()
    val coroutine = publisher.coroutine

    @Test
    fun filter() {
        whenever(publisher.filter(EMPTY_BSON)).thenReturn(publisher)
        coroutine.filter(EMPTY_BSON)
        verify(publisher).filter(EMPTY_BSON)
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
    fun first() {
        whenever(publisher.first()).thenReturn(DoNothingPublisher())
        runBlocking {
            coroutine.first()
        }
        verify(publisher).first()
    }
}