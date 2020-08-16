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

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.issues.Issue94InconsistentIdGeneration.Task
import org.litote.kmongo.newId
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertTrue

/**
 *
 */
class Issue94InconsistentIdGeneration : AllCategoriesKMongoBaseTest<Task>() {

    @Serializable
    data class Task(@Contextual val _id: Id<Task> = newId())

    @Test
    fun generateTwoTasks() {
        val t1 = Task()
        val t2 = Task()
        col.insertOne(t1)
        col.insertOne(t2)
        col.withDocumentClass<Document>().find().forEach {
            assertTrue { it.get("_id") is ObjectId }
        }
    }

}