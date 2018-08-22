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

import com.squareup.kotlinpoet.AnnotationSpec
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
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
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

        dataClasses.forEach {
            registerSuperclasses(it, dataClasses)
        }
        dataClasses.forEach {
            process(it, dataClasses)
        }
        return dataClasses.isNotEmpty()
    }

    private fun registerSuperclasses(element: AnnotatedClass, dataClasses: AnnotatedClassSet) {
        val superclass = element.superclass
        if (superclass is DeclaredType && superclass.toString() != "java.lang.Object") {
            a.debug { "$element : $superclass - ${superclass.getAnnotation(Data::class.java)}" }
            val superElement = superclass.asElement() as TypeElement
            if (!dataClasses.contains(superElement)) {
                val sE = AnnotatedClass(superElement, element.internal, a)
                dataClasses.add(sE)
                registerSuperclasses(sE, dataClasses)
                process(sE, dataClasses)
            }
        }
    }

    private fun collectionSuperclass(element: AnnotatedClass): TypeName {
        val className = generatedClassName(element)
        val sourceClassName = element.asClassName()
        val typeName = ClassName.bestGuess(className)
        val superMirrorClass = element.superclass
        return if (superMirrorClass is DeclaredType && superMirrorClass.toString() != "java.lang.Object") {
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
                KCollectionPropertyPath::class.asClassName(),
                TypeVariableName("T"),
                sourceClassName.asNullable(),
                ParameterizedTypeName.get(
                    typeName,
                    TypeVariableName("T")
                )
            )
        }
    }

    private fun collectionClassBuilder(element: AnnotatedClass): TypeSpec.Builder {
        val collectionSuperclass = collectionSuperclass(element)
        val className = generatedClassName(element)
        val sourceClassName = element.asClassName()
        val typeName = ClassName.bestGuess(className)
        val colClass = ClassName.bestGuess("${className}Col")
        return TypeSpec.classBuilder(colClass)
            .addTypeVariable(TypeVariableName("T"))
            .apply {
                if (element.internal) {
                    addModifiers(KModifier.INTERNAL)
                }
            }
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
                                sourceClassName.javaToKotlinType()
                            ).asNullable()
                        )
                    )
                    .build()
            )
            .superclass(collectionSuperclass)
            .addSuperclassConstructorParameter("%1L,%2L", "previous", "property")
            .addFunction(
                FunSpec.builder("memberWithAdditionalPath")
                    .addParameter("additionalPath", String::class.asClassName())
                    .returns(
                        ParameterizedTypeName.get(
                            typeName,
                            TypeVariableName("T")
                        )
                    )
                    .addModifiers(KModifier.OVERRIDE)
                    .addAnnotation(
                        AnnotationSpec.builder(Suppress::class).addMember("\"UNCHECKED_CAST\"").build()
                    )
                    .addCode(
                        "return %1T(this, customProperty(this, additionalPath))",
                        typeName
                    )
                    .build()
            )
    }

    private fun mapSuperclass(element: AnnotatedClass): TypeName {
        val className = generatedClassName(element)
        val sourceClassName = element.asClassName()
        val typeName = ClassName.bestGuess(className)
        val superMirrorClass = element.superclass
        return if (superMirrorClass is DeclaredType && superMirrorClass.toString() != "java.lang.Object") {
            val superElement = superMirrorClass.asElement() as TypeElement
            ParameterizedTypeName.get(
                ClassName(
                    a.processingEnv.elementUtils.getPackageOf(superElement).qualifiedName.toString(),
                    "${generatedClassName(superElement)}Map"
                ),
                TypeVariableName("T"),
                TypeVariableName("K")
            )
        } else {
            ParameterizedTypeName.get(
                KMapPropertyPath::class.asClassName(),
                TypeVariableName("T"),
                TypeVariableName("K"),
                sourceClassName.asNullable(),
                ParameterizedTypeName.get(
                    typeName,
                    TypeVariableName("T")
                )
            )
        }
    }

    private fun mapClassBuilder(element: AnnotatedClass): TypeSpec.Builder {
        val mapSuperclass = mapSuperclass(element)
        val className = generatedClassName(element)
        val sourceClassName = element.asClassName()
        val typeName = ClassName.bestGuess(className)
        val mapClass = ClassName.bestGuess("${className}Map")
        return TypeSpec.classBuilder(mapClass)
            .addTypeVariable(TypeVariableName("T"))
            .addTypeVariable(TypeVariableName("K"))
            .apply {
                if (element.internal) {
                    addModifiers(KModifier.INTERNAL)
                }
            }
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
                                ClassName("kotlin.collections", "Map"),
                                TypeVariableName("K"),
                                sourceClassName.javaToKotlinType()
                            ).asNullable()
                        )
                    )
                    .build()
            )
            .superclass(mapSuperclass)
            .addSuperclassConstructorParameter("%1L,%2L", "previous", "property")
            .addFunction(
                FunSpec.builder("memberWithAdditionalPath")
                    .addParameter("additionalPath", String::class.asClassName())
                    .returns(
                        ParameterizedTypeName.get(
                            typeName,
                            TypeVariableName("T")
                        )
                    )
                    .addModifiers(KModifier.OVERRIDE)
                    .addAnnotation(
                        AnnotationSpec.builder(Suppress::class).addMember("\"UNCHECKED_CAST\"").build()
                    )
                    .addCode(
                        "return %1T(this, customProperty(this, additionalPath))",
                        typeName
                    )
                    .build()
            )
    }

    private fun process(element: AnnotatedClass, dataClasses: AnnotatedClassSet) {
        val sourceClassName = element.asClassName()
        val className = generatedClassName(element)
        val fileBuilder = FileSpec.builder(element.getPackage(), className)

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
            .apply {
                if (element.internal) {
                    addModifiers(KModifier.INTERNAL)
                }
            }
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

        val collectionClassBuilder = collectionClassBuilder(element)
        val mapClassBuilder = mapClassBuilder(element)

        if (!element.modifiers.contains(Modifier.FINAL)) {
            classBuilder.addModifiers(KModifier.OPEN)
            collectionClassBuilder.addModifiers(KModifier.OPEN)
            mapClassBuilder.addModifiers(KModifier.OPEN)
        }

        val companionObject = TypeSpec.companionObjectBuilder()

        element.properties().forEach { e ->
            a.debug { "${e.simpleName}-${e.asType()}" }
            val type = e.asType()
            val returnType = a.processingEnv.typeUtils.asElement(type) as? TypeElement
            if (type != null) {
                a.debug { "$type-annot: ${type.getAnnotation(Data::class.java)}" }
            }
            val (collection, annotatedCollection) = type.run {
                a.debug { this is DeclaredType }
                if (this is ArrayType) {
                    a.debug { componentType }
                    a.debug { a.processingEnv.typeUtils.asElement(componentType) }
                    false to dataClasses.contains(a.processingEnv.typeUtils.asElement(componentType))
                } else if (this is DeclaredType
                    && a.processingEnv.typeUtils.isAssignable(
                        a.processingEnv.typeUtils.erasure(this),
                        a.processingEnv.elementUtils.getTypeElement("java.util.Collection").asType()
                    )
                ) {
                    true to
                            (typeArguments.firstOrNull()?.run {
                                val asElement = a.processingEnv.typeUtils.asElement(this)
                                a.debug { asElement.javaClass }
                                dataClasses.contains(asElement).also {
                                    a.debug { it }
                                }
                            } == true)
                } else {
                    false to false
                }
            }
            val (map, annotatedMap) = type.run {
                a.debug { this is DeclaredType }
                if (this is DeclaredType
                    && a.processingEnv.typeUtils.isAssignable(
                        a.processingEnv.typeUtils.erasure(this),
                        a.processingEnv.elementUtils.getTypeElement("java.util.Map").asType()
                    )
                ) {
                    true to
                            (typeArguments.getOrNull(1)?.run {
                                val asElement = a.processingEnv.typeUtils.asElement(this)
                                a.debug { asElement.javaClass }
                                dataClasses.contains(asElement).also {
                                    a.debug { it }
                                }
                            } == true)
                } else {
                    false to false
                }
            }
            val annotated = returnType?.let { dataClasses.contains(it) } ?: false || annotatedCollection || annotatedMap
            val propertyType = e.javaToKotlinType()
            val packageOfReturnType =
                if (returnType == null) ""
                else if (annotatedCollection) a.enclosedCollectionPackage(type)
                else if (annotatedMap) a.enclosedValueMapPackage(type)
                else a.processingEnv.elementUtils.getPackageOf(returnType).qualifiedName.toString()

            fun propertyClass(start:Boolean): TypeName {
                val sourceClass = if(start) sourceClassName else  TypeVariableName("T")
                return if (annotated) {
                    ParameterizedTypeName.get(
                        ClassName(
                            packageOfReturnType,
                            a.generatedClassProperty(type, annotatedCollection, annotatedMap)
                        ),
                        *listOfNotNull(sourceClass, a.mapKeyClass(type, annotatedMap)).toTypedArray()
                    )
                } else {
                    if (collection) {
                        ParameterizedTypeName.get(
                            KCollectionSimplePropertyPath::class.asClassName(),
                            sourceClass,
                            a.firstTypeArgument(e).asNullable()
                        )
                    } else if (map) {
                        ParameterizedTypeName.get(
                            KMapSimplePropertyPath::class.asClassName(),
                            sourceClass,
                            a.firstTypeArgument(e).asNullable(),
                            a.secondTypeArgument(e).asNullable()
                        )
                    } else {
                        ParameterizedTypeName.get(
                            if(start) KProperty1::class.asClassName() else KPropertyPath::class.asClassName(),
                            sourceClass,
                            propertyType.asNullable()
                        )
                    }
                }
            }

            fun propertyReference(start: Boolean) =
                element.propertyReference(
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
                ) {
                    val pRef = CodeBlock.builder().add("%1T::%2L", sourceClassName, e.simpleName).build()
                    if (start && !annotated && collection) {
                        CodeBlock.builder()
                            .add(
                                "%1T(%2L, %3L)",
                                KCollectionSimplePropertyPath::class.asClassName(),
                                if(start) "null" else "this",
                                pRef
                            ).build()
                    } else if (start && !annotated && map) {
                        CodeBlock.builder().add(
                            "%1T(%2L, %3L)",
                            KMapSimplePropertyPath::class.asClassName(),
                            if(start) "null" else "this",
                            pRef)
                            .build()
                    } else {
                        pRef
                    }
                }


            //add companion property
            companionObject.addProperty(
                PropertySpec
                    .varBuilder(generatedCompanionFieldName(e), propertyClass(true))
                    .mutable(false)
                    .getter(
                        FunSpec.getterBuilder().apply {
                            if (annotated) {
                                addCode("return %1T(null,%2L)", propertyClass(true), propertyReference(true))
                            } else {
                                addCode("return %1L", propertyReference(true))
                            }
                        }.build()
                    )
                    .build()
            )

            val classPropertyClass: TypeName =  propertyClass(false)
            val property =  PropertySpec
                .varBuilder(generatedFieldName(e), classPropertyClass)
                .mutable(false)
                .getter(
                    FunSpec.getterBuilder().apply {
                        addCode(
                            "return %1L(this,%2L)\n",
                            if (annotated) {
                                a.generatedClassProperty(type, annotatedCollection, annotatedMap)
                            } else {
                                classPropertyClass
                            },
                            propertyReference(false)
                        )
                    }.build()
                )
                .build()

            //add class property
            classBuilder.addProperty(property)
            collectionClassBuilder.addProperty(property)
            mapClassBuilder.addProperty(property)
        }

        //add classes
        fileBuilder.addType(
            classBuilder
                .companionObject(companionObject.build())
                .build()
        )
        fileBuilder.addType(collectionClassBuilder.build())
        fileBuilder.addType(mapClassBuilder.build())

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