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

package org.litote.kmongo.coroutine

import com.mongodb.ClientSessionOptions
import com.mongodb.async.client.ClientSession
import com.mongodb.async.client.MongoClient

/**
 * Creates a client session with default options.
 *
 * <p>Note: A ClientSession instance can not be used concurrently in multiple asynchronous operations.</p>
 */
suspend fun MongoClient.startSession(): ClientSession {
    return singleResult(this::startSession) ?: throw IllegalStateException("Unexpected null result from startSession()")
}

/**
 * Creates a client session.
 *
 * <p>Note: A ClientSession instance can not be used concurrently in multiple asynchronous operations.</p>
 *
 * @param options  the options for the client session
 */
suspend fun MongoClient.startSession(options: ClientSessionOptions): ClientSession {
    return singleResult { this.startSession(options, it) }
            ?: throw IllegalStateException("Unexpected null result from startSession()")
}
