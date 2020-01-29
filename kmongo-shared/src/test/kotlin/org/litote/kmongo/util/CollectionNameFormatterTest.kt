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

import org.junit.Test
import org.litote.kmongo.KMongoRootTest
import kotlin.test.assertEquals

/**
 *
 */
class CollectionNameFormatterTest : KMongoRootTest() {

    class _Class_With_Underscore
    class CLASS_WithTWO_OrMoreAdjacentUpperCaseChars

    @Test
    fun testCamelCaseCollectionNameBuilder() {
        CollectionNameFormatter.useCamelCaseCollectionNameBuilder()
        assertEquals("collectionNameFormatterTest", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(this::class))
        assertEquals("_Class_With_Underscore", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(_Class_With_Underscore::class))
        assertEquals("class_WithTwo_OrMoreAdjacentUpperCaseChars", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(CLASS_WithTWO_OrMoreAdjacentUpperCaseChars::class))
    }

    @Test
    fun testLowerCaseCollectionNameBuilder() {
        CollectionNameFormatter.useLowerCaseCollectionNameBuilder()
        assertEquals("collectionnameformattertest", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(this::class))
        assertEquals("_class_with_underscore", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(_Class_With_Underscore::class))
        assertEquals("class_withtwo_ormoreadjacentuppercasechars", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(CLASS_WithTWO_OrMoreAdjacentUpperCaseChars::class))
    }

    @Test
    fun testSnakeCaseCollectionNameBuilder() {
        CollectionNameFormatter.useSnakeCaseCollectionNameBuilder()
        assertEquals("collection_name_formatter_test", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(this::class))
        assertEquals("_class_with_underscore", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(_Class_With_Underscore::class))
        assertEquals("class_with_two_or_more_adjacent_upper_case_chars", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(CLASS_WithTWO_OrMoreAdjacentUpperCaseChars::class))
    }

    @Test
    fun testCamelCaseCollectionNameBuilderWithCustomFilter() {
        CollectionNameFormatter.useCamelCaseCollectionNameBuilder { it.simpleName!!.replace("Test", "") }
        assertEquals("collectionNameFormatter", CollectionNameFormatter.defaultCollectionNameBuilder.invoke(this::class))
    }

}