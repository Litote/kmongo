/*
 * Copyright (C) 2016/2022 Litote
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

package org.litote.kmongo.issues

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOneById
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertEquals

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes(
    Type(value = ImplementationClass::class, name = "impl")
)
interface CollectionInterface

@JsonTypeName("impl")
data class ImplementationClass(val _id: String? = null) : CollectionInterface

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes(
    Type(value = ExtendClass::class, name = "impl")
)
abstract class AbstractClass

@JsonTypeName("impl")
class ExtendClass(val _id: String? = null) : AbstractClass() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ExtendClass

        if (_id != other._id) return false

        return true
    }

    override fun hashCode(): Int {
        return _id?.hashCode() ?: 0
    }
}

data class ClassWithAbstractMember(val _id: String? = null, val ext: AbstractClass = ExtendClass("c"))

data class ClassWithAbstractMemberMap(
    val _id: String? = null,
    val ext: Map<String, List<AbstractClass>> = mapOf(
        Pair(
            "c",
            listOf(ExtendClass("c"))
        )
    )
)

/**
 * [Id not generated for subclass](https://github.com/Litote/kmongo/issues/2)
 */
class Issue2IdNotGeneratedForSubclass : AllCategoriesKMongoBaseTest<CollectionInterface>() {

    @Test
    fun testSerializeAndDeserializeInterface() {
        val e = ImplementationClass()
        col.insertOne(e)
        val e2 = col.findOneById(e._id!!)
        assertEquals(e, e2)
    }

    @Test
    fun testSerializeAndDeserializeAbstractClass() {
        val e = ExtendClass()
        val col2 = col.withDocumentClass<AbstractClass>()
        col2.insertOne(e)
        val e2 = col2.findOneById(e._id!!)
        assertEquals(e, e2)
    }

    @Test
    fun testSerializeAndDeserializeClassWithAbstractMember() {
        val e = ClassWithAbstractMember()
        val col2 = col.withDocumentClass<ClassWithAbstractMember>()
        col2.insertOne(e)
        val e2 = col2.findOneById(e._id!!)
        assertEquals(e, e2)
    }

    @Test
    fun testSerializeAndDeserializeClassWithAbstractMemberMp() {
        val e = ClassWithAbstractMemberMap(null, mapOf(Pair("e", listOf(ExtendClass("e")))))
        val col2 = col.withDocumentClass<ClassWithAbstractMemberMap>()
        col2.insertOne(e)
        val e2 = col2.findOneById(e._id!!)
        assertEquals(e, e2)
    }
}