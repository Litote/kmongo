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

package org.litote.kmongo

import org.bson.BsonDocument
import org.bson.types.ObjectId
import org.litote.kmongo.MongoOperator.lookup
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class AggregatesTest {

    class AggregateData(val s: String, val qty: Int, val date: LocalDate, val objectId: ObjectId)

    @Test
    fun testGte() {
        val bson = project(MongoOperator.gte from listOf(AggregateData::qty.projection, 250))
        assertEquals(
            BsonDocument.parse("""{"${MongoOperator.project}": {"${MongoOperator.gte}": ["${'$'}qty", 250]}}"""),
            bson.document
        )
    }

    @Test
    fun testLookup() {
        val bson = lookup(from = "f", resultProperty = AggregateData::s)
        assertEquals(
            BsonDocument.parse("""{"$lookup":{from:"f", pipeline:[], as:"s"}}"""),
            bson.document
        )
    }

    @Test
    fun testVariable() {
        assertEquals("\$\$s", AggregateData::s.variable)
        assertEquals("\$\$s", "s".variable)
    }

    @Test
    fun testYearWithTemporalAndObjectId() {
        assertEquals(BsonDocument.parse("{\"\$year\":\"\$date\"}"), year(AggregateData::date).document)
        assertEquals(BsonDocument.parse("{\"\$year\":\"\$objectId\"}"), year(AggregateData::objectId).document)
    }
}