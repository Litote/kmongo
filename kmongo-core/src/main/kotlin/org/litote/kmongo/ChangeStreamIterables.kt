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

package org.litote.kmongo

import com.mongodb.client.ChangeStreamIterable
import com.mongodb.client.model.changestream.ChangeStreamDocument
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Listens change stream in an other thread.
 *
 * @param executor the executor service for the thread instantiation - default is [Executors.newSingleThreadExecutor]
 * @param delay the delay the executor is waiting before submitting the task
 * @param unit the unit of the [delay]
 * @param listener to listen changes
 */
fun <T> ChangeStreamIterable<T>.listen(
    executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    delay: Long = 500,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    listener: (ChangeStreamDocument<T>) -> Unit
) {
    //need to listen the iterator from the original thread
    val cursor = iterator()
    executor.schedule(
        {
            cursor.use { cursor ->
                while (cursor.hasNext()) {
                    listener(cursor.next())
                }
            }
        },
        delay,
        unit
    )
}