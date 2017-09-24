/*
 * Copyright (C) 2017 Litote
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
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.findOneAndDelete
import org.litote.kmongo.findOneAndReplace
import org.litote.kmongo.findOneAndUpdate
import org.litote.kmongo.model.Friend
import kotlin.test.assertNull

/**
 *
 */
class Issue34FindOneAndUpdateOrReplaceOrDeleteCouldReturnNull : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun findOneAndReplaceCouldReturnNull() {
        assertNull(col.findOneAndReplace("{}", Friend("test")))
    }

    @Test
    fun findOneAndUpdateCouldReturnNull() {
        assertNull(col.findOneAndUpdate("{}", "{$set:{name:'John'}}"))
    }

    @Test
    fun findOneAndDeleteCouldReturnNull() {
        assertNull(col.findOneAndDelete("{}"))
    }

}