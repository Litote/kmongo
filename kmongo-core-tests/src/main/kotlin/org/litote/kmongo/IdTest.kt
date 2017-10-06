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

import com.mongodb.client.MongoCollection
import org.bson.codecs.pojo.annotations.BsonId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.IdTest.Article
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdGenerator
import org.litote.kmongo.id.ObjectIdToStringGenerator
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class IdTest : AllCategoriesKMongoBaseTest<Article>() {

    data class Article(
            val _id: Id<Article> = newId(),
            val title: String,
            val shopId: Id<Shop>? = null) {

        constructor(title: String, shopId: Id<Shop>? = null) : this(newId(), title, shopId)

    }

    data class Shop(val name: String, @BsonId val id: Id<Shop> = newId())

    lateinit var shopCol: MongoCollection<Shop>

    @Before
    fun setup() {
        shopCol = getCollection<Shop>()
    }

    @After
    fun tearDown() {
        dropCollection<Shop>()
    }

    override fun getDefaultCollectionClass(): KClass<Article> = Article::class

    private fun stringGenerator() {
        IdGenerator.defaultGenerator = ObjectIdToStringGenerator
    }

    private fun objectIdGenerator() {
        IdGenerator.defaultGenerator = ObjectIdGenerator
    }

    @Test
    fun extendedJsonShouldBeHandledWell() {
        stringGenerator()
        val article = Article("ok")
        assertTrue(
                article.json.replace(" ", "").contains("{\"_id\":\"${article._id}\"")
        )
        assertTrue(
                article.json.replace(" ", "").contains("\"title\":\"ok\"")
        )
        objectIdGenerator()
        val objectIdArticle = Article("ok")
        assertTrue(
                objectIdArticle.json.replace(" ", "").contains("{\"_id\":{\"$oid\":\"${objectIdArticle._id}\"}")
        )
        assertTrue(
                objectIdArticle.json.replace(" ", "").contains("\"title\":\"ok\"")
        )
    }

    @Test
    fun savingAndRetrievingObjectShouldBeOk() {
        stringGenerator()
        val shop = Shop("Shop")
        val article = Article("ok", shop.id)
        col.save(article)
        shopCol.save(shop)
        assertEquals(article, col.findOneById(article._id))
        assertEquals(shop, shopCol.findOneById(shop.id))

        objectIdGenerator()
        val objectIdArticle = Article("ok", shop.id)
        col.save(objectIdArticle)
        assertEquals(objectIdArticle, col.findOneById(objectIdArticle._id))
        assertEquals(shop, shopCol.findOneById(shop.id))
    }
}