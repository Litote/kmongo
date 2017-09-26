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
import org.bson.codecs.pojo.annotations.BsonDiscriminator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import org.junit.Test
import org.litote.kmongo.PojoAnnotationsTest.WithAnnotations
import kotlin.reflect.KClass
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
            @BsonIgnore val ignore: String?)

    data class OtherClass(val ok: Boolean = true)

    override fun getDefaultCollectionClass(): KClass<WithAnnotations> {
        return WithAnnotations::class
    }

    @Test
    fun insertAndLoadShouldRespectAnnotationsBehaviour() {
        val a = WithAnnotations("abc", "oldName", OtherClass(), "toignore")
        col.insertOne(a)
        assertEquals(
                Document.parse("{\"_id\":\"abc\", \"_t\":\"an\", \"other\":{\"_t\":\"org.litote.kmongo.PojoAnnotationsTest\$OtherClass\", \"ok\":true}, \"otherName\":\"oldName\"}"),
                col.withDocumentClass<Document>().find().first())
        assertEquals(a.copy(ignore = null), col.find().first())
    }
}