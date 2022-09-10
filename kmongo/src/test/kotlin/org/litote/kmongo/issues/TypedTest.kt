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

package org.litote.kmongo.issues

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.aggregate
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.from
import org.litote.kmongo.match
import org.litote.kmongo.project
import org.litote.kmongo.projection
import org.litote.kmongo.save
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals

data class MyClass(
    val items: List<Item>,
    val timestamp: Long = Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond(),
    @BsonId
    val id: String = ObjectId().toString()
)

data class Item(
    val name: String,
    val price: Double,
    val qty: Double
)


data class Result(val sumPrice: Double)

/**
 *
 */
class TypedTest :
    AllCategoriesKMongoBaseTest<MyClass>() {

    @Test
    fun `serialization and deserialization is ok`() {
        val d = MyClass(listOf(Item("a", 12.9, 1.0)), id = "id")
        col.save(d)

        val r: Double = col.aggregate<Result>(
            match(MyClass::id eq "id"),
            project(Result::sumPrice to MongoOperator.sum.from((MyClass::items / Item::price).projection))
        ).first()?.sumPrice ?: 0.0
        assertEquals(12.9, r)
    }
}