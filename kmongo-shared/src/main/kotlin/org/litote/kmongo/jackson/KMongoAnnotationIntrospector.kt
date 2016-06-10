/*
 * Copyright (C) 2016 Litote
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

package org.litote.kmongo.jackson

import com.fasterxml.jackson.databind.PropertyName
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector
import org.litote.kmongo.util.MongoIdUtil
import java.lang.reflect.Field

/**
 *
 */
internal open class KMongoAnnotationIntrospector : NopAnnotationIntrospector() {

    companion object {
        val INTROSPECTOR = KMongoAnnotationIntrospector()
    }

    override fun findNameForDeserialization(a: Annotated): PropertyName? {
        if (isAnnotatedWithMongoId(a)) {
            return PropertyName("_id")
        }
        return super.findNameForDeserialization(a)
    }

    override fun findNameForSerialization(a: Annotated): PropertyName? {
        if (isAnnotatedWithMongoId(a)) {
            return PropertyName("_id")
        }
        return super.findNameForSerialization(a)
    }

    private fun isAnnotatedWithMongoId(a: Annotated): Boolean {
        val field = a.annotated
        if (field !is Field) {
            return false
        }
        val p = MongoIdUtil.getAnnotatedMongoIdProperty(field.declaringClass.kotlin)
        return p != null && p.name == a.name
    }
}