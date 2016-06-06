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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.PropertyFilter
import com.fasterxml.jackson.databind.ser.PropertyWriter
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter

/**
 *
 */
internal object FilterIdIntrospector : JacksonAnnotationIntrospector() {

    object IdPropertyFilter : SimpleBeanPropertyFilter() {

        private fun include(pojo: Any, writer: PropertyWriter): Boolean {
            return writer.name != "_id"
        }

        override fun serializeAsField(pojo: Any, jgen: JsonGenerator, provider: SerializerProvider, writer: PropertyWriter) {
            if (include(pojo, writer)) {
                writer.serializeAsField(pojo, jgen, provider)
            } else if (!jgen.canOmitFields()) { // since 2.3
                writer.serializeAsOmittedField(pojo, jgen, provider)
            }
        }
    }

    object IdPropertyFilterProvider : FilterProvider() {


        override fun findFilter(filterId: Any): BeanPropertyFilter? {
            throw UnsupportedOperationException()
        }

        override fun findPropertyFilter(filterId: Any, valueToFilter: Any): PropertyFilter {
            return IdPropertyFilter
        }
    }

    override fun findFilterId(a: Annotated): Any? {
        return "_id"
    }
}