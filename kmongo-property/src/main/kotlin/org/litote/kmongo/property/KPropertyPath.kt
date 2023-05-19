/*
 * Copyright (C) 2016/2022 Litote
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

package org.litote.kmongo.property

import org.litote.kmongo.path
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVisibility

/**
 * A property path, operations on which take one receiver as a parameter.
 *
 * @param T the type of the receiver which should be used to obtain the value of the property.
 * @param R the type of the property.
 */
open class KPropertyPath<T, R>(
    private val previous: KPropertyPath<T, *>?,
    private val property: KProperty1<*, R?>
) : KProperty1<T, R> {

    @Suppress("UNCHECKED_CAST")
    internal constructor(previous: KProperty1<*, Any?>, property: KProperty1<*, R?>) :
            this(
                if (previous is KPropertyPath<*, *>) {
                    previous as KPropertyPath<T, *>?
                } else {
                    KPropertyPath<T, Any?>(
                        null as (KPropertyPath<T, *>?),
                        previous
                    )
                },
                property
            )

    internal val path: String
        get() = "${previous?.path?.let { "$it." } ?: ""}${property.path()}"

    override val annotations: List<Annotation> get() = property.annotations
    override val getter: KProperty1.Getter<T, R> get() = notImplemented()
    override val isAbstract: Boolean get() = previous?.isAbstract ?: false || property.isAbstract
    override val isConst: Boolean get() = previous?.isConst ?: false && property.isConst
    override val isFinal: Boolean get() = previous?.isFinal ?: false && property.isFinal
    override val isLateinit: Boolean get() = previous?.isLateinit ?: false && property.isLateinit
    override val isOpen: Boolean get() = previous?.isOpen ?: false && property.isOpen
    override val isSuspend: Boolean get() = property.isSuspend
    override val name: String get() = path
    override val parameters: List<KParameter> get() = property.parameters
    override val returnType: KType get() = property.returnType
    override val typeParameters: List<KTypeParameter> get() = property.typeParameters
    override val visibility: KVisibility? get() = property.visibility
    override fun invoke(p1: T): R = notImplemented()
    override fun call(vararg args: Any?): R = notImplemented()
    override fun callBy(args: Map<KParameter, Any?>): R = notImplemented()
    override fun get(receiver: T): R = notImplemented()
    override fun getDelegate(receiver: T): Any? = notImplemented()

    companion object {

        private fun notImplemented(): Nothing = error("not implemented")

        private class CustomProperty<T, R>(val previous: KPropertyPath<*, T>, path: String) : KProperty1<T, R> {
            override val annotations: List<Annotation> get() = emptyList()

            override val getter: KProperty1.Getter<T, R> get() = notImplemented()
            override val isAbstract: Boolean get() = previous.isAbstract
            override val isConst: Boolean get() = previous.isConst
            override val isFinal: Boolean get() = previous.isFinal
            override val isLateinit: Boolean get() = previous.isLateinit
            override val isOpen: Boolean get() = previous.isOpen
            override val isSuspend: Boolean get() = previous.isSuspend
            override val name: String = path
            override val parameters: List<KParameter> get() = previous.parameters
            override val returnType: KType get() = notImplemented()
            override val typeParameters: List<KTypeParameter> get() = previous.typeParameters
            override val visibility: KVisibility? get() = previous.visibility
            override fun call(vararg args: Any?): R = notImplemented()
            override fun callBy(args: Map<KParameter, Any?>): R = notImplemented()
            override fun get(receiver: T): R = notImplemented()
            override fun getDelegate(receiver: T): Any? = notImplemented()
            override fun invoke(p1: T): R = notImplemented()
        }

        /**
         * Provides "fake" property with custom name.
         */
        fun <T, R> customProperty(previous: KPropertyPath<*, T>, path: String): KProperty1<T, R?> =
            CustomProperty(previous, path)
    }
}

/**
 * Base class for collection property path.
 */
open class KCollectionPropertyPath<T, R, MEMBER : KPropertyPath<T, R?>>(
    previous: KPropertyPath<T, *>?,
    property: KProperty1<*, Iterable<R>?>
) : KPropertyPath<T, Iterable<R>?>(previous, property) {

    /**
     * To be overridden to return the right type.
     */
    @Suppress("UNCHECKED_CAST")
    open fun memberWithAdditionalPath(additionalPath: String): MEMBER =
        KPropertyPath<T, R>(
            this as KProperty1<T, Collection<R>?>,
            customProperty(this as KPropertyPath<*, T>, additionalPath)
        ) as MEMBER

    /**
     * [The positional array operator (projection or update) $](https://docs.mongodb.com/manual/reference/operator/update/positional/)
     */
    val posOp: MEMBER get() = memberWithAdditionalPath("\$")

    /**
     * [The all positional operator $[]](https://docs.mongodb.com/manual/reference/operator/update/positional-all/)
     */
    val allPosOp: MEMBER get() = memberWithAdditionalPath("\$[]")

    /**
     * [The filtered positional operator $[<identifier>]](https://docs.mongodb.com/manual/reference/operator/update/positional-filtered/)
     */
    fun filteredPosOp(identifier: String): MEMBER = memberWithAdditionalPath("\$[$identifier]")

    /**
     * In order to write array indexed expressions (like `accesses.0.timestamp`)
     */
    fun pos(position: Int): MEMBER = memberWithAdditionalPath(position.toString())
}

/**
 * A property path for a collection property.
 */
class KCollectionSimplePropertyPath<T, R>(
    previous: KPropertyPath<T, *>?,
    property: KProperty1<*, Iterable<R>?>
) : KCollectionPropertyPath<T, R, KPropertyPath<T, R?>>(previous, property)

/**
 * Base class for map property path.
 */
open class KMapPropertyPath<T, K, R, MEMBER : KPropertyPath<T, R?>>(
    previous: KPropertyPath<T, *>?,
    property: KProperty1<*, Map<out K, R>?>
) : KPropertyPath<T, Map<out K?, R>?>(previous, property) {

    /**
     * To be overridden to returns the right type.
     */
    @Suppress("UNCHECKED_CAST")
    open fun memberWithAdditionalPath(additionalPath: String): MEMBER =
        KPropertyPath<T, R>(
            this as KProperty1<T, Collection<R>?>,
            customProperty(this as KPropertyPath<*, T>, additionalPath)
        ) as MEMBER

    /**
     * Key projection of map.
     * Sample: `p.keyProjection(Locale.ENGLISH) / Gift::amount`
     */
    fun keyProjection(key: K): MEMBER = memberWithAdditionalPath(key.toString())
}

/**
 * A property path for a map property.
 */
class KMapSimplePropertyPath<T, K, R>(
    previous: KPropertyPath<T, *>?,
    property: KProperty1<*, Map<out K, R>?>
) : KMapPropertyPath<T, K, R, KPropertyPath<T, R?>>(previous, property)
