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

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.addToSet
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.newId
import kotlin.test.assertEquals

@Serializable
data class Data(
    @Contextual @SerialName("_id") val _id: Id<Data> = newId(),
    val refs: List<@Contextual Id<Data2>> = listOf(),
)

@Serializable
data class Data2(
    @Contextual @SerialName("_id") val id: Id<Data2> = newId()
)

/**
 *
 */
class Issue258UpdateListOfReferences : AllCategoriesKMongoBaseTest<Data>() {

    @Test
    fun `test insert and load`() {
        val document = Data()
        val idToAdd: Id<Data2> = newId()
        col.insertOne(document)
        col.updateOne(
            Data::_id eq document._id,
            addToSet(Data::refs, idToAdd)
        )
        assertEquals(document.copy(refs = listOf(idToAdd)), col.findOne(Data::_id eq document._id))
    }

}