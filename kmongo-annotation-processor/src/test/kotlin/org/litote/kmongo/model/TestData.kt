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

package org.litote.kmongo.model

import org.litote.kmongo.Id
import org.litote.kmongo.JacksonData
import org.litote.kmongo.model.other.SimpleReferencedData
import java.util.Date
import java.util.Locale

/**
 *
 */
@JacksonData
open class TestData(
    val set: Set<SimpleReferencedData> = emptySet(),
    val list: List<List<Boolean>> = emptyList(),
    //TODO support nullable generic
    //val nullableList: List<List<Boolean?>> = emptyList(),
    val name: String? = null,
    val date: Date? = null,
    val referenced: SimpleReferencedData? = null,
    val map: Map<Id<Locale>, Set<String>> = emptyMap(),
    val nullableFloat: Float? = null,
    val nullableBoolean: Boolean? = null,
    private val privateData: String = ""
) {
    companion object {
        val test: String = "should not be serialized"
    }

}
