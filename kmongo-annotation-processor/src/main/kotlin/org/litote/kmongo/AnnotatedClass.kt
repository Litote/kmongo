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

import com.squareup.kotlinpoet.CodeBlock
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 *
 */
internal class AnnotatedClass(
    val element: TypeElement,
    val internal: Boolean,
    private val a: KMongoAnnotations
) : TypeElement by element {

    fun getPackage(): String =
        a.processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()

    fun properties(): List<VariableElement> =
        element.enclosedElements
            .filterIsInstance<VariableElement>()
            .filter { e -> e.modifiers.none { notSupportedModifiers.contains(it) } }

    fun propertyReference(
        property: Element,
        privateHandler: () -> CodeBlock,
        nonPrivateHandler: () -> CodeBlock
    ): CodeBlock {
        return if (element
                .enclosedElements
                .firstOrNull { it.simpleName.toString() == "get${property.simpleName.toString().capitalize()}" }
                ?.modifiers
                ?.contains(Modifier.PRIVATE) != false
        ) {
            privateHandler()
        } else {
            nonPrivateHandler()
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is AnnotatedClass) {
            element == other.element
        } else {
            element == other
        }
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }

    override fun toString(): String {
        return "Annotated(element=$element, internal=$internal)"
    }

}