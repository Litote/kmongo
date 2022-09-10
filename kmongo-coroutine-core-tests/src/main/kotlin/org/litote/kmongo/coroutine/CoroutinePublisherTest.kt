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

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.CoreCategory
import org.reactivestreams.Publisher

@Category(CoreCategory::class)
class CoroutinePublisherTest {

    private val publisher: Publisher<Int> = DoSinglePublisher(1)
    private val coroutine = publisher.coroutine

    @Test
    fun toList() {
        runBlocking {
            assertEquals(listOf(1), coroutine.toList())
        }
    }

    @Test
    fun toFlow() {
        runBlocking {
            assertEquals(listOf(1), coroutine.toFlow().toList())
        }
    }
}
