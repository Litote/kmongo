/*
 * Copyright (C) 2016/2021 Litote
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

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.litote.kmongo.KMongoRootTest
import org.litote.kmongo.issues.Foo
import kotlin.reflect.full.memberProperties

/**

 */
class MongoIdUtilTest : KMongoRootTest() {

    class WithMongoId(@BsonId val key: ObjectId? = null)

    @Test
    fun `idProperty extension returns the id property`() {
        assertEquals(
            MongoIdUtil.findIdProperty(WithMongoId::class),
            WithMongoId::class.idProperty
        )
    }

    @Test
    fun `idValue extension returns the id value`() {
        val id = ObjectId()
        assertEquals(id, WithMongoId(id).idValue)
    }

    @Test
    fun findIdPropertyShouldFindMongoIdAnnotatedField() {
        val p = MongoIdUtil.findIdProperty(WithMongoId::class)
        assertEquals("key", p!!.name)
    }

    class WithBsonId(@BsonId val key: ObjectId? = null)

    @Test
    fun findIdPropertyShouldFindBsonIdAnnotatedField() {
        val p = MongoIdUtil.findIdProperty(WithBsonId::class)
        assertEquals("key", p!!.name)
    }

    class Obj(var _id: ObjectId = ObjectId())

    @Test
    fun extractId() {
        val id = ObjectId()
        assertEquals(id, KMongoUtil.extractId(Obj(id), Obj::class))
    }

    @Test
    fun extractIdIfIdNotEnabled() {
        System.setProperty("kmongo.id.enabled", "false")
        val id = ObjectId()
        assertEquals(id, KMongoUtil.extractId(Obj(id), Obj::class))
    }

    @Test
    fun `id property is detected even for java classes`() {
        assertEquals(Foo::class.memberProperties.first { it.name == "id" }, Foo::class.idProperty)
    }

    data class TestObjWithId(val id : String?)

    @Test
    fun extractIdWithoutUnderscore() {
        System.setProperty("kmongo.id.enabled", "true")
        assertEquals("id", KMongoUtil.extractId(TestObjWithId("id"), TestObjWithId::class))
        System.setProperty("kmongo.id.enabled", "false")
    }


}