/*
 * Copyright (C) 2016 Litote
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

import org.junit.Before
import org.junit.Test
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
import kotlin.reflect.KClass
import kotlin.test.assertEquals

/**
 * Samples used in doc
 */
class UsageTest : KMongoBaseTest<Jedi>() {

    data class Jedi(val name: String, val age: Int, val firstAppearance: StarWarsFilm)
    data class StarWarsFilm(val name: String, val date: LocalDate)

    override fun getDefaultCollectionClass(): KClass<Jedi> {
        return Jedi::class
    }

    @Before
    fun setup() {
        col.insertOne(Jedi("Luke Skywalker", 19, StarWarsFilm("A New Hope", LocalDate.of(1977, MAY, 25))))
        col.insertOne("{name:'Yoda',age:896,firstAppearance:{name:'The Empire Strikes Back',date:new Date('Sat May 17 1980 00:00:00 CEST')}}")
    }

    @Test
    fun firstSample() {
        val yoda = col.findOne("{name: {$regex: 'Yo.*'}}")!!

        val luke = col.aggregate<Jedi>("""[ {$match:{age:{$lt : ${yoda.age}}}},
                                            {$sample:{size:1}}
                                          ]""").first()

        assertEquals("Luke Skywalker", luke.name)
        assertEquals("Yoda", yoda.name)
    }

    @Test
    fun aggregateSample() {
        val (averageAge, maxAge) = col.aggregate<Pair<Int, Int>>("{$group:{_id:null,first:{$avg:'\$age'},second:{$max:'\$age'}}}").first()
        assertEquals((896 + 19) / 2, averageAge)
        assertEquals(896, maxAge)
    }
}