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

package org.litote.kmongo.property

import org.litote.kmongo.path
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMembers

/**
 * Find a property for class [R] of name [name].
 * Useful for private properties.
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified R, T> findProperty(name: String): KProperty1<R, T?> =
    R::class.declaredMembers.first { it.name == name } as KProperty1<R, T?>


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
                if (property is KPropertyPath<*, *>)
                    property as KPropertyPath<T, *>?
                else
                    KPropertyPath<T, Any?>(
                        null as (KPropertyPath<T, *>?),
                        previous
                    ),
                property
            )

    internal val path: String get() =
        "${previous?.path ?: ""}${if (previous == null) "" else "."}${property.path()}"

    override val annotations: List<Annotation> get() = property.annotations
    override val getter: KProperty1.Getter<T, R> get() = error("getter on KPropertyPath is not implemented")
    override val isAbstract: Boolean get() = previous?.isAbstract ?: false || property.isAbstract
    override val isConst: Boolean get() = previous?.isConst ?: false && property.isConst
    override val isFinal: Boolean get() = previous?.isFinal ?: false && property.isFinal
    override val isLateinit: Boolean get() = previous?.isLateinit ?: false && property.isLateinit
    override val isOpen: Boolean get() = previous?.isOpen ?: false && property.isOpen
    override val name: String get() = path
    override val parameters: List<KParameter> get() = property.parameters
    override val returnType: KType get() = property.returnType
    override val typeParameters: List<KTypeParameter> get() = property.typeParameters
    override val visibility: KVisibility? get() = property.visibility

    override fun invoke(p1: T): R = error("invoke on KPropertyPath is not implemented")

    override fun call(vararg args: Any?): R = error("call on KPropertyPath is not implemented")

    override fun callBy(args: Map<KParameter, Any?>): R = error("callBy on KPropertyPath is not implemented")

    override fun get(receiver: T): R = error("get on KPropertyPath is not implemented")

    override fun getDelegate(receiver: T): Any? = error("getDelegate on KPropertyPath is not implemented")

}