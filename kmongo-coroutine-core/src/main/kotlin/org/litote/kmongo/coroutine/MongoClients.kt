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
