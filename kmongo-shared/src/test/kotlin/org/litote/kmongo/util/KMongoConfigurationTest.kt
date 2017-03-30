/*
 * Copyright (C) 2016 Litote
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

import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class KMongoConfigurationTest {

    class _Class_With_Underscore
    class CLASS_WithTWO_OrMoreAdjacentUpperCaseChars

    @Test
    fun testCamelCaseCollectionNameBuilder() {
        KMongoConfiguration.useCamelCaseCollectionNameBuilder()
        assertEquals("kmongoConfigurationTest", KMongoConfiguration.defaultCollectionNameBuilder.invoke(this::class))
        assertEquals("_Class_With_Underscore", KMongoConfiguration.defaultCollectionNameBuilder.invoke(_Class_With_Underscore::class))
        assertEquals("class_WithTwo_OrMoreAdjacentUpperCaseChars", KMongoConfiguration.defaultCollectionNameBuilder.invoke(CLASS_WithTWO_OrMoreAdjacentUpperCaseChars::class))
    }

    @Test
    fun testLowerCaseCollectionNameBuilder() {
        KMongoConfiguration.useLowerCaseCollectionNameBuilder()
        assertEquals("kmongoconfigurationtest", KMongoConfiguration.defaultCollectionNameBuilder.invoke(this::class))
        assertEquals("_class_with_underscore", KMongoConfiguration.defaultCollectionNameBuilder.invoke(_Class_With_Underscore::class))
        assertEquals("class_withtwo_ormoreadjacentuppercasechars", KMongoConfiguration.defaultCollectionNameBuilder.invoke(CLASS_WithTWO_OrMoreAdjacentUpperCaseChars::class))
    }

    @Test
    fun testSnakeCaseCollectionNameBuilder() {
        KMongoConfiguration.useSnakeCaseCollectionNameBuilder()
        assertEquals("kmongo_configuration_test", KMongoConfiguration.defaultCollectionNameBuilder.invoke(this::class))
        assertEquals("_class_with_underscore", KMongoConfiguration.defaultCollectionNameBuilder.invoke(_Class_With_Underscore::class))
        assertEquals("class_with_two_or_more_adjacent_upper_case_chars", KMongoConfiguration.defaultCollectionNameBuilder.invoke(CLASS_WithTWO_OrMoreAdjacentUpperCaseChars::class))
    }

    @Test
    fun testCamelCaseCollectionNameBuilderWithCustomFilter() {
        KMongoConfiguration.useCamelCaseCollectionNameBuilder { it.simpleName!!.replace("Test", "") }
        assertEquals("kmongoConfiguration", KMongoConfiguration.defaultCollectionNameBuilder.invoke(this::class))
    }

}