/*
 * Copyright (C) 2017 Litote
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

package org.litote.kmongo.other

import com.mongodb.Block
import com.mongodb.Function
import com.mongodb.ServerAddress
import com.mongodb.ServerCursor
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoIterable
import com.mongodb.lang.Nullable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.model.Friend
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

//copied from mongo

internal class MongoMappingCursor<T, U>(val proxied: MongoCursor<T>, val mapper: Function<T, U>) : MongoCursor<U> {

    override fun close() {
        proxied.close()
    }

    override fun hasNext(): Boolean {
        return proxied.hasNext()
    }

    override fun next(): U {
        return mapper.apply(proxied.next())
    }

    @Nullable
    override fun tryNext(): U? {
        val proxiedNext = proxied.tryNext()
        return if (proxiedNext == null) {
            null
        } else {
            mapper.apply(proxiedNext)
        }
    }

    override fun remove() {
        proxied.remove()
    }

    @Nullable
    override fun getServerCursor(): ServerCursor? {
        return proxied.serverCursor
    }

    override fun getServerAddress(): ServerAddress {
        return proxied.serverAddress
    }
}


class MappingIterable<U, V>(val mapped: MongoIterable<U>, private val mapper: Function<U, V>) :
    MongoIterable<V> {

    override fun iterator(): MongoCursor<V> {
        return MongoMappingCursor(mapped.iterator(), mapper)
    }

    @Nullable
    override fun first(): V? {
        val iterator = iterator()
        return if (!iterator.hasNext()) {
            null
        } else iterator.next()
    }

    override fun forEach(block: Block<in V>) {
        mapped.forEach(Block { document -> block.apply(mapper.apply(document)) })
    }

    override fun <A : MutableCollection<in V>> into(target: A): A {
        forEach(Block { v -> target.add(v) })
        return target
    }

    override fun batchSize(batchSize: Int): MappingIterable<U, V> {
        mapped.batchSize(batchSize)
        return this
    }

    override fun <W> map(newMap: Function<V, W>): MongoIterable<W> {
        return MappingIterable(this, newMap)
    }
}

/**
 *
 */
class MongoIterableTest : AllCategoriesKMongoBaseTest<Friend>() {

    class MongoCursorWrapper<T>(val mongoCursor: MongoCursor<T>) : MongoCursor<T> by mongoCursor {

        var closed = false

        override fun close() {
            mongoCursor.close()
            closed = true
        }
    }

    class MongoIterableWrapper<T>(val mongoIterable: MongoIterable<T>) : MongoIterable<T> by mongoIterable {

        var cursor: MongoCursorWrapper<T>? = null

        override fun iterator(): MongoCursorWrapper<T> {
            return MongoCursorWrapper(mongoIterable.iterator()).also { cursor = it }
        }

        override fun first(): T? {
            val cursor = iterator()
            try {
                return if (!cursor.hasNext()) {
                    null
                } else cursor.next()
            } finally {
                cursor.close()
            }
        }

        override fun forEach(block: Block<in T>) {
            val cursor = iterator()
            try {
                while (cursor.hasNext()) {
                    block.apply(cursor.next())
                }
            } finally {
                cursor.close()
            }
        }

        override fun <U> map(mapper: Function<T, U>): MongoIterable<U> {
            return MappingIterable(this, mapper)
        }

    }

    @Test
    fun `forEach close the mongo cursor`() {
        val john = Friend("John", "22 Wall Street Avenue")
        col.insertOne(john)
        val iterable = MongoIterableWrapper(col.find())
        val friends = mutableListOf<Friend>()
        iterable.forEach { friends.add(it) }

        assertTrue(iterable.cursor?.closed ?: false)

        assertEquals(john, friends.first())
        assertEquals(1, friends.size)
    }

    @Test
    fun `find close the mongo cursor`() {
        val john = Friend("John", "22 Wall Street Avenue")
        col.insertOne(john)
        val iterable = MongoIterableWrapper(col.find())

        assertEquals(john, iterable.find { true })
        assertTrue(iterable.cursor?.closed ?: false)
    }

    @Test
    fun `firstOrNull close the mongo cursor`() {
        val john = Friend("John", "22 Wall Street Avenue")
        col.insertOne(john)
        val iterable = MongoIterableWrapper(col.find())

        assertEquals(john, iterable.firstOrNull())
        assertTrue(iterable.cursor?.closed ?: false)
    }

    @Test
    fun `mapIndexed close the mongo cursor`() {
        val john = Friend("John", "22 Wall Street Avenue")
        col.insertOne(john)
        val iterable = MongoIterableWrapper(col.find())
        val indexed = iterable.mapIndexed { i, v -> v }

        assertEquals(john, indexed.first())
        assertTrue(iterable.cursor?.closed ?: false)
    }

    @Test
    fun `map does not close the mongo cursor`() {
        val john = Friend("John", "22 Wall Street Avenue")
        col.insertOne(john)
        val iterable = MongoIterableWrapper(col.find())
        val mapIt = iterable.map { it }
        assertFalse(iterable.cursor?.closed ?: false)

        assertEquals(john, mapIt.find { true })
        assertTrue(iterable.cursor?.closed ?: false)
    }


    @Test
    fun `asSequence closes the mongo cursor`() {
        val john = Friend("John", "22 Wall Street Avenue")
        col.insertOne(john)
        val iterable = MongoIterableWrapper(col.find())
        val sequence = iterable.asSequence()
        assertTrue(iterable.cursor?.closed ?: false)

        assertEquals(john, sequence.filter { it.name != "Joe" }.last())
    }

    @Test
    fun `toMap closes the mongo cursor`() {
        val john = Friend("John", "22 Wall Street Avenue")
        col.insertOne(john)
        val iterable = MongoIterableWrapper(col.find())
        val map = iterable.map { it._id to it }.toMap()
        assertTrue(iterable.cursor?.closed ?: false)

        assertEquals(john, map.values.first())
        assertEquals(john._id, map.keys.first())
    }

    @Test
    fun `toHashSet closes the mongo cursor`() {
        val john = Friend("John", "22 Wall Street Avenue")
        col.insertOne(john)
        val iterable = MongoIterableWrapper(col.find())
        val hashSet = iterable.toHashSet()
        assertTrue(iterable.cursor?.closed ?: false)

        assertEquals(john, hashSet.first())
    }
}