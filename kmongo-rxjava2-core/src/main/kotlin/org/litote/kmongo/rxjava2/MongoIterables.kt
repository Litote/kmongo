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

package org.litote.kmongo.rxjava2

import com.mongodb.Block
import com.mongodb.async.AsyncBatchCursor
import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.MongoIterable
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Iterates over all the documents, adding each to an observable.
 *
 */
fun <TResult> MongoIterable<TResult>.toObservable(): Observable<TResult> {
    return Observable.create { source ->
        batchCursor { cursor, t1 ->
            if (t1 != null) {
                source.onError(t1)
            } else {
                loopCursor(cursor,
                    Block<TResult> { res ->
                        source.onNext(res)
                    },
                    SingleResultCallback { _, t2 ->
                        if (t2 != null) {
                            source.onError(t2)
                        } else {
                            source.onComplete()
                        }
                    })
            }
        }
    }
}

fun <TResult> MongoIterable<TResult>.toFlowable(mode: BackpressureStrategy = BackpressureStrategy.MISSING): Flowable<TResult> {
    return Flowable.create({ source ->
        batchCursor { cursor, t1 ->
            if (t1 != null) {
                source.onError(t1)
            } else {
                loopCursor(cursor,
                    Block<TResult> { res ->
                        source.onNext(res)
                    },
                    SingleResultCallback { _, t2 ->
                        if (t2 != null) {
                            source.onError(t2)
                        } else {
                            source.onComplete()
                        }
                    })
            }
        }
    }, mode)
}

private fun <TResult> MongoIterable<TResult>.loopCursor(
    batchCursor: AsyncBatchCursor<TResult>,
    block: Block<in TResult>,
    callback: SingleResultCallback<Void>
) {
    batchCursor.next { results, t ->
        if (t != null || results == null) {
            batchCursor.close()
            callback.onResult(null, t)
        } else {
            try {
                for (result in results) {
                    block.apply(result)
                }
                loopCursor(batchCursor, block, callback)
            } catch (tr: Throwable) {
                batchCursor.close()
                callback.onResult(null, tr)
            }

        }
    }
}
