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

import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonDiscriminator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import org.junit.Test
import org.litote.kmongo.PojoAnnotationsTest.WithAnnotations
import kotlin.test.assertEquals

/**
 *
 */
class PojoAnnotationsTest : AllCategoriesKMongoBaseTest<WithAnnotations>() {

    @BsonDiscriminator("an")
    data class WithAnnotations(
        @BsonId val withAnnotationsId: String,
        @BsonProperty("otherName") val prop: String,
        @BsonProperty(useDiscriminator = true) val other: OtherClass,
        @BsonIgnore val ignore: String?
    )

    data class OtherClass(val ok: Boolean = true)

    data class WithBsonCreatorConstructor(
        @BsonProperty("n") val name: String,
        @BsonProperty("o") val optional: String? = null,
        @BsonId val id: Id<WithBsonCreatorConstructor> = newId()
    ) {

        @BsonCreator
        constructor(name: String, id: Id<WithBsonCreatorConstructor>) : this(name, null, id)
    }

    data class WithBsonCreatorConstructorAndBsonProperty(
        val name: String,
        @BsonProperty("o") val optional: String? = null,
        @BsonId val id: Id<WithBsonCreatorConstructorAndBsonProperty> = newId()
    ) {

        @BsonCreator
        constructor(@BsonProperty("n") name: String, id: Id<WithBsonCreatorConstructorAndBsonProperty>)
                : this(name, null, id)
    }

    data class WithBsonCompanionInstantiatorMethod(
        @BsonProperty("n") val name: String,
        @BsonProperty("o") val optional: String? = null,
        @BsonId val id: Id<WithBsonCompanionInstantiatorMethod> = newId()
    ) {

        companion object {
            @BsonCreator
            fun create(name: String, id: Id<WithBsonCompanionInstantiatorMethod>) =
                WithBsonCompanionInstantiatorMethod(name, null, id)
        }
    }

    data class WithBsonCompanionInstantiatorMethodWithBsonProperty(
        val name: String,
        @BsonProperty("o") val optional: String? = null,
        @BsonId val id: Id<WithBsonCompanionInstantiatorMethodWithBsonProperty> = newId()
    ) {

        companion object {
            @BsonCreator
            fun create(@BsonProperty("n") name: String, id: Id<WithBsonCompanionInstantiatorMethodWithBsonProperty>) =
                WithBsonCompanionInstantiatorMethodWithBsonProperty(name, null, id)
        }
    }

    @Test
    fun insertAndLoadShouldRespectAnnotationsBehaviour() {
        val a = WithAnnotations("abc", "oldName", OtherClass(), "toignore")
        col.insertOne(a)
        assertEquals(
            Document.parse("{\"_id\":\"abc\", \"_t\":\"an\", \"other\":{\"_t\":\"org.litote.kmongo.PojoAnnotationsTest\$OtherClass\", \"ok\":true}, \"otherName\":\"oldName\"}"),
            col.withDocumentClass<Document>().find().first()
        )
        assertEquals(a.copy(ignore = null), col.find().first())
    }

    @Test
    fun insertAndLoadWorksWithBsonCreator() {
        val a = WithBsonCreatorConstructor("Joe", "op")
        val c = col.withDocumentClass<WithBsonCreatorConstructor>()
        c.insertOne(a)
        val r = c.withDocumentClass<Document>().find().first()
        assertEquals(
            Document.parse("{\"_id\":${a.id.json}, \"n\":\"Joe\", \"o\":\"op\"}"),
            r
        )
        assertEquals(a.copy(optional = null), c.find().first())
    }

    @Test
    fun insertAndLoadWorksWithBsonCreatorWithPropertyOnConstructor() {
        val a = WithBsonCreatorConstructorAndBsonProperty("Joe", "op")
        val c = col.withDocumentClass<WithBsonCreatorConstructorAndBsonProperty>()
        c.insertOne(a)
        val r = c.withDocumentClass<Document>().find().first()
        assertEquals(
            Document.parse("{\"_id\":${a.id.json}, \"n\":\"Joe\", \"o\":\"op\"}"),
            r
        )
        assertEquals(a.copy(optional = null), c.find().first())
    }

    @Test
    fun insertAndLoadWorksWithBsonCreatorOnCompanionFunction() {
        val a = WithBsonCompanionInstantiatorMethod("Joe", "op")
        val c = col.withDocumentClass<WithBsonCompanionInstantiatorMethod>()
        c.insertOne(a)
        val r = c.withDocumentClass<Document>().find().first()
        assertEquals(
            Document.parse("{\"_id\":${a.id.json}, \"n\":\"Joe\", \"o\":\"op\"}"),
            r
        )
        assertEquals(a.copy(optional = null), c.find().first())
    }

    @Test
    fun insertAndLoadWorksWithBsonCreatorOnCompanionFunctionWithBsonProperty() {
        val a = WithBsonCompanionInstantiatorMethodWithBsonProperty("Joe", "op")
        val c = col.withDocumentClass<WithBsonCompanionInstantiatorMethodWithBsonProperty>()
        c.insertOne(a)
        val r = c.withDocumentClass<Document>().find().first()
        assertEquals(
            Document.parse("{\"_id\":${a.id.json}, \"n\":\"Joe\", \"o\":\"op\"}"),
            r
        )
        assertEquals(a.copy(optional = null), c.find().first())
    }


}