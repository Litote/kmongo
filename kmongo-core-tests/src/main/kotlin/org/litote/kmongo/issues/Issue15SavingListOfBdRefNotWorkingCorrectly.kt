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

package org.litote.kmongo.issues

import com.mongodb.DBRef
import org.bson.types.ObjectId
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.issues.Issue15SavingListOfBdRefNotWorkingCorrectly.MongoReportDefinition
import org.litote.kmongo.save
import kotlin.test.assertEquals

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class)
class Issue15SavingListOfBdRefNotWorkingCorrectly : AllCategoriesKMongoBaseTest<MongoReportDefinition>() {

    data class MongoReportDefinition(val _id: ObjectId, val title: String, val columns: List<DBRef>)

    @Test
    fun testSerializeAndDeserializeListOfDBRefs() {
        val dbRefs = listOf(DBRef("test", "id1"), DBRef("test2", "id2"))
        val saveCard = MongoReportDefinition(
            _id = ObjectId(),
            title = "title",
            columns = dbRefs
        )
        database.getCollection<MongoReportDefinition>("col")
            .save(saveCard)
        assertEquals(dbRefs, database.getCollection("col").findOne()!!["columns"])
    }


}