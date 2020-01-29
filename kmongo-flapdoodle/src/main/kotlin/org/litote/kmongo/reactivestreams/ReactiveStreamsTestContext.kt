/*
 * Copyright (C) 2016/2020 Litote
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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


internal class ReactiveStreamsTestContext {

    val lock = CountDownLatch(1)
    @Volatile
    var error: Throwable? = null

    fun test(testToRun: () -> Unit) {
        try {
            testToRun()
        } catch (t: Throwable) {
            error = t
            throw t
        } finally {
            lock.countDown()
        }
    }

    fun waitToComplete() {
        assert(lock.await(40, TimeUnit.SECONDS))
        val err = error
        if (err != null) throw err
    }
}