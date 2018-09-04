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

package org.litote.kmongo

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * TODO check internal private protected on class -> kotlin metadata
 * TODO support nullable generic -> kotlin metadata
 */
@SupportedAnnotationTypes(
    "org.litote.kmongo.Data",
    "org.litote.kmongo.DataRegistry",
    "org.litote.kmongo.JacksonData",
    "org.litote.kmongo.JacksonDataRegistry",
    "org.litote.kmongo.NativeData",
    "org.litote.kmongo.NativeDataRegistry"
)
class KMongoAnnotationProcessor : AbstractProcessor() {

    private lateinit var a: KMongoAnnotations

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        a = KMongoAnnotations(processingEnv)
        a.debug { annotations }
        a.debug { processingEnv.options }

        return try {
            KMongoDataProcessor(a).processDataClasses(roundEnv)
                    && KMongoJacksonProcessor(a).processJacksonDataClasses(roundEnv)
                    && KMongoNativeProcessor(a).processNativeDataClasses(roundEnv)
        } catch (e: Throwable) {
            a.logError(e)
            false
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        KMongoAnnotations(processingEnv).debug { SourceVersion.latest() }
        return SourceVersion.latest()
    }

}