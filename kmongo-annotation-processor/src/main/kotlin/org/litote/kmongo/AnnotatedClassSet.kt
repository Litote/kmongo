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

package org.litote.kmongo

import javax.lang.model.element.Element

/**
 *
 */
internal class AnnotatedClassSet(
    private val elements: MutableSet<AnnotatedClass> = mutableSetOf()
) {

    fun isNotEmpty(): Boolean = elements.isNotEmpty()

    fun forEach(action: (AnnotatedClass) -> Unit) {
        elements.toList().forEach(action)
    }

    fun contains(element: Element?): Boolean = elements.any { it.element == element }

    fun add(element: AnnotatedClass) {
        elements.add(element)
    }

    fun filter(predicate: (AnnotatedClass) -> Boolean): AnnotatedClassSet {
        return AnnotatedClassSet(elements.filter(predicate).toMutableSet())
    }

    fun toList(): List<AnnotatedClass> = elements.toList()

    override fun toString() : String = elements.toString()
}