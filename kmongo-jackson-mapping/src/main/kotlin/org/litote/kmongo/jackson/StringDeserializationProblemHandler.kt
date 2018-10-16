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

package org.litote.kmongo.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler
import org.litote.kmongo.MongoOperator.oid

/**
 *
 */
internal object StringDeserializationProblemHandler : DeserializationProblemHandler() {

    override fun handleUnexpectedToken(
        ctxt: DeserializationContext,
        targetType: Class<*>,
        t: JsonToken,
        p: JsonParser,
        failureMsg: String?
    ): Any {
        //handle ObjectId -> String mapping
        if (targetType == String::class.java && t == JsonToken.START_OBJECT) {
            val fieldName = p.nextFieldName()
            if (fieldName == "$oid") {
                return p.nextTextValue()
                    .also {
                        while(p.currentToken != JsonToken.END_OBJECT) {
                            p.nextToken()
                        }
                    }

            }
        }
        return super.handleUnexpectedToken(ctxt, targetType, t, p, failureMsg)
    }
}