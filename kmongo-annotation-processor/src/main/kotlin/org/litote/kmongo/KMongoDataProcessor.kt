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
import org.litote.kmongo.property.KPropertyPath
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import kotlin.reflect.KProperty1

/**
 *
 */
internal class KMongoDataProcessor(val a: KMongoAnnotations) {

    fun processDataClasses(roundEnv: RoundEnvironment): Boolean {
        val dataClasses = a.getAnnotatedClasses<Data, DataRegistry>(roundEnv)

        for (element in dataClasses.toList()) {
            if (element.kind != ElementKind.CLASS) {
                error("$element is annotated with @Data but is not a class")
            }
            registerSuperclasses(element as TypeElement, dataClasses)
        }
        for (element in dataClasses) {
            process(element as TypeElement, dataClasses)
        }
        return dataClasses.isNotEmpty()
    }

    private fun registerSuperclasses(element: TypeElement, dataClasses: MutableSet<Element>) {
        val superclass = element.superclass
        if (superclass is DeclaredType && superclass.toString() != "java.lang.Object") {
            a.debug { "$element : $superclass - ${superclass.getAnnotation(Data::class.java)}" }
            val superElement = superclass.asElement() as TypeElement
            if (!dataClasses.contains(superElement)) {
                dataClasses.add(superElement)
                registerSuperclasses(superElement, dataClasses)
                process(superElement, dataClasses)
            }
        }
    }

    private fun process(element: TypeElement, dataClasses: MutableSet<Element>) {
        val pack = a.getPackage(element)
        val sourceClassName = element.asClassName()
        val className = generatedClassName(element)
        val fileBuilder = FileSpec.builder(pack, className)

        val superMirrorClass = element.superclass
        val superclass: TypeName =
            if (superMirrorClass is DeclaredType && superMirrorClass.toString() != "java.lang.Object") {
                val superElement = superMirrorClass.asElement() as TypeElement
                ParameterizedTypeName.get(
                    ClassName(
                        a.processingEnv.elementUtils.getPackageOf(superElement).qualifiedName.toString(),
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

        val collectionSuperclass: TypeName =
            if (superMirrorClass is DeclaredType && superMirrorClass.toString() != "java.lang.Object") {
                val superElement = superMirrorClass.asElement() as TypeElement
                ParameterizedTypeName.get(
                    ClassName(
                        a.processingEnv.elementUtils.getPackageOf(superElement).qualifiedName.toString(),
                        "${generatedClassName(superElement)}Col"
                    ),
                    TypeVariableName("T")
                )
            } else {
                ParameterizedTypeName.get(
                    KPropertyPath::class.asClassName(),
                    TypeVariableName("T"),
                    ParameterizedTypeName.get(
                        ClassName("kotlin.collections", "Collection"),
                        sourceClassName
                    ).asNullable()
                )
            }

        val collectionClassBuilder = TypeSpec.classBuilder("${className}Col")
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
                            ParameterizedTypeName.get(
                                ClassName("kotlin.collections", "Collection"),
                                sourceClassName
                            ).asNullable()
                        )
                    )
                    .build()
            )
            .superclass(collectionSuperclass)
            .addSuperclassConstructorParameter("%1L,%2L", "previous", "property")


        if (!element.modifiers.contains(Modifier.FINAL)) {
            classBuilder.addModifiers(KModifier.OPEN)
            collectionClassBuilder.addModifiers(KModifier.OPEN)
        }

        val companionObject = TypeSpec.companionObjectBuilder()

        a.properties(element).forEach { e ->
            a.debug { "${e.simpleName}-${e.asType()}" }
            val type = e.asType()
            val returnType = a.processingEnv.typeUtils.asElement(type) as? TypeElement
            if (type != null) {
                a.debug { "$type-annot: ${type.getAnnotation(Data::class.java)}" }
            }
            val annotatedCollection = type.run {
                if (this is ArrayType) {
                    dataClasses.contains(a.processingEnv.typeUtils.asElement(componentType))
                } else if (this is DeclaredType
                    && a.processingEnv.typeUtils.isAssignable(
                        a.processingEnv.typeUtils.erasure(this),
                        a.processingEnv.elementUtils.getTypeElement("java.util.Collection").asType()
                    )
                ) {
                    typeArguments.firstOrNull()?.run {
                        dataClasses.contains(a.processingEnv.typeUtils.asElement(this))
                    } == true
                } else {
                    false
                }
            }
            val annotated = returnType?.let { dataClasses.contains(it) } ?: false || annotatedCollection
            val propertyType = e.javaToKotlinType()
            val packageOfReturnType =
                if (returnType == null) ""
                else if (annotatedCollection) a.enclosedCollectionPackage(type)
                else a.processingEnv.elementUtils.getPackageOf(returnType).qualifiedName.toString()

            val companionPropertyClass: TypeName =
                if (annotated) {
                    ParameterizedTypeName.get(
                        ClassName(
                            packageOfReturnType,
                            a.generatedClassProperty(type, annotatedCollection)
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
                a.propertyReference(
                    element,
                    e,
                    {
                        a.findByProperty(
                            sourceClassName,
                            if (annotated) {
                                ClassName(
                                    packageOfReturnType,
                                    returnType!!.simpleName.toString()
                                )
                            } else {
                                propertyType.asNullable()
                            },
                            e.simpleName.toString()
                        )
                    }
                ) { CodeBlock.builder().add("%1T::%2L", sourceClassName, e.simpleName).build() }


            //add companion property
            companionObject.addProperty(
                PropertySpec
                    .varBuilder(generatedCompanionFieldName(e), companionPropertyClass)
                    .mutable(false)
                    .getter(
                        FunSpec.getterBuilder().apply {
                            if (annotated) {
                                addCode("return %1T(null,%2L)", companionPropertyClass, propertyReference)
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
                            a.generatedClassProperty(type, annotatedCollection)
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
                    .varBuilder(generatedFieldName(e), classPropertyClass)
                    .mutable(false)
                    .getter(
                        FunSpec.getterBuilder().apply {
                            addCode(
                                "return %1L(this,%2L)\n",
                                if (annotated) {
                                    a.generatedClassProperty(type, annotatedCollection)
                                } else {
                                    KPropertyPath::class.asClassName()
                                },
                                propertyReference
                            )
                        }.build()
                    )
                    .build()
            )

            collectionClassBuilder.addProperty(
                PropertySpec
                    .varBuilder(generatedFieldName(e), classPropertyClass)
                    .mutable(false)
                    .getter(
                        FunSpec.getterBuilder().apply {
                            addCode(
                                "return %1L(this,%2L)\n",
                                if (annotated) {
                                    a.generatedClassProperty(type, annotatedCollection)
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

        //add classes
        fileBuilder.addType(
            classBuilder
                .companionObject(companionObject.build())
                .build()
        )
        fileBuilder.addType(collectionClassBuilder.build())

        a.writeFile(fileBuilder)
    }

    private fun generatedClassName(element: Element): String = "${element.simpleName}_"

    companion object {
        val KPROPERTY_PATH_FIELDS = setOf(
            "annotations",
            "getter",
            "isAbstract",
            "isConst",
            "isFinal",
            "isLateinit",
            "isOpen",
            "name",
            "parameters",
            "returnType",
            "visibility",
            "path"
        )
    }

    private fun generatedCompanionFieldName(element: Element): String {
        return element.simpleName.toString().capitalize()
    }

    private fun generatedFieldName(element: Element): String {
        return element.simpleName.toString().let {
            if (KPROPERTY_PATH_FIELDS.contains(it)) {
                "${it}_"
            } else {
                it
            }
        }
    }

}