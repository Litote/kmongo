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
import org.litote.kmongo.MongoId
import java.lang.reflect.Field
import kotlin.reflect.jvm.kotlinProperty

/**
 *
 */
internal class KMongoAnnotationIntrospector : NopAnnotationIntrospector() {

    companion object {
        val INTROSPECTOR = KMongoAnnotationIntrospector()
        val ID_PROPERTY_NAME: PropertyName = PropertyName.construct("_id")
    }

    override fun findNameForDeserialization(a: Annotated): PropertyName? {
        if (isAnnotatedWithMongoId(a)) {
            return ID_PROPERTY_NAME
        }
        return super.findNameForDeserialization(a)
    }

    override fun findNameForSerialization(a: Annotated): PropertyName? {
        if (isAnnotatedWithMongoId(a)) {
            return ID_PROPERTY_NAME
        }
        return super.findNameForSerialization(a)
    }

    private fun isAnnotatedWithMongoId(a: Annotated): Boolean {
        val field = a.annotated
        if (field !is Field) {
            return false
        }
        //fix kotlin reflection issue with enum
        if (field.declaringClass.isEnum) {
            return false
        }
        return field.kotlinProperty?.annotations?.any { it is MongoId } ?: false
    }
}