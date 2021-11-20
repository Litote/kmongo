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

package kotlin.collections

import com.mongodb.Function
import com.mongodb.ServerAddress
import com.mongodb.ServerCursor
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoIterable
import java.util.function.Consumer
import kotlin.internal.HidesMembers
import kotlin.internal.InlineOnly
import kotlin.internal.NoInfer
import kotlin.internal.OnlyInputTypes

//utility class & methods ->

/**
 * Utility class - this is not part of the KMongo public API.
 */
private class MongoCursorIterable<T>(private val cursor: MongoCursor<T>) : MongoCursor<T> by cursor, Iterable<T> {

    override fun iterator(): Iterator<T> = cursor
}

/**
 * Utility method - this is not part of the KMongo public API.
 */
private fun <T> MongoIterable<T>.kCursor(): MongoCursorIterable<T> = MongoCursorIterable(iterator())

/**
 * Utility method - this is not part of the KMongo public API.
 */
fun <T, R> MongoIterable<T>.useCursor(block: (Iterable<T>) -> R): R {
    return kCursor().use(block)
}

//specific overrides

/**
 * Overrides [Iterable.forEach] to ensure [MongoIterable.forEach] is called.
 *
 * @param
 */
@HidesMembers
inline fun <T> MongoIterable<T>.forEach(crossinline action: (T) -> Unit): Unit =
    forEach(Consumer { action.invoke(it) })

/**
 * Returns the first element, or `null` if the collection is empty.
 */
fun <T> MongoIterable<T>.firstOrNull(): T? {
    return first()
}

/**
 * Iterator transforming original `iterator` into iterator of [IndexedValue], counting index from zero.
 */
private class MongoIndexingIterator<T>(val iterator: MongoCursor<T>) : MongoCursor<IndexedValue<T>> {
    private var index = 0
    override fun hasNext(): Boolean = iterator.hasNext()
    override fun next(): IndexedValue<T> = IndexedValue(index++, iterator.next())
    override fun tryNext(): IndexedValue<T>? = iterator.tryNext()?.let { IndexedValue(index++, it) }
    override fun remove() = iterator.remove()
    override fun close() = iterator.close()
    override fun getServerCursor(): ServerCursor? = iterator.serverCursor
    override fun getServerAddress(): ServerAddress = iterator.serverAddress
    override fun available(): Int = iterator.available()
}

/**
 * A wrapper over another [Iterable] (or any other object that can produce an [Iterator]) that returns
 * an indexing iterator.
 */
private class MongoIndexingIterable<T>(
    private val iterable: MongoIterable<T>
) : MongoIterable<IndexedValue<T>> {

    override fun batchSize(batchSize: Int): MongoIterable<IndexedValue<T>> =
        MongoIndexingIterable(iterable.batchSize(batchSize))

    override fun <U : Any?> map(mapper: Function<IndexedValue<T>, U>): MongoIterable<U> {
        var index = 0
        return iterable.map { mapper.apply(IndexedValue(index++, it)) }
    }

    override fun forEach(action: Consumer<in IndexedValue<T>>) {
        var index = 0
        iterable.forEach {
            action.accept(IndexedValue(index++, it))
        }
    }

    override fun first(): IndexedValue<T> = IndexedValue(0, iterable.first())

    override fun <A : MutableCollection<in IndexedValue<T>>> into(target: A): A {
        val l = mutableListOf<T>()
        return target.apply { addAll(iterable.into(l).mapIndexed { i, v -> IndexedValue(i, v) }) }
    }

    override fun cursor(): MongoCursor<IndexedValue<T>> = iterator()

    override fun iterator(): MongoCursor<IndexedValue<T>> = MongoIndexingIterator(iterable.iterator())
}

/**
 * Returns a lazy [Iterable] of [IndexedValue] for each element of the original collection.
 */
fun <T> MongoIterable<T>.withIndex(): MongoIterable<IndexedValue<T>> {
    return MongoIndexingIterable(this)
}

/**
 * Returns an [Iterator] wrapping each value produced by this [Iterator] with the [IndexedValue],
 * containing value and it's index.
 * @sample samples.collections.Iterators.withIndexIterator
 */
fun <T> MongoCursor<T>.withIndex(): MongoCursor<IndexedValue<T>> =
    MongoIndexingIterator(this)

/**
 * Returns an original collection containing all the non-`null` elements, throwing an [IllegalArgumentException] if there are any `null` elements.
 */
fun <T : Any> MongoIterable<T?>.requireNoNulls(): Iterable<T> =
    toList().requireNoNulls()

/**
 * Creates a [Sequence] instance that wraps the original collection returning its elements when being iterated.
 *
 * @sample samples.collections.Sequences.Building.sequenceFromCollection
 */
fun <T> MongoIterable<T>.asSequence(): Sequence<T> {
    //lazy sequence
    return object : Sequence<T> {
        override fun iterator(): Iterator<T> = this@asSequence.toList().asSequence().iterator()
    }
}

//common overrides ->

/**
 * Returns `true` if [element] is found in the collection.
 */
operator fun <@OnlyInputTypes T> MongoIterable<T>.contains(element: T): Boolean {
    return useCursor { it.contains(element) }
}

/**
 * Returns an element at the given [index] or throws an [IndexOutOfBoundsException] if the [index] is out of bounds of this collection.
 */
fun <T> MongoIterable<T>.elementAt(index: Int): T {
    return useCursor { it.elementAt(index) }
}

/**
 * Returns an element at the given [index] or the result of calling the [defaultValue] function if the [index] is out of bounds of this collection.
 */
fun <T> MongoIterable<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T {
    return useCursor { it.elementAtOrElse(index, defaultValue) }
}

/**
 * Returns an element at the given [index] or `null` if the [index] is out of bounds of this collection.
 */
fun <T> MongoIterable<T>.elementAtOrNull(index: Int): T? {
    return useCursor { it.elementAtOrNull(index) }
}

/**
 * Returns the first element matching the given [predicate], or `null` if no such element was found.
 */
@InlineOnly
inline fun <T> MongoIterable<T>.find(crossinline predicate: (T) -> Boolean): T? {
    return useCursor { it.find(predicate) }
}

/**
 * Returns the last element matching the given [predicate], or `null` if no such element was found.
 */
@InlineOnly
inline fun <T> MongoIterable<T>.findLast(crossinline predicate: (T) -> Boolean): T? {
    return useCursor { it.findLast(predicate) }
}

/**
 * Returns the first element matching the given [predicate].
 * @throws [NoSuchElementException] if no such element is found.
 */
inline fun <T> MongoIterable<T>.first(crossinline predicate: (T) -> Boolean): T {
    return useCursor { it.first(predicate) }
}

/**
 * Returns the first element matching the given [predicate], or `null` if element was not found.
 */
inline fun <T> MongoIterable<T>.firstOrNull(crossinline predicate: (T) -> Boolean): T? {
    return useCursor { it.firstOrNull(predicate) }
}

/**
 * Returns first index of [element], or -1 if the collection does not contain element.
 */
fun <@OnlyInputTypes T> MongoIterable<T>.indexOf(element: T): Int {
    return useCursor { it.indexOf(element) }
}

/**
 * Returns index of the first element matching the given [predicate], or -1 if the collection does not contain such element.
 */
inline fun <T> MongoIterable<T>.indexOfFirst(crossinline predicate: (T) -> Boolean): Int {
    return useCursor { it.indexOfFirst(predicate) }
}

/**
 * Returns index of the last element matching the given [predicate], or -1 if the collection does not contain such element.
 */
inline fun <T> MongoIterable<T>.indexOfLast(crossinline predicate: (T) -> Boolean): Int {
    return useCursor { it.indexOfLast(predicate) }
}

/**
 * Returns the last element.
 * @throws [NoSuchElementException] if the collection is empty.
 */
fun <T> MongoIterable<T>.last(): T {
    return useCursor { it.last() }
}

/**
 * Returns the last element matching the given [predicate].
 * @throws [NoSuchElementException] if no such element is found.
 */
inline fun <T> MongoIterable<T>.last(crossinline predicate: (T) -> Boolean): T {
    return useCursor { it.last(predicate) }
}

/**
 * Returns last index of [element], or -1 if the collection does not contain element.
 */
fun <@OnlyInputTypes T> MongoIterable<T>.lastIndexOf(element: T): Int {
    return useCursor { it.lastIndexOf(element) }
}

/**
 * Returns the last element, or `null` if the collection is empty.
 */
fun <T> MongoIterable<T>.lastOrNull(): T? {
    return useCursor { it.lastOrNull() }
}

/**
 * Returns the last element matching the given [predicate], or `null` if no such element was found.
 */
inline fun <T> MongoIterable<T>.lastOrNull(crossinline predicate: (T) -> Boolean): T? {
    return useCursor { it.lastOrNull(predicate) }
}

/**
 * Returns the single element, or throws an exception if the collection is empty or has more than one element.
 */
fun <T> MongoIterable<T>.single(): T {
    return useCursor { it.single() }
}

/**
 * Returns the single element matching the given [predicate], or throws exception if there is no or more than one matching element.
 */
inline fun <T> MongoIterable<T>.single(crossinline predicate: (T) -> Boolean): T {
    return useCursor { it.single(predicate) }
}

/**
 * Returns single element, or `null` if the collection is empty or has more than one element.
 */
fun <T> MongoIterable<T>.singleOrNull(): T? {
    return useCursor { it.singleOrNull() }
}

/**
 * Returns the single element matching the given [predicate], or `null` if element was not found or more than one element was found.
 */
inline fun <T> MongoIterable<T>.singleOrNull(crossinline predicate: (T) -> Boolean): T? {
    return useCursor { it.singleOrNull(predicate) }
}

/**
 * Returns a list containing all elements except first [n] elements.
 *
 * @sample samples.collections.Collections.Transformations.drop
 */
fun <T> MongoIterable<T>.drop(n: Int): List<T> {
    return useCursor { it.drop(n) }
}

/**
 * Returns a list containing all elements except first elements that satisfy the given [predicate].
 *
 * @sample samples.collections.Collections.Transformations.drop
 */
inline fun <T> MongoIterable<T>.dropWhile(crossinline predicate: (T) -> Boolean): List<T> {
    return useCursor { it.dropWhile(predicate) }
}

/**
 * Returns a list containing only elements matching the given [predicate].
 */
inline fun <T> MongoIterable<T>.filter(crossinline predicate: (T) -> Boolean): List<T> {
    return useCursor { it.filter(predicate) }
}

/**
 * Returns a list containing only elements matching the given [predicate].
 * @param [predicate] function that takes the index of an element and the element itself
 * and returns the result of predicate evaluation on the element.
 */
inline fun <T> MongoIterable<T>.filterIndexed(crossinline predicate: (index: Int, T) -> Boolean): List<T> {
    return useCursor { it.filterIndexed(predicate) }
}

/**
 * Appends all elements matching the given [predicate] to the given [destination].
 * @param [predicate] function that takes the index of an element and the element itself
 * and returns the result of predicate evaluation on the element.
 */
inline fun <T, C : MutableCollection<in T>> MongoIterable<T>.filterIndexedTo(
    destination: C,
    crossinline predicate: (index: Int, T) -> Boolean
): C {
    return useCursor { it.filterIndexedTo(destination, predicate) }
}

/**
 * Returns a list containing all elements that are instances of specified type parameter R.
 */
inline fun <reified R> MongoIterable<*>.filterIsInstance(): List<@NoInfer R> {
    return useCursor { it.filterIsInstance<R>() }
}

/**
 * Appends all elements that are instances of specified type parameter R to the given [destination].
 */
inline fun <reified R, C : MutableCollection<in R>> MongoIterable<*>.filterIsInstanceTo(destination: C): C {
    return useCursor { it.filterIsInstanceTo(destination) }
}

/**
 * Returns a list containing all elements not matching the given [predicate].
 */
inline fun <T> MongoIterable<T>.filterNot(crossinline predicate: (T) -> Boolean): List<T> {
    return useCursor { it.filterNot(predicate) }
}

/**
 * Returns a list containing all elements that are not `null`.
 */
fun <T : Any> MongoIterable<T?>.filterNotNull(): List<T> {
    return useCursor { it.filterNotNull() }
}

/**
 * Appends all elements that are not `null` to the given [destination].
 */
fun <C : MutableCollection<in T>, T : Any> MongoIterable<T?>.filterNotNullTo(destination: C): C {
    return useCursor { it.filterNotNullTo(destination) }
}

/**
 * Appends all elements not matching the given [predicate] to the given [destination].
 */
inline fun <T, C : MutableCollection<in T>> MongoIterable<T>.filterNotTo(
    destination: C,
    crossinline predicate: (T) -> Boolean
): C {
    return useCursor { it.filterNotTo(destination, predicate) }
}

/**
 * Appends all elements matching the given [predicate] to the given [destination].
 */
inline fun <T, C : MutableCollection<in T>> MongoIterable<T>.filterTo(
    destination: C,
    crossinline predicate: (T) -> Boolean
): C {
    return useCursor { it.filterTo(destination, predicate) }
}

/**
 * Returns a list containing first [n] elements.
 *
 * @sample samples.collections.Collections.Transformations.take
 */
fun <T> MongoIterable<T>.take(n: Int): List<T> {
    return useCursor { it.take(n) }
}

/**
 * Returns a list containing first elements satisfying the given [predicate].
 *
 * @sample samples.collections.Collections.Transformations.take
 */
inline fun <T> MongoIterable<T>.takeWhile(crossinline predicate: (T) -> Boolean): List<T> {
    return useCursor { it.takeWhile(predicate) }
}

/**
 * Returns a list with elements in reversed order.
 */
fun <T> MongoIterable<T>.reversed(): List<T> {
    return useCursor { it.reversed() }
}

/**
 * Returns a list of all elements sorted according to their natural sort order.
 */
fun <T : Comparable<T>> MongoIterable<T>.sorted(): List<T> {
    return useCursor { it.sorted() }
}

/**
 * Returns a list of all elements sorted according to natural sort order of the value returned by specified [selector] function.
 */
inline fun <T, R : Comparable<R>> MongoIterable<T>.sortedBy(crossinline selector: (T) -> R?): List<T> {
    return useCursor { it.sortedBy(selector) }
}

/**
 * Returns a list of all elements sorted descending according to natural sort order of the value returned by specified [selector] function.
 */
inline fun <T, R : Comparable<R>> MongoIterable<T>.sortedByDescending(crossinline selector: (T) -> R?): List<T> {
    return useCursor { it.sortedByDescending(selector) }
}

/**
 * Returns a list of all elements sorted descending according to their natural sort order.
 */
fun <T : Comparable<T>> MongoIterable<T>.sortedDescending(): List<T> {
    return useCursor { it.sortedDescending() }
}

/**
 * Returns a list of all elements sorted according to the specified [comparator].
 */
fun <T> MongoIterable<T>.sortedWith(comparator: Comparator<in T>): List<T> {
    return useCursor { it.sortedWith(comparator) }
}

/**
 * Returns a [Map] containing key-value pairs provided by [transform] function
 * applied to elements of the given collection.
 *
 * If any of two pairs would have the same key the last one gets added to the map.
 *
 * The returned map preserves the entry iteration order of the original collection.
 */
inline fun <T, K, V> MongoIterable<T>.associate(crossinline transform: (T) -> Pair<K, V>): Map<K, V> {
    return useCursor { it.associate(transform) }
}

/**
 * Returns a [Map] containing the elements from the given collection indexed by the key
 * returned from [keySelector] function applied to each element.
 *
 * If any two elements would have the same key returned by [keySelector] the last one gets added to the map.
 *
 * The returned map preserves the entry iteration order of the original collection.
 */
inline fun <T, K> MongoIterable<T>.associateBy(crossinline keySelector: (T) -> K): Map<K, T> {
    return useCursor { it.associateBy(keySelector) }
}

/**
 * Returns a [Map] containing the values provided by [valueTransform] and indexed by [keySelector] functions applied to elements of the given collection.
 *
 * If any two elements would have the same key returned by [keySelector] the last one gets added to the map.
 *
 * The returned map preserves the entry iteration order of the original collection.
 */
inline fun <T, K, V> MongoIterable<T>.associateBy(
    crossinline keySelector: (T) -> K,
    crossinline valueTransform: (T) -> V
): Map<K, V> {
    return useCursor { it.associateBy(keySelector, valueTransform) }
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs,
 * where key is provided by the [keySelector] function applied to each element of the given collection
 * and value is the element itself.
 *
 * If any two elements would have the same key returned by [keySelector] the last one gets added to the map.
 */
inline fun <T, K, M : MutableMap<in K, in T>> MongoIterable<T>.associateByTo(
    destination: M,
    crossinline keySelector: (T) -> K
): M {
    return useCursor { it.associateByTo(destination, keySelector) }
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs,
 * where key is provided by the [keySelector] function and
 * and value is provided by the [valueTransform] function applied to elements of the given collection.
 *
 * If any two elements would have the same key returned by [keySelector] the last one gets added to the map.
 */
inline fun <T, K, V, M : MutableMap<in K, in V>> MongoIterable<T>.associateByTo(
    destination: M,
    crossinline keySelector: (T) -> K,
    crossinline valueTransform: (T) -> V
): M {
    return useCursor { it.associateByTo(destination, keySelector, valueTransform) }
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs
 * provided by [transform] function applied to each element of the given collection.
 *
 * If any of two pairs would have the same key the last one gets added to the map.
 */
inline fun <T, K, V, M : MutableMap<in K, in V>> MongoIterable<T>.associateTo(
    destination: M,
    crossinline transform: (T) -> Pair<K, V>
): M {
    return useCursor { it.associateTo(destination, transform) }
}

/**
 * Appends all elements to the given [destination] collection.
 */
fun <T, C : MutableCollection<in T>> MongoIterable<T>.toCollection(destination: C): C {
    return useCursor { it.toCollection(destination) }
}

/**
 * Returns a [HashSet] of all elements.
 */
fun <T> MongoIterable<T>.toHashSet(): HashSet<T> {
    return useCursor { it.toHashSet() }
}

/**
 * Returns a [List] containing all elements.
 */
fun <T> MongoIterable<T>.toList(): List<T> {
    return useCursor { it.toList() }
}

/**
 * Returns a [MutableList] filled with all elements of this collection.
 */
fun <T> MongoIterable<T>.toMutableList(): MutableList<T> {
    return useCursor { it.toMutableList() }
}

/**
 * Returns a [Set] of all elements.
 *
 * The returned set preserves the element iteration order of the original collection.
 */
fun <T> MongoIterable<T>.toSet(): Set<T> {
    return useCursor { it.toSet() }
}

/**
 * Returns a single list of all elements yielded from results of [transform] function being invoked on each element of original collection.
 */
inline fun <T, R> MongoIterable<T>.flatMap(crossinline transform: (T) -> MongoIterable<R>): List<R> {
    return useCursor { it.flatMap(transform) }
}

/**
 * Appends all elements yielded from results of [transform] function being invoked on each element of original collection, to the given [destination].
 */
inline fun <T, R, C : MutableCollection<in R>> MongoIterable<T>.flatMapTo(
    destination: C,
    crossinline transform: (T) -> MongoIterable<R>
): C {
    return useCursor { it.flatMapTo(destination, transform) }
}

/**
 * Groups elements of the original collection by the key returned by the given [keySelector] function
 * applied to each element and returns a map where each group key is associated with a list of corresponding elements.
 *
 * The returned map preserves the entry iteration order of the keys produced from the original collection.
 *
 * @sample samples.collections.Collections.Transformations.groupBy
 */
inline fun <T, K> MongoIterable<T>.groupBy(crossinline keySelector: (T) -> K): Map<K, List<T>> {
    return useCursor { it.groupBy(keySelector) }
}

/**
 * Groups values returned by the [valueTransform] function applied to each element of the original collection
 * by the key returned by the given [keySelector] function applied to the element
 * and returns a map where each group key is associated with a list of corresponding values.
 *
 * The returned map preserves the entry iteration order of the keys produced from the original collection.
 *
 * @sample samples.collections.Collections.Transformations.groupByKeysAndValues
 */
inline fun <T, K, V> MongoIterable<T>.groupBy(
    crossinline keySelector: (T) -> K,
    crossinline valueTransform: (T) -> V
): Map<K, List<V>> {
    return useCursor { it.groupBy(keySelector, valueTransform) }
}

/**
 * Groups elements of the original collection by the key returned by the given [keySelector] function
 * applied to each element and puts to the [destination] map each group key associated with a list of corresponding elements.
 *
 * @return The [destination] map.
 *
 * @sample samples.collections.Collections.Transformations.groupBy
 */
inline fun <T, K, M : MutableMap<in K, MutableList<T>>> MongoIterable<T>.groupByTo(
    destination: M,
    crossinline keySelector: (T) -> K
): M {
    return useCursor { it.groupByTo(destination, keySelector) }
}

/**
 * Groups values returned by the [valueTransform] function applied to each element of the original collection
 * by the key returned by the given [keySelector] function applied to the element
 * and puts to the [destination] map each group key associated with a list of corresponding values.
 *
 * @return The [destination] map.
 *
 * @sample samples.collections.Collections.Transformations.groupByKeysAndValues
 */
inline fun <T, K, V, M : MutableMap<in K, MutableList<V>>> MongoIterable<T>.groupByTo(
    destination: M,
    crossinline keySelector: (T) -> K,
    crossinline valueTransform: (T) -> V
): M {
    return useCursor { it.groupByTo(destination, keySelector, valueTransform) }
}

/**
 * Creates a [Grouping] source from a collection to be used later with one of group-and-fold operations
 * using the specified [keySelector] function to extract a key from each element.
 *
 * @sample samples.collections.Collections.Transformations.groupingByEachCount
 */
@SinceKotlin("1.1")
inline fun <T, K> MongoIterable<T>.groupingBy(crossinline keySelector: (T) -> K): Grouping<T, K> {
    return useCursor { it.groupingBy(keySelector) }
}

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element in the original collection.
 */
inline fun <T, R> MongoIterable<T>.map(crossinline transform: (T) -> R): List<R> {
    return useCursor { it.map(transform) }
}

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element and its index in the original collection.
 * @param [transform] function that takes the index of an element and the element itself
 * and returns the result of the transform applied to the element.
 */
inline fun <T, R> MongoIterable<T>.mapIndexed(crossinline transform: (index: Int, T) -> R): List<R> {
    return useCursor { it.mapIndexed(transform) }
}

/**
 * Returns a list containing only the non-null results of applying the given [transform] function
 * to each element and its index in the original collection.
 * @param [transform] function that takes the index of an element and the element itself
 * and returns the result of the transform applied to the element.
 */
inline fun <T, R : Any> MongoIterable<T>.mapIndexedNotNull(crossinline transform: (index: Int, T) -> R?): List<R> {
    return useCursor { it.mapIndexedNotNull(transform) }
}

/**
 * Applies the given [transform] function to each element and its index in the original collection
 * and appends only the non-null results to the given [destination].
 * @param [transform] function that takes the index of an element and the element itself
 * and returns the result of the transform applied to the element.
 */
inline fun <T, R : Any, C : MutableCollection<in R>> MongoIterable<T>.mapIndexedNotNullTo(
    destination: C,
    crossinline transform: (index: Int, T) -> R?
): C {
    return useCursor { it.mapIndexedNotNullTo(destination, transform) }
}

/**
 * Applies the given [transform] function to each element and its index in the original collection
 * and appends the results to the given [destination].
 * @param [transform] function that takes the index of an element and the element itself
 * and returns the result of the transform applied to the element.
 */
inline fun <T, R, C : MutableCollection<in R>> MongoIterable<T>.mapIndexedTo(
    destination: C,
    crossinline transform: (index: Int, T) -> R
): C {
    return useCursor { it.mapIndexedTo(destination, transform) }
}

/**
 * Returns a list containing only the non-null results of applying the given [transform] function
 * to each element in the original collection.
 */
inline fun <T, R : Any> MongoIterable<T>.mapNotNull(crossinline transform: (T) -> R?): List<R> {
    return useCursor { it.mapNotNull(transform) }
}

/**
 * Applies the given [transform] function to each element in the original collection
 * and appends only the non-null results to the given [destination].
 */
inline fun <T, R : Any, C : MutableCollection<in R>> MongoIterable<T>.mapNotNullTo(
    destination: C,
    crossinline transform: (T) -> R?
): C {
    return useCursor { it.mapNotNullTo(destination, transform) }
}

/**
 * Applies the given [transform] function to each element of the original collection
 * and appends the results to the given [destination].
 */
inline fun <T, R, C : MutableCollection<in R>> MongoIterable<T>.mapTo(
    destination: C,
    crossinline transform: (T) -> R
): C {
    return useCursor { it.mapTo(destination, transform) }
}


/**
 * Returns a list containing only distinct elements from the given collection.
 *
 * The elements in the resulting list are in the same order as they were in the source collection.
 */
fun <T> MongoIterable<T>.distinct(): List<T> {
    return useCursor { it.distinct() }
}

/**
 * Returns a list containing only elements from the given collection
 * having distinct keys returned by the given [selector] function.
 *
 * The elements in the resulting list are in the same order as they were in the source collection.
 */
inline fun <T, K> MongoIterable<T>.distinctBy(crossinline selector: (T) -> K): List<T> {
    return useCursor { it.distinctBy(selector) }
}

/**
 * Returns a set containing all elements that are contained by both this set and the specified collection.
 *
 * The returned set preserves the element iteration order of the original collection.
 */
infix fun <T> MongoIterable<T>.intersect(other: Iterable<T>): Set<T> {
    return useCursor { it.intersect(other) }
}

/**
 * Returns a set containing all elements that are contained by this collection and not contained by the specified collection.
 *
 * The returned set preserves the element iteration order of the original collection.
 */
infix fun <T> MongoIterable<T>.subtract(other: Iterable<T>): Set<T> {
    return useCursor { it.subtract(other) }
}

/**
 * Returns a mutable set containing all distinct elements from the given collection.
 *
 * The returned set preserves the element iteration order of the original collection.
 */
fun <T> MongoIterable<T>.toMutableSet(): MutableSet<T> {
    return useCursor { it.toMutableSet() }
}

/**
 * Returns a set containing all distinct elements from both collections.
 *
 * The returned set preserves the element iteration order of the original collection.
 * Those elements of the [other] collection that are unique are iterated in the end
 * in the order of the [other] collection.
 */
infix fun <T> MongoIterable<T>.union(other: Iterable<T>): Set<T> {
    return useCursor { it.union(other) }
}

/**
 * Returns `true` if all elements match the given [predicate].
 *
 * @sample samples.collections.Collections.Aggregates.all
 */
inline fun <T> MongoIterable<T>.all(crossinline predicate: (T) -> Boolean): Boolean {
    return useCursor { it.all(predicate) }
}

/**
 * Returns `true` if collection has at least one element.
 *
 * @sample samples.collections.Collections.Aggregates.any
 */
fun <T> MongoIterable<T>.any(): Boolean {
    return useCursor { it.any() }
}

/**
 * Returns `true` if at least one element matches the given [predicate].
 *
 * @sample samples.collections.Collections.Aggregates.anyWithPredicate
 */
inline fun <T> MongoIterable<T>.any(crossinline predicate: (T) -> Boolean): Boolean {
    return useCursor { it.any(predicate) }
}

/**
 * Returns the number of elements in this collection.
 */
fun <T> MongoIterable<T>.count(): Int {
    return useCursor { it.count() }
}

/**
 * Returns the number of elements matching the given [predicate].
 */
inline fun <T> MongoIterable<T>.count(crossinline predicate: (T) -> Boolean): Int {
    return useCursor { it.count(predicate) }
}

/**
 * Accumulates value starting with [initial] value and applying [operation] from left to right to current accumulator value and each element.
 */
inline fun <T, R> MongoIterable<T>.fold(initial: R, crossinline operation: (acc: R, T) -> R): R {
    return useCursor { it.fold(initial, operation) }
}

/**
 * Accumulates value starting with [initial] value and applying [operation] from left to right
 * to current accumulator value and each element with its index in the original collection.
 * @param [operation] function that takes the index of an element, current accumulator value
 * and the element itself, and calculates the next accumulator value.
 */
inline fun <T, R> MongoIterable<T>.foldIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): R {
    return useCursor { it.foldIndexed(initial, operation) }
}

/**
 * Performs the given [action] on each element, providing sequential index with the element.
 * @param [action] function that takes the index of an element and the element itself
 * and performs the desired action on the element.
 */
inline fun <T> MongoIterable<T>.forEachIndexed(crossinline action: (index: Int, T) -> Unit): Unit {
    return useCursor { it.forEachIndexed(action) }
}

/**
 * Returns the largest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
@SinceKotlin("1.1")
fun MongoIterable<Double>.max(): Double? {
    return useCursor { it.maxOrNull() }
}

/**
 * Returns the largest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
@SinceKotlin("1.1")
fun MongoIterable<Float>.max(): Float? {
    return useCursor { it.maxOrNull() }
}

/**
 * Returns the largest element or `null` if there are no elements.
 */
fun <T : Comparable<T>> MongoIterable<T>.max(): T? {
    return useCursor { it.maxOrNull() }
}

/**
 * Returns the first element yielding the largest value of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> MongoIterable<T>.maxBy(crossinline selector: (T) -> R): T? {
    return useCursor { it.maxByOrNull(selector) }
}

/**
 * Returns the first element having the largest value according to the provided [comparator] or `null` if there are no elements.
 */
fun <T> MongoIterable<T>.maxWith(comparator: Comparator<in T>): T? {
    return useCursor { it.maxWithOrNull(comparator) }
}

/**
 * Returns the smallest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
@SinceKotlin("1.1")
fun MongoIterable<Double>.min(): Double? {
    return useCursor { it.minOrNull() }
}

/**
 * Returns the smallest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
@SinceKotlin("1.1")
fun MongoIterable<Float>.min(): Float? {
    return useCursor { it.minOrNull() }
}

/**
 * Returns the smallest element or `null` if there are no elements.
 */
fun <T : Comparable<T>> MongoIterable<T>.min(): T? {
    return useCursor { it.minOrNull() }
}

/**
 * Returns the first element yielding the smallest value of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> MongoIterable<T>.minBy(crossinline selector: (T) -> R): T? {
    return useCursor { it.minByOrNull(selector) }
}

/**
 * Returns the first element having the smallest value according to the provided [comparator] or `null` if there are no elements.
 */
fun <T> MongoIterable<T>.minWith(comparator: Comparator<in T>): T? {
    return useCursor { it.minWithOrNull(comparator) }
}

/**
 * Returns the largest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
@SinceKotlin("1.4")
fun MongoIterable<Double>.maxOrNull(): Double? {
    return useCursor { it.maxOrNull() }
}

/**
 * Returns the largest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
@SinceKotlin("1.4")
fun MongoIterable<Float>.maxOrNull(): Float? {
    return useCursor { it.maxOrNull() }
}

/**
 * Returns the largest element or `null` if there are no elements.
 */
fun <T : Comparable<T>> MongoIterable<T>.maxOrNull(): T? {
    return useCursor { it.maxOrNull() }
}

/**
 * Returns the first element yielding the largest value of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> MongoIterable<T>.maxByOrNull(crossinline selector: (T) -> R): T? {
    return useCursor { it.maxByOrNull(selector) }
}

/**
 * Returns the first element having the largest value according to the provided [comparator] or `null` if there are no elements.
 */
fun <T> MongoIterable<T>.maxWithOrNull(comparator: Comparator<in T>): T? {
    return useCursor { it.maxWithOrNull(comparator) }
}

/**
 * Returns the smallest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
@SinceKotlin("1.4")
fun MongoIterable<Double>.minOrNull(): Double? {
    return useCursor { it.minOrNull() }
}

/**
 * Returns the smallest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
@SinceKotlin("1.4")
fun MongoIterable<Float>.minOrNull(): Float? {
    return useCursor { it.minOrNull() }
}

/**
 * Returns the smallest element or `null` if there are no elements.
 */
fun <T : Comparable<T>> MongoIterable<T>.minOrNull(): T? {
    return useCursor { it.minOrNull() }
}

/**
 * Returns the first element yielding the smallest value of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> MongoIterable<T>.minByOrNull(crossinline selector: (T) -> R): T? {
    return useCursor { it.minByOrNull(selector) }
}

/**
 * Returns the first element having the smallest value according to the provided [comparator] or `null` if there are no elements.
 */
fun <T> MongoIterable<T>.minWithOrNull(comparator: Comparator<in T>): T? {
    return useCursor { it.minWithOrNull(comparator) }
}

/**
 * Returns `true` if the collection has no elements.
 *
 * @sample samples.collections.Collections.Aggregates.none
 */
fun <T> MongoIterable<T>.none(): Boolean {
    return useCursor { it.none() }
}

/**
 * Returns `true` if no elements match the given [predicate].
 *
 * @sample samples.collections.Collections.Aggregates.noneWithPredicate
 */
inline fun <T> MongoIterable<T>.none(crossinline predicate: (T) -> Boolean): Boolean {
    return useCursor { it.none(predicate) }
}

/**
 * Accumulates value starting with the first element and applying [operation] from left to right to current accumulator value and each element.
 */
inline fun <S, T : S> MongoIterable<T>.reduce(crossinline operation: (acc: S, T) -> S): S {
    return useCursor { it.reduce(operation) }
}

/**
 * Accumulates value starting with the first element and applying [operation] from left to right
 * to current accumulator value and each element with its index in the original collection.
 * @param [operation] function that takes the index of an element, current accumulator value
 * and the element itself and calculates the next accumulator value.
 */
inline fun <S, T : S> MongoIterable<T>.reduceIndexed(crossinline operation: (index: Int, acc: S, T) -> S): S {
    return useCursor { it.reduceIndexed(operation) }
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
inline fun <T> MongoIterable<T>.sumBy(crossinline selector: (T) -> Int): Int {
    return useCursor { it.sumBy(selector) }
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
inline fun <T> MongoIterable<T>.sumByDouble(crossinline selector: (T) -> Double): Double {
    return useCursor { it.sumByDouble(selector) }
}

/**
 * Splits this collection into a list of lists each not exceeding the given [size].
 *
 * The last list in the resulting list may have less elements than the given [size].
 *
 * @param size the number of elements to take in each list, must be positive and can be greater than the number of elements in this collection.
 *
 * @sample samples.collections.Collections.Transformations.chunked
 */
@SinceKotlin("1.2")
fun <T> MongoIterable<T>.chunked(size: Int): List<List<T>> {
    return useCursor { it.chunked(size) }
}

/**
 * Splits this collection into several lists each not exceeding the given [size]
 * and applies the given [transform] function to an each.
 *
 * @return list of results of the [transform] applied to an each list.
 *
 * Note that the list passed to the [transform] function is ephemeral and is valid only inside that function.
 * You should not store it or allow it to escape in some way, unless you made a snapshot of it.
 * The last list may have less elements than the given [size].
 *
 * @param size the number of elements to take in each list, must be positive and can be greater than the number of elements in this collection.
 *
 * @sample samples.text.Strings.chunkedTransform
 */
@SinceKotlin("1.2")
fun <T, R> MongoIterable<T>.chunked(size: Int, transform: (List<T>) -> R): List<R> {
    return useCursor { it.chunked(size, transform) }
}

/**
 * Returns a list containing all elements of the original collection without the first occurrence of the given [element].
 */
operator fun <T> MongoIterable<T>.minus(element: T): List<T> {
    return useCursor { it.minus(element) }
}

/**
 * Returns a list containing all elements of the original collection except the elements contained in the given [elements] array.
 */
operator fun <T> MongoIterable<T>.minus(elements: Array<out T>): List<T> {
    return useCursor { it.minus(elements) }
}

/**
 * Returns a list containing all elements of the original collection except the elements contained in the given [elements] collection.
 */
operator fun <T> MongoIterable<T>.minus(elements: Iterable<T>): List<T> {
    return useCursor { it.minus(elements) }
}

/**
 * Returns a list containing all elements of the original collection except the elements contained in the given [elements] sequence.
 */
operator fun <T> MongoIterable<T>.minus(elements: Sequence<T>): List<T> {
    return useCursor { it.minus(elements) }
}

/**
 * Returns a list containing all elements of the original collection without the first occurrence of the given [element].
 */
@InlineOnly
inline fun <T> MongoIterable<T>.minusElement(element: T): List<T> {
    return useCursor { it.minusElement(element) }
}

/**
 * Splits the original collection into pair of lists,
 * where *first* list contains elements for which [predicate] yielded `true`,
 * while *second* list contains elements for which [predicate] yielded `false`.
 */
inline fun <T> MongoIterable<T>.partition(crossinline predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    return useCursor { it.partition(predicate) }
}

/**
 * Returns a list containing all elements of the original collection and then the given [element].
 */
operator fun <T> MongoIterable<T>.plus(element: T): List<T> {
    return useCursor { it.plus(element) }
}

/**
 * Returns a list containing all elements of the original collection and then all elements of the given [elements] array.
 */
operator fun <T> MongoIterable<T>.plus(elements: Array<out T>): List<T> {
    return useCursor { it.plus(elements) }
}

/**
 * Returns a list containing all elements of the original collection and then all elements of the given [elements] collection.
 */
operator fun <T> MongoIterable<T>.plus(elements: Iterable<T>): List<T> {
    return useCursor { it.plus(elements) }
}

/**
 * Returns a list containing all elements of the original collection and then all elements of the given [elements] sequence.
 */
operator fun <T> MongoIterable<T>.plus(elements: Sequence<T>): List<T> {
    return useCursor { it.plus(elements) }
}

/**
 * Returns a list containing all elements of the original collection and then the given [element].
 */
@InlineOnly
inline fun <T> MongoIterable<T>.plusElement(element: T): List<T> {
    return useCursor { it.plusElement(element) }
}

/**
 * Returns a list of snapshots of the window of the given [size]
 * sliding along this collection with the given [step], where each
 * snapshot is a list.
 *
 * Several last lists may have less elements than the given [size].
 *
 * Both [size] and [step] must be positive and can be greater than the number of elements in this collection.
 * @param size the number of elements to take in each window
 * @param step the number of elements to move the window forward by on an each step, by default 1
 * @param partialWindows controls whether or not to keep partial windows in the end if any,
 * by default `false` which means partial windows won't be preserved
 *
 * @sample samples.collections.Sequences.Transformations.takeWindows
 */
@SinceKotlin("1.2")
fun <T> MongoIterable<T>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false): List<List<T>> {
    return useCursor { it.windowed(size, step, partialWindows) }
}

/**
 * Returns a list of results of applying the given [transform] function to
 * an each list representing a view over the window of the given [size]
 * sliding along this collection with the given [step].
 *
 * Note that the list passed to the [transform] function is ephemeral and is valid only inside that function.
 * You should not store it or allow it to escape in some way, unless you made a snapshot of it.
 * Several last lists may have less elements than the given [size].
 *
 * Both [size] and [step] must be positive and can be greater than the number of elements in this collection.
 * @param size the number of elements to take in each window
 * @param step the number of elements to move the window forward by on an each step, by default 1
 * @param partialWindows controls whether or not to keep partial windows in the end if any,
 * by default `false` which means partial windows won't be preserved
 *
 * @sample samples.collections.Sequences.Transformations.averageWindows
 */
@SinceKotlin("1.2")
fun <T, R> MongoIterable<T>.windowed(
    size: Int,
    step: Int = 1,
    partialWindows: Boolean = false,
    transform: (List<T>) -> R
): List<R> {
    return useCursor { it.windowed(size, step, partialWindows, transform) }
}

/**
 * Returns a list of pairs built from the elements of `this` collection and the [other] array with the same index.
 * The returned list has length of the shortest collection.
 *
 * @sample samples.collections.Iterables.Operations.zipIterable
 */
infix fun <T, R> MongoIterable<T>.zip(other: Array<out R>): List<Pair<T, R>> {
    return useCursor { it.zip(other) }
}

/**
 * Returns a list of values built from the elements of `this` collection and the [other] array with the same index
 * using the provided [transform] function applied to each pair of elements.
 * The returned list has length of the shortest collection.
 *
 * @sample samples.collections.Iterables.Operations.zipIterableWithTransform
 */
inline fun <T, R, V> MongoIterable<T>.zip(other: Array<out R>, crossinline transform: (a: T, b: R) -> V): List<V> {
    return useCursor { it.zip(other, transform) }
}

/**
 * Returns a list of pairs built from the elements of `this` collection and [other] collection with the same index.
 * The returned list has length of the shortest collection.
 *
 * @sample samples.collections.Iterables.Operations.zipIterable
 */
infix fun <T, R> MongoIterable<T>.zip(other: Iterable<R>): List<Pair<T, R>> {
    return useCursor { it.zip(other) }
}

/**
 * Returns a list of values built from the elements of `this` collection and the [other] collection with the same index
 * using the provided [transform] function applied to each pair of elements.
 * The returned list has length of the shortest collection.
 *
 * @sample samples.collections.Iterables.Operations.zipIterableWithTransform
 */
inline fun <T, R, V> MongoIterable<T>.zip(other: Iterable<R>, crossinline transform: (a: T, b: R) -> V): List<V> {
    return useCursor { it.zip(other, transform) }
}

/**
 * Returns a list of pairs of each two adjacent elements in this collection.
 *
 * The returned list is empty if this collection contains less than two elements.
 *
 * @sample samples.collections.Collections.Transformations.zipWithNext
 */
@SinceKotlin("1.2")
fun <T> MongoIterable<T>.zipWithNext(): List<Pair<T, T>> {
    return useCursor { it.zipWithNext() }
}

/**
 * Returns a list containing the results of applying the given [transform] function
 * to an each pair of two adjacent elements in this collection.
 *
 * The returned list is empty if this collection contains less than two elements.
 *
 * @sample samples.collections.Collections.Transformations.zipWithNextToFindDeltas
 */
@SinceKotlin("1.2")
inline fun <T, R> MongoIterable<T>.zipWithNext(crossinline transform: (a: T, b: T) -> R): List<R> {
    return useCursor { it.zipWithNext(transform) }
}

/**
 * Appends the string from all the elements separated using [separator] and using the given [prefix] and [postfix] if supplied.
 *
 * If the collection could be huge, you can specify a non-negative value of [limit], in which case only the first [limit]
 * elements will be appended, followed by the [truncated] string (which defaults to "...").
 *
 * @sample samples.collections.Collections.Transformations.joinTo
 */
fun <T, A : Appendable> MongoIterable<T>.joinTo(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): A {
    return useCursor { it.joinTo(buffer, separator, prefix, postfix, limit, truncated, transform) }
}

/**
 * Creates a string from all the elements separated using [separator] and using the given [prefix] and [postfix] if supplied.
 *
 * If the collection could be huge, you can specify a non-negative value of [limit], in which case only the first [limit]
 * elements will be appended, followed by the [truncated] string (which defaults to "...").
 *
 * @sample samples.collections.Collections.Transformations.joinToString
 */
fun <T> MongoIterable<T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
    return useCursor { it.joinToString(separator, prefix, postfix, limit, truncated, transform) }
}


/**
 * Returns an average value of elements in the collection.
 */
@kotlin.jvm.JvmName("averageOfByte")
fun MongoIterable<Byte>.average(): Double {
    return useCursor { it.average() }
}

/**
 * Returns an average value of elements in the collection.
 */
@kotlin.jvm.JvmName("averageOfShort")
fun MongoIterable<Short>.average(): Double {
    return useCursor { it.average() }
}

/**
 * Returns an average value of elements in the collection.
 */
@kotlin.jvm.JvmName("averageOfInt")
fun MongoIterable<Int>.average(): Double {
    return useCursor { it.average() }
}

/**
 * Returns an average value of elements in the collection.
 */
@kotlin.jvm.JvmName("averageOfLong")
fun MongoIterable<Long>.average(): Double {
    return useCursor { it.average() }
}

/**
 * Returns an average value of elements in the collection.
 */
@kotlin.jvm.JvmName("averageOfFloat")
fun MongoIterable<Float>.average(): Double {
    return useCursor { it.average() }
}

/**
 * Returns an average value of elements in the collection.
 */
@kotlin.jvm.JvmName("averageOfDouble")
fun MongoIterable<Double>.average(): Double {
    return useCursor { it.average() }
}

/**
 * Returns the sum of all elements in the collection.
 */
@kotlin.jvm.JvmName("sumOfByte")
fun MongoIterable<Byte>.sum(): Int {
    return useCursor { it.sum() }
}

/**
 * Returns the sum of all elements in the collection.
 */
@kotlin.jvm.JvmName("sumOfShort")
fun MongoIterable<Short>.sum(): Int {
    return useCursor { it.sum() }
}

/**
 * Returns the sum of all elements in the collection.
 */
@kotlin.jvm.JvmName("sumOfInt")
fun MongoIterable<Int>.sum(): Int {
    return useCursor { it.sum() }
}

/**
 * Returns the sum of all elements in the collection.
 */
@kotlin.jvm.JvmName("sumOfLong")
fun MongoIterable<Long>.sum(): Long {
    return useCursor { it.sum() }
}

/**
 * Returns the sum of all elements in the collection.
 */
@kotlin.jvm.JvmName("sumOfFloat")
fun MongoIterable<Float>.sum(): Float {
    return useCursor { it.sum() }
}

/**
 * Returns the sum of all elements in the collection.
 */
@kotlin.jvm.JvmName("sumOfDouble")
fun MongoIterable<Double>.sum(): Double {
    return useCursor { it.sum() }
}

/**
 * Returns a single list of all elements from all collections in the given collection.
 * @sample samples.collections.Iterables.Operations.flattenIterable
 */
fun <T> MongoIterable<Iterable<T>>.flatten(): List<T> {
    return useCursor { it.flatten() }
}

/**
 * Returns a pair of lists, where
 * *first* list is built from the first values of each pair from this collection,
 * *second* list is built from the second values of each pair from this collection.
 * @sample samples.collections.Iterables.Operations.unzipIterable
 */
fun <T, R> MongoIterable<Pair<T, R>>.unzip(): Pair<List<T>, List<R>> {
    return useCursor { it.unzip() }
}

/**
 * Returns a new map containing all key-value pairs from the given collection of pairs.
 *
 * The returned map preserves the entry iteration order of the original collection.
 */
fun <K, V> MongoIterable<Pair<K, V>>.toMap(): Map<K, V> {
    return useCursor { it.toMap() }
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs from the given collection of pairs.
 */
fun <K, V, M : MutableMap<in K, in V>> MongoIterable<Pair<K, V>>.toMap(destination: M): M =
    useCursor { it.toMap(destination) }

/**
 * Returns a list containing all elements that are instances of specified class.
 */
fun <R> MongoIterable<*>.filterIsInstance(klass: Class<R>): List<R> {
    return useCursor { it.filterIsInstance(klass) }
}

/**
 * Appends all elements that are instances of specified class to the given [destination].
 */
fun <C : MutableCollection<in R>, R> MongoIterable<*>.filterIsInstanceTo(destination: C, klass: Class<R>): C {
    return useCursor { it.filterIsInstanceTo(destination, klass) }
}

/**
 * Returns a [SortedSet][java.util.SortedSet] of all elements.
 */
fun <T : Comparable<T>> MongoIterable<T>.toSortedSet(): java.util.SortedSet<T> {
    return useCursor { it.toSortedSet() }
}

/**
 * Returns a [SortedSet][java.util.SortedSet] of all elements.
 *
 * Elements in the set returned are sorted according to the given [comparator].
 */
fun <T> MongoIterable<T>.toSortedSet(comparator: Comparator<in T>): java.util.SortedSet<T> {
    return useCursor { it.toSortedSet(comparator) }
}

/**
 * Returns a new list with the elements of this list randomly shuffled.
 */
@SinceKotlin("1.2")
fun <T> MongoIterable<T>.shuffled(): List<T> = useCursor { it.shuffled() }

/**
 * Returns a new list with the elements of this list randomly shuffled
 * using the specified [random] instance as the source of randomness.
 */
@SinceKotlin("1.2")
fun <T> MongoIterable<T>.shuffled(random: java.util.Random): List<T> = useCursor { it.shuffled(random) }

