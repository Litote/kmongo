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

import com.mongodb.client.model.Filters.lt
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.MongoOperator.avg
import org.litote.kmongo.MongoOperator.group
import org.litote.kmongo.MongoOperator.lt
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.max
import org.litote.kmongo.MongoOperator.regex
import org.litote.kmongo.MongoOperator.sample
import org.litote.kmongo.UsageTest.Jedi
import java.time.LocalDate
import java.time.Month.MAY
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Samples used in doc
 */
class UsageTest : KMongoBaseTest<Jedi>() {

    @Serializable
    data class Jedi(val name: String, val age: Int, val firstAppearance: StarWarsFilm)
    @Serializable
    data class StarWarsFilm(val name: String, @Contextual val date: LocalDate)

    @Serializable
    data class LightSaber1(val _id: String?)
    @Serializable
    data class LightSaber2(@Contextual val _id: org.bson.types.ObjectId?)
    @Serializable
    data class LightSaber3(val _id: String)
    @Serializable
    data class LightSaber4(@Contextual val _id: org.bson.types.ObjectId)
    @Serializable
    data class LightSaber5(var _id: String?)
    @Serializable
    data class LightSaber6(@Contextual var _id: org.bson.types.ObjectId?)

    @Serializable
    class TFighter(val version: String, val pilot: Pilot?)
    @Serializable
    class Pilot()

    @Before
    fun setup() {
        col.insertOne(Jedi("Luke Skywalker", 19, StarWarsFilm("A New Hope", LocalDate.of(1977, MAY, 25))))
        col.insertOne("{name:'Yoda',age:896,firstAppearance:{name:'The Empire Strikes Back',date:new Date('Sat May 17 1980 00:00:00 CEST')}}")
    }

    @Category(JacksonMappingCategory::class, NativeMappingCategory::class, SerializationMappingCategory::class)
    @Test
    fun firstSample() {
        val yoda = col.findOne("{name: {$regex: 'Yo.*'}}")!!

        val luke = col.aggregate<Jedi>(
            """[ {$match:{age:{$lt : ${yoda.age}}}},
                                            {$sample:{size:1}}
                                          ]"""
        ).first()

        val luke2 = col.aggregate<Jedi>(
            match(lt("age", yoda.age)),
            sample(1)
        )
            .first()

        assertEquals("Luke Skywalker", luke?.name)
        assertEquals("Yoda", yoda.name)
        assertEquals(luke, luke2)
    }

    @Category(JacksonMappingCategory::class)
    @Test
    fun aggregateSample() {
        val (averageAge, maxAge) = col.aggregate<Pair<Int, Int>>("{$group:{_id:null,first:{$avg:'\$age'},second:{$max:'\$age'}}}").first()
        assertEquals((896 + 19) / 2, averageAge)
        assertEquals(896, maxAge)
    }

    @Category(JacksonMappingCategory::class, NativeMappingCategory::class, SerializationMappingCategory::class)
    @Test
    fun testInsertId() {
        val lightSaber1 = LightSaber1(null)
        database.getCollection<LightSaber1>().insertOne(lightSaber1)
        assertNotNull(lightSaber1._id)
        val lightSaber2 = LightSaber2(null)
        database.getCollection<LightSaber2>().insertOne(lightSaber2)
        assertNotNull(lightSaber2._id)
        database.getCollection<LightSaber3>().insertOne(LightSaber3("coucou"))
        val id = ObjectId()
        database.getCollection<LightSaber4>().insertOne(LightSaber4(id))
        val lightSaber5 = LightSaber5(null)
        database.getCollection<LightSaber5>().insertOne(lightSaber5)
        assertNotNull(lightSaber5._id)
        val lightSaber6 = LightSaber6(null)
        database.getCollection<LightSaber6>().insertOne(lightSaber6)
        assertNotNull(lightSaber6._id)

        assertNotNull(database.getCollection<LightSaber1>().findOne()!!._id)
        assertNotNull(database.getCollection<LightSaber2>().findOne()!!._id)
        assertEquals("coucou", database.getCollection<LightSaber3>().findOne()!!._id)
        assertEquals(id, database.getCollection<LightSaber4>().findOne()!!._id)
        assertNotNull(database.getCollection<LightSaber5>().findOne()!!._id)
        assertNotNull(database.getCollection<LightSaber6>().findOne()!!._id)
    }

    @Category(JacksonMappingCategory::class)
    @Test
    fun testInsertNullFieldForJacksonMapping() {
        database.getCollection<TFighter>().insertOne(TFighter("v1", null))
        val doc = database.getCollection("tfighter").findOne()

        assertEquals("v1", doc!!.get("version"))
        assertTrue(doc.containsKey("pilot"))
    }

    @Category(NativeMappingCategory::class)
    @Test
    fun testInsertNullFieldForNativeMapping() {
        database.getCollection<TFighter>().insertOne(TFighter("v1", null))
        val doc = database.getCollection("tfighter").findOne()

        assertEquals("v1", doc!!.get("version"))
        assertFalse(doc.containsKey("pilot"))
    }
}

