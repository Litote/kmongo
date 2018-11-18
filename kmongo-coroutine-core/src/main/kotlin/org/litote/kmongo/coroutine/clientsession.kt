package org.litote.kmongo.coroutine

import com.mongodb.async.client.ClientSession

/**
 * Commit a transaction in the context of this session.  A transaction can only be commited if one has first been started.
 */
suspend fun ClientSession.commitTransaction(): Void {
    return singleNonNullResult(this::commitTransaction)
}

/**
 * Abort a transaction in the context of this session.  A transaction can only be aborted if one has first been started.
 */
suspend fun ClientSession.abortTransaction(): Void {
    return singleNonNullResult(this::abortTransaction)
}