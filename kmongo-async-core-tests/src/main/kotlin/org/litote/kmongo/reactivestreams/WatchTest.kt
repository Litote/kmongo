/*
 * Copyright (C) 2016/2021 Litote
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
import org.litote.kmongo.reactivestreams.WatchTest.Watch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test

/**
 *
 */
class WatchTest : KMongoReactiveStreamsBaseTest<Watch>() {

    data class Watch(val name: String)

    @Test
    fun `check invalidate event does not close watchIndefinitely`() {
        val friend = Watch("Paul")
        val dropped = AtomicBoolean()
        val counter1 = CountDownLatch(1)
        val counter2 = CountDownLatch(1)

        col.watchIndefinitely(
            reopenDelayInMS = 5000,
            errorListener = { it.printStackTrace() },
            reopenListener = { println("reopen") },
            subscribeListener = {
                println("subscribed $dropped")
                Executors.newSingleThreadScheduledExecutor().apply {
                    schedule(
                        {
                            if (!dropped.get()) {
                                col.insertOne(friend).forEach { _, _ ->
                                    counter1.await()
                                    dropped.set(true)
                                    col.drop().forEach { _, _ ->
                                    }
                                }
                            } else {
                                col.insertOne(friend).forEach { _, _ ->
                                    asyncTest {
                                        counter2.await()
                                    }
                                }
                            }
                        },
                        5000,
                        TimeUnit.MILLISECONDS
                    )
                }
            }) {
            println(it)
            if (it.operationType == OperationType.INSERT) {
                counter1.countDown()
                counter2.countDown()
            }
        }
    }
}