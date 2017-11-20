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

import org.bson.Document
import org.bson.types.Decimal128
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.BigDecimalTest.Article
import org.litote.kmongo.MongoOperator.numberDecimal
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class BigDecimalTest : KMongoBaseTest<Article>() {

    data class Article(val title: String, val big: BigDecimal) {
    }

    override fun getDefaultCollectionClass(): KClass<Article> = Article::class

    @Category(NativeMappingCategory::class)
    @Test
    fun bigDecimalShouldBePersistedAsDecimal128() {
        val a = Article("title", BigDecimal("1.0"))
        col.save(a)
        val doc = col.withDocumentClass<Document>().findOne()!!
        assertTrue(doc["big"] is Decimal128)
        assertEquals(a, col.findOne())
    }

    @Category(NativeMappingCategory::class)
    @Test
    fun bigDecimalShouldBeTransformedInJsonNumberDecimal() {
        val a = Article("title", BigDecimal("1.0"))
        assertEquals("{\"big\":{\"$numberDecimal\":\"1.0\"},\"title\":\"title\"}", a.json.replace(" ", ""))
    }
}