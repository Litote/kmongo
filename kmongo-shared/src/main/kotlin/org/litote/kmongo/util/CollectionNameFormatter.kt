/*
 * Copyright (C) 2016/2020 Litote
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

package org.litote.kmongo.util

import kotlin.reflect.KClass

/**
 * To configure the default collection name strategy - default is camel case.
 * To be used before KMongo initialization.
 */
object CollectionNameFormatter {

    /**
     * To change the default collection name strategy - default is camel case.
     */
    lateinit var defaultCollectionNameBuilder: (KClass<*>) -> String

    init {
        useCamelCaseCollectionNameBuilder()
    }

    /**
     * Use Camel Case default collection name builder.
     *
     * @param fromClass optional custom [KClass] -> [String] transformer (default is [KClass.simpleName])
     */
    fun useCamelCaseCollectionNameBuilder(fromClass: (KClass<*>) -> String = { it.simpleName!! }) {
        defaultCollectionNameBuilder = {
            fromClass
                    .invoke(it)
                    .toCharArray()
                    .run {
                        foldIndexed(StringBuilder()) { i, s, c ->
                            s.append(
                                    if (c.isUpperCase() && (i == 0 || this[i - 1].isUpperCase())) {
                                        c.toLowerCase()
                                    } else {
                                        c
                                    })
                        }.toString()
                    }
        }
    }

    /**
     * Use Snake Case default collection name builder.
     *
     * @param fromClass optional custom [KClass] -> [String] transformer (default is [KClass.simpleName])
     */
    fun useSnakeCaseCollectionNameBuilder(fromClass: (KClass<*>) -> String = { it.simpleName!! }) {
        defaultCollectionNameBuilder = {
            fromClass
                    .invoke(it)
                    .toCharArray()
                    .run {
                        foldIndexed(StringBuilder()) { i, s, c ->
                            if (c.isUpperCase()) {
                                if (i != 0 && this[i - 1].isLowerCase()) {
                                    s.append('_')
                                }
                                s.append(c.toLowerCase())
                            } else {
                                s.append(c)
                            }
                        }.toString()
                    }
        }
    }

    /**
     * Use Lower Case default collection name builder.
     *
     * @param fromClass optional custom [KClass] -> [String] transformer (default is [KClass.simpleName])
     */
    fun useLowerCaseCollectionNameBuilder(fromClass: (KClass<*>) -> String = { it.simpleName!! }) {
        defaultCollectionNameBuilder = { fromClass.invoke(it).toLowerCase() }
    }
}