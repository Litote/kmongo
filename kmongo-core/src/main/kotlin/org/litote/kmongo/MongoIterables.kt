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

package org.litote.kmongo

import com.mongodb.client.MongoIterable

/**
 * Iterates over all the documents, adding each to the given target.
 *
 * @param target   the collection to insert into
 * @param callback a callback that will be passed the target containing all documents
 */
@Deprecated("use classic toList extension")
fun <T> MongoIterable<T>.toList(): List<T> = into(mutableListOf<T>())

private val NULL = Any()

private class NullIterator<T>(val iterator: Iterator<T>) : Iterator<T?> by iterator {
    override fun next(): T? = iterator.next()?.takeUnless { it === NULL }
}

private class NullHandlerSequence<T>(val sequence: Sequence<T>) : Sequence<T?> {

    override fun iterator(): Iterator<T?> = NullIterator(sequence.iterator())
}

/**
 * Evaluates the current [MongoIterable] given the [expression] of Sequences.
 *
 * The mongo cursor is closed before returning the result.
 *
 * Sample:
 * ```
 *   col.find().evaluate {
 *      //this is a sequence evaluation
 *      //If the first row has a name like "Fred", only one row is loaded in memory!
 *      filter { it.name != "Joe" }.first()
 *   }
 * ```
 */
fun <T, R> MongoIterable<T>.evaluate(expression: Sequence<T>.() -> R): R {
    @Suppress("UNCHECKED_CAST")
    return iterator().run {
        use {
            expression(
                NullHandlerSequence(
                    generateSequence {
                        if (hasNext()) {
                            next() ?: NULL
                        } else {
                            null
                        }
                    }
                ) as Sequence<T>
            )
        }
    }
}
