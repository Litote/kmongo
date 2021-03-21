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

package org.litote.kmongo.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler
import org.litote.kmongo.Id
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.id.WrappedObjectId

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
        if (t == JsonToken.START_OBJECT) {
            val fieldName = p.nextFieldName()
            if (fieldName == "$oid") {
                //handle ObjectId -> String mapping
                if (targetType == String::class.java) {
                    return p.nextTextValue()
                        .also {
                            while (p.currentToken != JsonToken.END_OBJECT) {
                                p.nextToken()
                            }
                        }

                }
                //handle ObjectId -> Id mapping
                else if (targetType == Id::class.java) {
                    return WrappedObjectId<Any>(p.nextTextValue())
                        .also {
                            while (p.currentToken != JsonToken.END_OBJECT) {
                                p.nextToken()
                            }
                        }
                }
            }
        }
        return super.handleUnexpectedToken(ctxt, targetType, t, p, failureMsg)
    }
}