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

package org.litote.kmongo

import com.mongodb.client.MongoCollection
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.IdTest.Article
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.MongoId
import org.litote.kmongo.id.ObjectIdGenerator
import org.litote.kmongo.id.ObjectIdToStringGenerator
import org.litote.kmongo.id.StringId
import org.litote.kmongo.id.WrappedObjectId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 *
 */
class IdTest : AllCategoriesKMongoBaseTest<Article>() {

    @Serializable
    data class Article(
        @Contextual
        val _id: Id<Article> = newId(),
        val title: String,
        @Contextual
        val shopId: Id<Shop>? = null,
        val articleIds: MutableSet<@Contextual Id<Article>> = mutableSetOf()
    ) {

        constructor(title: String, shopId: Id<Shop>? = null) : this(newId(), title, shopId)

        init {
            articleIds.add(_id)
        }

    }

    @Serializable
    data class Article2(
        @Contextual
        val _id: Id<Article2> = newId(),
        val title: String,
        @Contextual
        val shopId: Id<Shop>? = null,
        val articleIds: MutableSet<@Contextual Id<Article2>> = mutableSetOf(),
        val mapWithIds: MutableMap<@Contextual Id<Article2>, Boolean> = mutableMapOf()
    ) {

        constructor(title: String, shopId: Id<Shop>? = null) : this(newId(), title, shopId)

        init {
            articleIds.add(_id)
            mapWithIds[_id] = true
        }
    }

    @Serializable
    data class Shop(
        val name: String,
        @Contextual
        @BsonId
        @SerialName("_id")
        @MongoId
        val id: Id<Shop> = newId()
    )

    @Serializable
    data class Article3(
        val _id: Long,
        val title: String
    )

    @Serializable
    data class ArticleWithNullId(
        val _id: String?,
        val title: String = "test"
    )

    @Serializable
    data class ArticleWithNullableGenericId(
        @Contextual
        val _id: Id<ArticleWithNullableGenericId>?,
        val title: String = "test"
    )

    @Serializable
    data class ArticleWithNullableStringId(
        @Contextual
        val _id: StringId<ArticleWithNullableStringId>?,
        val title: String = "test"
    )

    @Serializable
    data class ArticleWithNullableWrappedObjectId(
        @Contextual
        val _id: WrappedObjectId<ArticleWithNullableWrappedObjectId>?,
        val title: String = "test"
    )


    lateinit var shopCol: MongoCollection<Shop>
    lateinit var article2Col: MongoCollection<Article2>
    lateinit var article3Col: MongoCollection<Article3>
    lateinit var articleNullCol: MongoCollection<ArticleWithNullId>

    @Before
    fun setup() {
        shopCol = getCollection()
        article2Col = getCollection()
        article3Col = getCollection()
        articleNullCol = getCollection()
    }

    @After
    fun tearDown() {
        dropCollection<Shop>()
        dropCollection<Article2>()
        dropCollection<Article3>()
        dropCollection<ArticleWithNullId>()
    }

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

    @Test
    fun longIdTest() {
        val a = Article3(Long.MAX_VALUE, "a")
        article3Col.insertOne(a)
        assertEquals(a, article3Col.findOne("{_id:{${MongoOperator.type}:'long'}}"))
    }

    @Test
    fun `query id with dsl is ok`() {
        stringGenerator()
        val shop = Shop("Shop")
        val article = Article2("ok", shop.id)
        article2Col.save(article)
        shopCol.save(shop)
        assertEquals(article, article2Col.findOne(article::_id eq article._id))
        assertEquals(shop, shopCol.findOne(shop::id eq shop.id))

        objectIdGenerator()
        val objectIdArticle = Article2("ok", shop.id)
        article2Col.save(objectIdArticle)
        assertEquals(objectIdArticle, article2Col.findOne(objectIdArticle::_id eq objectIdArticle._id))
        assertEquals(shop, shopCol.findOne(shop::id eq shop.id))
    }

    @Test
    fun `class with null id is generated on client side`() {
        val a = ArticleWithNullId(null)
        articleNullCol.insertOne(a)
        assertNotNull(a._id)
        assertEquals(a, articleNullCol.findOne())
    }

    @Test
    fun `class with null generated id is generated on client side`() {
        val a = ArticleWithNullableGenericId(null)
        val col = articleNullCol.withDocumentClass<ArticleWithNullableGenericId>()
        col.insertOne(a)
        assertNotNull(a._id)
        assertEquals(a, col.findOne())
    }

    @Test
    fun `class with null StringId is generated on client side`() {
        val a = ArticleWithNullableStringId(null)
        val col = articleNullCol.withDocumentClass<ArticleWithNullableStringId>()
        col.insertOne(a)
        assertNotNull(a._id)
        assertEquals(a, col.findOne())
    }

    @Test
    fun `class with null WrappedObjectId is generated on client side`() {
        val a = ArticleWithNullableWrappedObjectId(null)
        val col = articleNullCol.withDocumentClass<ArticleWithNullableWrappedObjectId>()
        col.insertOne(a)
        assertNotNull(a._id)
        assertEquals(a, col.findOne())
    }
}