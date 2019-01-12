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

package org.litote.kmongo.reactivestreams

import com.mongodb.client.model.changestream.OperationType
import org.litote.kmongo.model.Friend
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test

/**
 *
 */
class WatchTest : KMongoReactiveStreamsBaseTest<Friend>() {

    @Test
    fun `check invalidate event does not close watchIndefinitely`() {
        val friend = Friend("Paul")
        val dropped = AtomicBoolean()
        var counter = CountDownLatch(1)
        col.watchIndefinitely(subscribeListener = {
            if (!dropped.get()) {
                col.insertOne(friend).forEach { _, _ ->
                    counter.await()
                    counter = CountDownLatch(1)
                    dropped.set(true)
                    col.drop().forEach { _, _ ->
                    }
                }
            } else {
                col.insertOne(friend).forEach { _, _ ->
                    asyncTest {
                        counter.await()
                    }
                }
            }
        }) {
            if (it.operationType == OperationType.INSERT) {
                counter.countDown()
            }
        }
    }
}