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

import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoRootTest
import org.litote.kmongo.MongoOperator.expr
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.SerializationMappingCategory
import org.litote.kmongo.div
import org.litote.kmongo.expr
import org.litote.kmongo.from
import org.litote.kmongo.json
import org.litote.kmongo.match
import org.litote.kmongo.projection
import org.litote.kmongo.variable
import kotlin.test.assertEquals

data class HistoryEventWrapper(val event: HistoricVariableInstance)
data class HistoricVariableInstance(val processInstanceId: String)
data class HistoricProcessInstance(val processInstanceId: String)

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class, SerializationMappingCategory::class)
class Issue255LookupWithNestedObjects : KMongoRootTest() {

    @Test
    fun `test json generation lookup`() {
        assertEquals(
            """{"$match": {"$expr": {"${'$'}event.processInstanceId": "${'$'}${'$'}processInstanceId"}}}""",
            match(
                expr(
                    (HistoryEventWrapper::event / HistoricVariableInstance::processInstanceId).projection
                            from HistoricProcessInstance::processInstanceId.variable
                )
            ).json
        )
    }
}