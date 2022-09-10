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

package org.litote.kmongo.model

import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Id
import org.litote.kmongo.model.other.SimpleReferencedData
import org.litote.kmongo.newId
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
    val map2: Map<Locale, SimpleReferenced2Data> = emptyMap(),
    val nullableFloat: Float? = null,
    val nullableBoolean: Boolean? = null,
    private val privateData: String = "",
    val id: Id<out Any?> = newId(),
    val byteArray: ByteArray? = null
    //TODO support mutable collections
    /*,
    val mutableSet:MutableSet<String> = mutableSetOf()*/
) {
    companion object {
        val test: String = "should not be serialized"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestData) return false

        if (set != other.set) return false
        if (list != other.list) return false
        if (name != other.name) return false
        if (date != other.date) return false
        if (referenced != other.referenced) return false
        if (map != other.map) return false
        if (nullableFloat != other.nullableFloat) return false
        if (nullableBoolean != other.nullableBoolean) return false
        if (privateData != other.privateData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = set.hashCode()
        result = 31 * result + list.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (referenced?.hashCode() ?: 0)
        result = 31 * result + map.hashCode()
        result = 31 * result + (nullableFloat?.hashCode() ?: 0)
        result = 31 * result + (nullableBoolean?.hashCode() ?: 0)
        result = 31 * result + privateData.hashCode()
        return result
    }

    override fun toString(): String {
        return "TestData(set=$set, list=$list, name=$name, date=$date, referenced=$referenced, map=$map, nullableFloat=$nullableFloat, nullableBoolean=$nullableBoolean, privateData='$privateData')"
    }


}
