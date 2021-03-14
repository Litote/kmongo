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
        val counter0 = CountDownLatch(1)
        col.insertOne(friend).forEach { _, _ ->  counter0.countDown() }
        counter0.await()
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
                                println("insert first")
                                col.insertOne(friend).forEach { _, _ ->
                                    println("await1")
                                    counter1.await()
                                    asyncTest {
                                        dropped.set(true)
                                        println("drop")
                                        col.drop().waitSingle {  _, _ ->

                                        }
                                    }
                                }
                            } else {
                                println("insert second")
                                col.insertOne(friend).forEach { _, _ ->
                                }
                            }
                        },
                        5000,
                        TimeUnit.MILLISECONDS
                    )
                }
            }) {
            println("event:$it")
            if (it.operationType == OperationType.INSERT) {
                println("listen insert")
                if(counter1.count != 0L) {
                    counter1.countDown()
                } else {
                    counter2.countDown()
                }
            }
        }

        asyncTest {
            counter1.await()
            println("pass 1")
            counter2.await()
            println("pass 2")
        }
    }
}