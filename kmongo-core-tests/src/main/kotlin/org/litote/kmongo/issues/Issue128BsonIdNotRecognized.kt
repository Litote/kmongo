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

import org.bson.codecs.pojo.annotations.BsonId
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.issues.Issue128BsonIdNotRecognized.Ticket
import org.litote.kmongo.save
import java.util.Date

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class)
class Issue128BsonIdNotRecognized : KMongoBaseTest<Ticket>() {

    abstract class ReportableData(@BsonId val id: Any) {
        var metadata: Map<String, Any?> = emptyMap()
        var updated: Date = Date()
        var dirty: Boolean = false
    }

    open class Ticket(id: String) : ReportableData(id) {
        var title: String? = null
        var status: String? = null
        var opened: Date? = null
        var closed: Date? = null
    }

    @Test
    fun saveTwiceDoesNotFail() {
        col.save(Ticket("apple").apply {
            title = "Testing"
            status = "Closed"
        })
        col.save(Ticket("apple").apply {
            title = "Testing"
            status = "Closed"
        })
    }
}