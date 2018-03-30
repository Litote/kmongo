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
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.litote.kmongo.property.KPropertyPath
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.platform.JavaToKotlinClassMap

/**
 *
 */
@SupportedAnnotationTypes("org.litote.kmongo.Data")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class KMongoAnnotationProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            for (element in roundEnv.getElementsAnnotatedWith(Data::class.java)) {
                if (element.kind != ElementKind.CLASS) {
                    error("$element is annotated with @Data but is not a class")
                }
                registerSuperclasses(element as TypeElement)
            }
            for (element in roundEnv.getElementsAnnotatedWith(Data::class.java)) {
                process(element as TypeElement)
            }
        } catch (e: Throwable) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.stackTrace())
        }

        return false
    }

    private fun registerSuperclasses(element: TypeElement) {
        val superclass = element.superclass
        if (superclass is DeclaredType && superclass.toString() != "java.lang.Object") {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                "$element : $superclass - ${superclass.getAnnotation(Data::class.java)}"
            )
            val superElement = superclass.asElement() as TypeElement
            if (superElement.getAnnotation(Data::class.java) == null) {
                registerSuperclasses(superElement)
                process(superElement)
            }
        }
    }

    private fun process(element: TypeElement) {
        val pack = processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()
        val sourceClassName = element.asClassName()
        val className = generatedClassName(element)
        val fileBuilder = FileSpec.builder(pack, className)

        val superMirrorClass = element.superclass
        val superclass: TypeName =
            if (superMirrorClass is DeclaredType && superMirrorClass.toString() != "java.lang.Object") {
                val superElement = superMirrorClass.asElement() as TypeElement
                ParameterizedTypeName.get(
                    ClassName(
                        processingEnv.elementUtils.getPackageOf(superElement).qualifiedName.toString(),
                        generatedClassName(superElement)
                    ),
                    TypeVariableName("T")
                )
            } else {
                ParameterizedTypeName.get(
                    KPropertyPath::class.asClassName(),
                    TypeVariableName("T"),
                    sourceClassName.asNullable()
                )
            }

        val classBuilder = TypeSpec.classBuilder(className)
            .addTypeVariable(TypeVariableName("T"))
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter(
                        "previous",
                        ParameterizedTypeName.get(
                            KPropertyPath::class.asClassName(),
                            TypeVariableName("T"),
                            TypeVariableName("*")
                        ).asNullable()
                    )
                    .addParameter(
                        "property",
                        ParameterizedTypeName.get(
                            KProperty1::class.asClassName(),
                            TypeVariableName("*"),
                            sourceClassName.asNullable()
                        )
                    )
                    .build()
            )
            .superclass(superclass)
            .addSuperclassConstructorParameter("%1L,%2L", "previous", "property")

        if(!element.modifiers.contains(FINAL)) {
            classBuilder.addModifiers(KModifier.OPEN)
        }

        val companionObject = TypeSpec.companionObjectBuilder()

        for (e in element.enclosedElements) {
            if (e is VariableElement) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.NOTE,
                    "${e.simpleName}-${e.asType()}"
                )
                val returnType = processingEnv.typeUtils.asElement(e.asType()) as? TypeElement
                val packageOfReturnType =
                    if (returnType == null) "" else processingEnv.elementUtils.getPackageOf(returnType).qualifiedName.toString()
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, e.asType().toString())
                val type = processingEnv.typeUtils.asElement(e.asType())
                if (type != null) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.NOTE,
                        "${e.asType()}-annot: ${type.getAnnotation(Data::class.java)}"
                    )
                }
                val annotated = type?.getAnnotation(Data::class.java) != null
                val propertyType = e.javaToKotlinType()
                val filePropertyClass: TypeName =
                    if (annotated) {
                        ParameterizedTypeName.get(
                            ClassName(
                                packageOfReturnType,
                                generatedClassName(returnType!!)
                            ),
                            sourceClassName
                        )
                    } else {
                        ParameterizedTypeName.get(
                            KProperty1::class.asClassName(),
                            sourceClassName,
                            propertyType.asNullable()
                        )
                    }

                val propertyReference =
                    if (element
                            .enclosedElements
                            .firstOrNull { it.simpleName.toString() == "get${capitalize(e.simpleName.toString())}" }
                            ?.modifiers
                            ?.contains(PRIVATE) != false
                    ) {
                        CodeBlock.builder().add(
                            "org.litote.kmongo.property.findProperty<%1T,%2T>(%3S)",
                            sourceClassName,
                            if (annotated) {
                                ClassName(
                                    packageOfReturnType,
                                    returnType!!.simpleName.toString()
                                )
                            } else {
                                propertyType.asNullable()
                            },
                            e.simpleName
                        ).build()
                    } else {
                        CodeBlock.builder().add("%1T::%2L", sourceClassName, e.simpleName).build()
                    }

                //add companion property
                companionObject.addProperty(
                    PropertySpec
                        .varBuilder(generatedClassName(e), filePropertyClass)
                        .mutable(false)
                        .getter(
                            FunSpec.getterBuilder().apply {
                                if (annotated) {
                                    addCode("return %1T(null,%2L)", filePropertyClass, propertyReference)
                                } else {
                                    addCode("return %1L", propertyReference)
                                }
                            }.build()
                        )
                        .build()
                )

                val classPropertyClass: TypeName =
                    if (annotated) {
                        ParameterizedTypeName.get(
                            ClassName(
                                packageOfReturnType,
                                generatedClassName(returnType!!)
                            ),
                            TypeVariableName("T")
                        )
                    } else {
                        ParameterizedTypeName.get(
                            KProperty1::class.asClassName(),
                            TypeVariableName("T"),
                            propertyType.asNullable()
                        )
                    }

                //add class property
                classBuilder.addProperty(
                    PropertySpec
                        .varBuilder(e.simpleName.toString(), classPropertyClass)
                        .mutable(false)
                        .getter(
                            FunSpec.getterBuilder().apply {
                                addCode(
                                    "return %1L(this,%2L)\n",
                                    if (annotated) {
                                        generatedClassName(returnType!!)
                                    } else {
                                        KPropertyPath::class.asClassName()
                                    },
                                    propertyReference
                                )
                            }.build()
                        )
                        .build()
                )
            }
        }

        //add class
        fileBuilder.addType(
            classBuilder
                .companionObject(companionObject.build())
                .build()
        )

        val kotlinFile = fileBuilder.build()
        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            processingEnv.filer.getResource(
                StandardLocation.SOURCE_OUTPUT,
                kotlinFile.packageName,
                kotlinFile.name
            ).name
        )

        kotlinFile.writeTo(
            Paths.get(
                processingEnv.filer.getResource(
                    StandardLocation.SOURCE_OUTPUT,
                    "",
                    kotlinFile.name
                ).toUri()
            ).parent
        )
    }

    //see https://github.com/square/kotlinpoet/issues/236
    private fun Element.javaToKotlinType(): TypeName {
        val className = JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(this.asType().asTypeName().toString()))
            ?.asSingleFqName()?.asString()
        return if (className == null) {
            (this as? TypeElement)?.asClassName() ?: asType().asTypeName()
        } else {
            ClassName.bestGuess(className)
        }
    }

    private fun generatedClassName(element: Element): String {
        return "${element.simpleName}_"
    }


    private fun capitalize(line: String): String {
        return Character.toUpperCase(line[0]) + line.substring(1)
    }

    private fun Throwable.stackTrace(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        printStackTrace(pw)
        return sw.toString()
    }

}