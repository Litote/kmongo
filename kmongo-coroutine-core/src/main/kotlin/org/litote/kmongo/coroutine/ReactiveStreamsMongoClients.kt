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

import com.mongodb.ClientSessionOptions
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoClient
import kotlinx.coroutines.reactive.awaitSingle

/**
 * Creates a client session with default options.
 *
 * <p>Note: A ClientSession instance can not be used concurrently in multiple asynchronous operations.</p>
 */
suspend fun MongoClient.startSessionAndAwait(): ClientSession = startSession().awaitSingle()

/**
 * Creates a client session.
 *
 * <p>Note: A ClientSession instance can not be used concurrently in multiple asynchronous operations.</p>
 *
 * @param options  the options for the client session
 */
suspend fun MongoClient.startSessionAndAwait(options: ClientSessionOptions): ClientSession =
    startSession(options).awaitSingle()

