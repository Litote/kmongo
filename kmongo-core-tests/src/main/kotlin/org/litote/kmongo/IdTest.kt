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
import org.junit.experimental.categories.Category
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
class IdTest : KMongoBaseTest<Article>() {

    data class Article(
            val _id: Id<Article> = newId(),
            val title: String,
            val shopId: Id<Shop>? = null,
            val articleIds:Set<Id<Article>> = setOf(_id)) {

        constructor(title: String, shopId: Id<Shop>? = null) : this(newId(), title, shopId)

    }

    data class Article2(
            val _id: Id<Article2> = newId(),
            val title: String,
            val shopId: Id<Shop>? = null,
            val articleIds:Set<Id<Article2>> = setOf(_id),
            val mapWithIds:Map<Id<Article2>, Boolean> = mapOf(_id to true)) {

        constructor(title: String, shopId: Id<Shop>? = null) : this(newId(), title, shopId)

    }

    data class Shop(
            val name: String,
            @BsonId val id: Id<Shop> = newId()
            )

    lateinit var shopCol: MongoCollection<Shop>
    lateinit var article2Col: MongoCollection<Article2>

    @Before
    fun setup() {
        shopCol = getCollection<Shop>()
        article2Col = getCollection<Article2>()
    }

    @After
    fun tearDown() {
        dropCollection<Shop>()
        dropCollection<Article2>()
    }

    override fun getDefaultCollectionClass(): KClass<Article> = Article::class

    private fun stringGenerator() {
        IdGenerator.defaultGenerator = ObjectIdToStringGenerator
    }

    private fun objectIdGenerator() {
        IdGenerator.defaultGenerator = ObjectIdGenerator
    }

    @Category(JacksonMappingCategory::class, NativeMappingCategory::class)
    @Test
    fun extendedJsonShouldBeHandledWell() {
        stringGenerator()
        val article = Article("ok")
        val json = article.json.replace(" ", "")
        assertTrue(
                json.contains("{\"_id\":\"${article._id}\"")
        )
        assertTrue(
                json.contains("\"title\":\"ok\"")
        )
        assertTrue(
                json.contains("\"articleIds\":[\"${article._id}\"]")
        )
        
        objectIdGenerator()
        val objectIdArticle = Article("ok")
        val jsonWithObjectId = objectIdArticle.json.replace(" ", "")
        assertTrue(
                jsonWithObjectId.contains("{\"_id\":{\"$oid\":\"${objectIdArticle._id}\"}")
        )
        assertTrue(
                jsonWithObjectId.contains("\"title\":\"ok\"")
        )
        assertTrue(
                jsonWithObjectId.contains("\"articleIds\":[{\"$oid\":\"${objectIdArticle._id}\"}]")
        )
    }

    @Category(JacksonMappingCategory::class, NativeMappingCategory::class)
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

    @Category(JacksonMappingCategory::class)
    @Test
    fun extendedJsonShouldBeHandledWellWithObjectContainingMapWithIds() {
        stringGenerator()
        val article = Article2("ok")
        val json = article.json.replace(" ", "")
        assertTrue(
                json.contains("{\"_id\":\"${article._id}\"")
        )
        assertTrue(
                json.contains("\"title\":\"ok\"")
        )
        assertTrue(
                json.contains("\"articleIds\":[\"${article._id}\"]")
        )
        assertTrue(
                json.contains("\"mapWithIds\":{\"${article._id}\":true}")
        )

        objectIdGenerator()
        val objectIdArticle = Article2("ok")
        val jsonWithObjectId = objectIdArticle.json.replace(" ", "")
        assertTrue(
                jsonWithObjectId.contains("{\"_id\":{\"$oid\":\"${objectIdArticle._id}\"}")
        )
        assertTrue(
                jsonWithObjectId.contains("\"title\":\"ok\"")
        )
        assertTrue(
                jsonWithObjectId.contains("\"articleIds\":[{\"$oid\":\"${objectIdArticle._id}\"}]")
        )
        assertTrue(
                jsonWithObjectId.contains("\"mapWithIds\":{\"${objectIdArticle._id}\":true}")
        )
    }

    @Category(JacksonMappingCategory::class)
    @Test
    fun savingAndRetrievingObjectShouldBeOkWithObjectContainingMapWithIds() {
        stringGenerator()
        val shop = Shop("Shop")
        val article = Article2("ok", shop.id)
        article2Col.save(article)
        shopCol.save(shop)
        assertEquals(article, article2Col.findOneById(article._id))
        assertEquals(shop, shopCol.findOneById(shop.id))

        objectIdGenerator()
        val objectIdArticle = Article2("ok", shop.id)
        article2Col.save(objectIdArticle)
        assertEquals(objectIdArticle, article2Col.findOneById(objectIdArticle._id))
        assertEquals(shop, shopCol.findOneById(shop.id))
    }
}