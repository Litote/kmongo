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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType

/**
 *
 */
internal class KMongoJacksonProcessor(val a: KMongoAnnotations) {

    fun processJacksonDataClasses(roundEnv: RoundEnvironment): Boolean {
        val classes = a.getAnnotatedClasses<JacksonData, JacksonDataRegistry>(roundEnv)
            .filter {
                val superclass = (it as TypeElement).superclass
                if (superclass is DeclaredType && superclass.toString() != "java.lang.Object") {
                    a.log("$it: inheritance not supported for now - skip")
                    false
                } else {
                    true
                }
            }

        classes.forEach {
            process(it as TypeElement)
        }

        return classes.isNotEmpty()
    }

    private fun process(element: TypeElement) {
        writeSerializer(element)
    }

    private fun writeSerializer(element: TypeElement) {
        val pack = a.getPackage(element)
        val sourceClassName = element.asClassName()
        val className = generatedSerializer(element)
        val fileBuilder = FileSpec.builder(a.getPackage(element), className)

        val superclass = ParameterizedTypeName.get(
            ClassName.bestGuess("com.fasterxml.jackson.databind.ser.std.StdSerializer"), sourceClassName
        )
        val classBuilder = TypeSpec.classBuilder(className)
            .superclass(superclass)
            .addSuperclassConstructorParameter(CodeBlock.of("%T::class.java", sourceClassName))
            .addFunction(
                FunSpec.builder("serialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        ParameterSpec.builder("value", sourceClassName).build()
                    )
                    .addParameter(
                        ParameterSpec.builder(
                            "gen",
                            ClassName.bestGuess("com.fasterxml.jackson.core.JsonGenerator")
                        ).build()
                    )
                    .addParameter(
                        ParameterSpec.builder(
                            "serializers",
                            ClassName.bestGuess("com.fasterxml.jackson.databind.SerializerProvider")
                        ).build()
                    )
                    .addStatement(
                        "gen.writeStartObject()"
                    )
                    .apply {
                        for (e in element.enclosedElements) {
                            if (e is VariableElement && e.modifiers.none { notSupportedModifiers.contains(it) }) {
                                a.debug { "${e.simpleName}-${e.asType()}" }
                                val propertyName = e.simpleName
                                val jsonField = e.simpleName
                                val type = e.asType()
                                val nullable = e.asTypeName().nullable
                                val fieldAccessor =
                                    a.propertyReference(
                                        element,
                                        e,
                                        {
                                            a.findPropertyValue(
                                                sourceClassName,
                                                e.javaToKotlinType(),
                                                "value",
                                                "$propertyName"
                                            )
                                        }) {
                                        CodeBlock.of("value.$propertyName")
                                    }

                                val fieldName = "_${propertyName}_"

                                addStatement("gen.writeFieldName(\"$jsonField\")")
                                addStatement("val $fieldName = $fieldAccessor")
                                addStatement(
                                    if (nullable) {
                                        "if($fieldName == null) { gen.writeNull() } else {"
                                    } else {
                                        ""
                                    } +
                                            when (type.toString()) {
                                                "java.lang.String" -> "gen.writeString($fieldName)"
                                                "java.lang.Boolean", "boolean" -> "gen.writeBoolean($fieldName)"
                                                "java.lang.Integer",
                                                "java.lang.Long",
                                                "java.lang.Short",
                                                "java.lang.Float",
                                                "java.lang.Double",
                                                "java.math.BigInteger",
                                                "java.math.BigDecimal",
                                                "short",
                                                "int",
                                                "long",
                                                "float",
                                                "double" -> "gen.writeNumber($fieldName)"
                                                else -> "serializers.defaultSerializeValue($fieldName, gen)"
                                            } +
                                            if (nullable) {
                                                "}"
                                            } else {
                                                ""
                                            }
                                )
                            }
                        }
                    }
                    .addStatement(
                        "gen.writeEndObject()"
                    )

                    .build()
            )


        //add classes
        fileBuilder.addType(
            classBuilder
                .build()
        )

        a.writeFile(fileBuilder)
    }

    private fun generatedSerializer(element: Element): String = "${element.simpleName}_Serializer"
    private fun generatedDeserializer(element: Element): String = "${element.simpleName}_Deserializer"
}