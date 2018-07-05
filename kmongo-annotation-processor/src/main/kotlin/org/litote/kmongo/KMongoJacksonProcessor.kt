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
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
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
        writeDeserializer(element)
    }

    private fun writeSerializer(element: TypeElement) {
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
                        a.properties(element).forEach { e ->
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

    private fun writeDeserializer(element: TypeElement) {
        val sourceClassName = element.asClassName()
        val className = generatedDeserializer(element)
        val fileBuilder = FileSpec.builder(a.getPackage(element), className)

        val superclass = ParameterizedTypeName.get(
            ClassName.bestGuess("com.fasterxml.jackson.databind.deser.std.StdDeserializer"), sourceClassName
        )
        val classBuilder = TypeSpec.classBuilder(className)
            .superclass(superclass)
            .addSuperclassConstructorParameter(CodeBlock.of("%T::class.java", sourceClassName))
            .companionObject(
                TypeSpec.companionObjectBuilder()
                    .apply {
                        a.properties(element).forEach { e ->
                            if (e.asTypeName() is ParameterizedTypeName) {
                                val propertyName = e.simpleName
                                val typeReference = ParameterizedTypeName.get(
                                    ClassName.bestGuess("com.fasterxml.jackson.core.type.TypeReference"),
                                    e.asTypeName().javaToKotlinType()
                                )

                                addProperty(
                                    PropertySpec.builder("${propertyName}_reference", typeReference)
                                        .initializer(
                                            CodeBlock.of(
                                                "object : %T() {}",
                                                typeReference
                                            )
                                        )
                                        .build()
                                )
                            }
                        }
                    }
                    .build()
            )
            .addFunction(
                FunSpec.builder("deserialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        ParameterSpec.builder(
                            "p",
                            ClassName.bestGuess("com.fasterxml.jackson.core.JsonParser")
                        ).build()
                    )
                    .addParameter(
                        ParameterSpec.builder(
                            "ctxt",
                            ClassName.bestGuess("com.fasterxml.jackson.databind.DeserializationContext")
                        ).build()
                    )
                    .returns(sourceClassName)
                    .addStatement("with(p) {")
                    .apply {
                        //generate fields
                        a.properties(element).forEach { e ->
                            addStatement("var %N: %T = null", e.simpleName, e.javaToKotlinType().asNullable())
                        }
                    }
                    //generate while
                    .addStatement(
                        "while (currentToken != %T && currentToken != %T) {%W",
                        ClassName.bestGuess("com.fasterxml.jackson.core.JsonToken.END_OBJECT"),
                        ClassName.bestGuess("com.fasterxml.jackson.core.JsonToken.END_ARRAY")
                    )
                    .addStatement("nextToken()%W")
                    .addStatement(
                        "if (currentToken == %T || currentToken == %T) { break }%W",
                        ClassName.bestGuess("com.fasterxml.jackson.core.JsonToken.END_OBJECT"),
                        ClassName.bestGuess("com.fasterxml.jackson.core.JsonToken.END_ARRAY")
                    )
                    .addStatement("val fieldName = currentName")
                    .addStatement("nextToken()")
                    .addStatement("if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {")
                    //generate set
                    .apply {
                        a.properties(element).forEach { e ->
                            val propertyName = e.simpleName
                            val jsonField = e.simpleName
                            val type = e.asTypeName()
                            val writeMethod = when (type.asNonNullable().toString()) {
                                "java.lang.String" -> CodeBlock.of("text")
                                "java.lang.Boolean", "boolean" -> CodeBlock.of("booleanValue")
                                "java.lang.Integer", "int" -> CodeBlock.of("intValue")
                                "java.lang.Long", "long" -> CodeBlock.of("longValue")
                                "java.lang.Short", "short" -> CodeBlock.of("shortValue")
                                "java.lang.Float", "float" -> CodeBlock.of("floatValue")
                                "java.lang.Double", "double" -> CodeBlock.of("doubleValue")
                                "java.math.BigInteger" -> CodeBlock.of("bigIntegerValue")
                                "java.math.BigDecimal" -> CodeBlock.of("decimalValue")
                                else -> if (e.asTypeName() is ParameterizedTypeName) {
                                    CodeBlock.of("readValueAs(${propertyName}_reference)")
                                } else {
                                    CodeBlock.of("readValueAs(%T::class.java)", e.javaToKotlinType())
                                }
                            }
                            addStatement("%S -> %N = p.%L", jsonField, propertyName, writeMethod)
                        }
                        addStatement(
                            "else -> if (currentToken == %T || currentToken == %T) { p.skipChildren() } else { nextToken() }",
                            ClassName.bestGuess("com.fasterxml.jackson.core.JsonToken.END_OBJECT"),
                            ClassName.bestGuess("com.fasterxml.jackson.core.JsonToken.END_ARRAY")
                        )
                    }
                    //generate end while
                    .addStatement(" }%W }")
                    .apply {
                        val parameters =
                            (element.enclosedElements.first { it.kind == ElementKind.CONSTRUCTOR } as ExecutableElement)
                                .parameters.map {
                                "${it.simpleName}${if (it.asTypeName().nullable) "" else "!!"}"
                            }
                        addStatement("return %T(%L)%W}", sourceClassName, parameters.joinToString())
                    }
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