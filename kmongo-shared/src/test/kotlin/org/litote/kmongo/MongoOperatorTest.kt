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
package org.litote.kmongo

import org.junit.Test
import org.litote.kmongo.MongoOperator.`in`
import org.litote.kmongo.MongoOperator.group
import org.litote.kmongo.MongoOperator.gt
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.sort
import org.litote.kmongo.MongoOperator.sum
import org.litote.kmongo.util.KMongoUtil
import kotlin.test.assertEquals


class MongoOperatorTest : KMongoRootTest() {

    @Test
    fun simpleOperator()
            = assertEquals("{ qty: { \$gt: 25 } }", "{ qty: { $gt: 25 } }")


    @Test
    fun inOperator()
            = assertEquals(
            "{_id: { \$in: [ 5,  ObjectId('507c35dd8fada716c89d0013') ] }}",
            "{_id: { $`in`: [ 5,  ObjectId('507c35dd8fada716c89d0013') ] }}")

    @Test
    fun rawString() {
        val dollar = "\$"
        assertEquals(
                """
                [
                     { ${dollar}match: { status: "A" } },
                     { ${dollar}group: { _id: "${dollar}cust_id", total: { ${dollar}sum: "${dollar}amount" } } },
                     { ${dollar}sort: { total: -1 } }
                ]
                """,
                KMongoUtil.formatJson("""
                [
                     { $match: { status: "A" } },
                     { $group: { _id: "$ cust_id", total: { $sum: "$ amount" } } },
                     { $sort: { total: -1 } }
                ]
                """))
    }
}