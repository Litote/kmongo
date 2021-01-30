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

package org.litote.kmongo.model.other

import org.litote.kmongo.Data
import org.litote.kmongo.model.SimpleReferenced2Data
import org.litote.kmongo.model.SubData
import org.litote.kmongo.model.TestData
import java.util.Locale

/**
 *
 */
@Data
class SimpleReferencedData {

    var version: Int = 0
    private val pojo2: SimpleReferenced2Data? = null
    var pojo: TestData? = null
    private val subPojo: SubData? = null
    val labels: Map<Locale, List<String>> = emptyMap()

    override fun toString(): String {
        return "SimpleReferencedData(version=$version, pojo2=$pojo2, pojo=$pojo, subPojo=$subPojo)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleReferencedData) return false

        if (version != other.version) return false
        if (pojo2 != other.pojo2) return false
        if (pojo != other.pojo) return false
        if (subPojo != other.subPojo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + (pojo2?.hashCode() ?: 0)
        result = 31 * result + (pojo?.hashCode() ?: 0)
        result = 31 * result + (subPojo?.hashCode() ?: 0)
        return result
    }


}