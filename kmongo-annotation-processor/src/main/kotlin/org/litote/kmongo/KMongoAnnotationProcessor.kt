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
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OPEN
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import org.litote.kgenerator.AnnotatedClass
import org.litote.kgenerator.AnnotatedClassSet
import org.litote.kgenerator.KGenerator
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KProperty1

/**
 * TODO check internal private protected on class -> kotlin metadata
 * TODO support nullable generic -> kotlin metadata
 */
@SupportedAnnotationTypes(
    "org.litote.kmongo.Data",
    "org.litote.kmongo.DataRegistry"
)
class KMongoAnnotationProcessor : KGenerator() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        debug { annotations }
        debug { processingEnv.options }

        return try {
            processDataClasses(roundEnv)
        } catch (e: Throwable) {
            error(e)
            false
        }
    }

    private fun generatedClassProperty(
        type: TypeMirror,
        annotatedCollection: Boolean,
        annotatedMap: Boolean = false
    ): String =
        if (annotatedCollection) {
            (if (type is ArrayType) {
                (processingEnv.typeUtils.asElement(type.componentType) as TypeElement).simpleName
            } else {
                (processingEnv.typeUtils.asElement((type as DeclaredType).typeArguments.first()) as TypeElement).simpleName
            }).let { "${it}_Col" }
        } else if (annotatedMap) {
            (processingEnv.typeUtils.asElement((type as DeclaredType).typeArguments[1]) as TypeElement).simpleName
                .let { "${it}_Map" }
        } else {
            "${processingEnv.typeUtils.asElement(type).simpleName}_"
        }

    private fun processDataClasses(roundEnv: RoundEnvironment): Boolean {
        val dataClasses = getAnnotatedClasses<Data, DataRegistry>(roundEnv)

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
            debug { "$element : $superclass - ${superclass.getAnnotation(Data::class.java)}" }
            val superElement = superclass.asElement() as TypeElement
            if (!dataClasses.contains(superElement)) {
                val sE = AnnotatedClass(this, superElement, element.internal)
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
            ClassName(
                env.elementUtils.getPackageOf(superElement).qualifiedName.toString(),
                "${generatedClassName(superElement)}Col"
            ).parameterizedBy(TypeVariableName("T"))
        } else {
            KCollectionPropertyPath::class.asClassName()
                .parameterizedBy(
                    TypeVariableName("T"),
                    sourceClassName.copy(nullable = true),
                    typeName.parameterizedBy(TypeVariableName("T"))
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
                    addModifiers(INTERNAL)
                }
            }
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter(
                        "previous",
                        KPropertyPath::class.asClassName().parameterizedBy(
                            TypeVariableName("T"),
                            TypeVariableName("*")
                        ).copy(nullable = true)
                    )
                    .addParameter(
                        "property",
                        KProperty1::class.asClassName().parameterizedBy(
                            TypeVariableName("*"),
                            ClassName("kotlin.collections", "Collection").parameterizedBy(
                                javaToKotlinType(sourceClassName)
                            ).copy(nullable = true)
                        )
                    )
                    .build()
            )
            .superclass(collectionSuperclass)
            .addSuperclassConstructorParameter("%L,%L", "previous", "property")
            .addFunction(
                FunSpec.builder("memberWithAdditionalPath")
                    .addParameter("additionalPath", String::class.asClassName())
                    .returns(typeName.parameterizedBy(TypeVariableName("T")))
                    .addModifiers(OVERRIDE)
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
            ClassName(
                env.elementUtils.getPackageOf(superElement).qualifiedName.toString(),
                "${generatedClassName(superElement)}Map"
            ).parameterizedBy(
                TypeVariableName("T"),
                TypeVariableName("K")
            )
        } else {
            KMapPropertyPath::class.asClassName().parameterizedBy(
                TypeVariableName("T"),
                TypeVariableName("K"),
                sourceClassName.copy(nullable = true),
                typeName.parameterizedBy(TypeVariableName("T"))
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
                    addModifiers(INTERNAL)
                }
            }
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter(
                        "previous",
                        KPropertyPath::class.asClassName().parameterizedBy(
                            TypeVariableName("T"),
                            TypeVariableName("*")
                        ).copy(nullable = true)
                    )
                    .addParameter(
                        "property",
                        KProperty1::class.asClassName().parameterizedBy(
                            TypeVariableName("*"),
                            ClassName("kotlin.collections", "Map").parameterizedBy(
                                TypeVariableName("K"),
                                javaToKotlinType(sourceClassName)
                            ).copy(nullable = true)
                        )
                    )
                    .build()
            )
            .superclass(mapSuperclass)
            .addSuperclassConstructorParameter("%L,%L", "previous", "property")
            .addFunction(
                FunSpec.builder("memberWithAdditionalPath")
                    .addParameter("additionalPath", String::class.asClassName())
                    .returns(typeName.parameterizedBy(TypeVariableName("T")))
                    .addModifiers(OVERRIDE)
                    .addAnnotation(
                        AnnotationSpec.builder(Suppress::class).addMember("\"UNCHECKED_CAST\"").build()
                    )
                    .addCode(
                        "return %T(this, customProperty(this, additionalPath))",
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
                ClassName(
                    env.elementUtils.getPackageOf(superElement).qualifiedName.toString(),
                    generatedClassName(superElement)
                ).parameterizedBy(TypeVariableName("T"))
            } else {
                KPropertyPath::class.asClassName().parameterizedBy(
                    TypeVariableName("T"),
                    sourceClassName.copy(nullable = true)
                )
            }

        val classBuilder = TypeSpec.classBuilder(className)
            .addTypeVariable(TypeVariableName("T"))
            .apply {
                if (element.internal) {
                    addModifiers(INTERNAL)
                }
            }
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter(
                        "previous",
                        KPropertyPath::class.asClassName().parameterizedBy(
                            TypeVariableName("T"),
                            TypeVariableName("*")
                        ).copy(nullable = true)
                    )
                    .addParameter(
                        "property",
                        KProperty1::class.asClassName().parameterizedBy(
                            TypeVariableName("*"),
                            sourceClassName.copy(nullable = true)
                        )
                    )
                    .build()
            )
            .superclass(superclass)
            .addSuperclassConstructorParameter("%L,%L", "previous", "property")

        val collectionClassBuilder = collectionClassBuilder(element)
        val mapClassBuilder = mapClassBuilder(element)

        if (!element.modifiers.contains(Modifier.FINAL)) {
            classBuilder.addModifiers(OPEN)
            collectionClassBuilder.addModifiers(OPEN)
            mapClassBuilder.addModifiers(OPEN)
        }

        val companionObject = TypeSpec.companionObjectBuilder()

        element.properties().forEach { e ->
            val type = e.asType()
            debug { "${e.simpleName}-$type-annot: ${type?.getAnnotation(Data::class.java)}" }
            val returnType = env.typeUtils.asElement(type) as? TypeElement
            val (collection, annotatedCollection) = type.run {
                debug { this is DeclaredType }
                if (this is ArrayType) {
                    debug { componentType }
                    debug { env.typeUtils.asElement(componentType) }
                    false to dataClasses.contains(env.typeUtils.asElement(componentType))
                } else if (this is DeclaredType && e.isCollection) {
                    true to
                            (e.typeArgumentElement()?.run {
                                debug { javaClass }
                                dataClasses.contains(this)
                            } == true)
                } else {
                    false to false
                }
            }
            val (map, annotatedMap) = type.run {
                debug { this is DeclaredType }
                if (this is DeclaredType && e.isMap) {
                    true to
                            (e.typeArgumentElement(1)?.run {
                                debug { javaClass }
                                dataClasses.contains(this)
                            } == true)
                } else {
                    false to false
                }
            }
            val annotated = returnType?.let { dataClasses.contains(it) } ?: false || annotatedCollection || annotatedMap
            val propertyType = javaToKotlinType(e)
            val packageOfReturnType =
                if (returnType == null) ""
                else if (annotatedCollection) enclosedCollectionPackage(type)
                else if (annotatedMap) enclosedValueMapPackage(type)
                else env.elementUtils.getPackageOf(returnType).qualifiedName.toString()

            fun propertyClass(start: Boolean): TypeName {
                val sourceClass = if (start) sourceClassName else TypeVariableName("T")
                return if (annotated) {
                    ClassName(
                        packageOfReturnType,
                        generatedClassProperty(type, annotatedCollection, annotatedMap)
                    ).parameterizedBy(
                        *listOfNotNull(sourceClass, mapKeyClass(type, annotatedMap)).toTypedArray()
                    )
                } else {
                    if (collection) {
                        KCollectionSimplePropertyPath::class.asClassName().parameterizedBy(
                            sourceClass,
                            e.typeArgument()!!.copy(nullable = true)
                        )
                    } else if (map) {
                        KMapSimplePropertyPath::class.asClassName().parameterizedBy(
                            sourceClass,
                            e.typeArgument()!!.copy(nullable = true),
                            e.typeArgument(1)!!.copy(nullable = true)
                        )
                    } else {
                        (if (start) KProperty1::class.asClassName() else KPropertyPath::class.asClassName()).parameterizedBy(
                            sourceClass,
                            propertyType.copy(nullable = true)
                        )
                    }
                }
            }

            fun propertyReference(start: Boolean, ref: String? = null, onlyRef: Boolean = false) =
                if (ref == null) {
                    element.propertyReference(
                        e,
                        {
                            findByProperty(
                                sourceClassName,
                                if (annotated) {
                                    ClassName(
                                        packageOfReturnType,
                                        returnType!!.simpleName.toString()
                                    )
                                } else {
                                    propertyType.copy(nullable = true)
                                },
                                e.simpleName.toString()
                            )
                        }
                    ) {
                        val pRef = CodeBlock.builder().add("%T::%L", sourceClassName, e.simpleName).build()
                        if (!onlyRef && start && !annotated && collection) {
                            CodeBlock.builder()
                                .add(
                                    "%T(%L, %L)",
                                    KCollectionSimplePropertyPath::class.asClassName(),
                                    if (start) "null" else "this",
                                    pRef
                                ).build()
                        } else if (!onlyRef && start && !annotated && map) {
                            CodeBlock.builder().add(
                                "%T(%L, %L)",
                                KMapSimplePropertyPath::class.asClassName(),
                                if (start) "null" else "this",
                                pRef
                            )
                                .build()
                        } else {
                            pRef
                        }
                    }
                } else {
                    if (start && !annotated && collection) {
                        CodeBlock.builder()
                            .add(
                                "%T(%L, %L)",
                                KCollectionSimplePropertyPath::class.asClassName(),
                                if (start) "null" else "this",
                                ref
                            ).build()
                    } else if (start && !annotated && map) {
                        CodeBlock.builder().add(
                            "%T(%L, %L)",
                            KMapSimplePropertyPath::class.asClassName(),
                            if (start) "null" else "this",
                            ref
                        )
                            .build()
                    } else {
                        CodeBlock.of(ref)
                    }
                }

            val propertyDef = "__" + generatedCompanionFieldName(e)
            //add property def
            fileBuilder.addProperty(
                PropertySpec
                    .builder(
                        propertyDef,
                        KProperty1::class.asClassName().parameterizedBy(
                            sourceClassName,
                            propertyType.copy(nullable = true)
                        )
                    )
                    .addModifiers(PRIVATE)
                    .mutable(false)
                    .getter(
                        FunSpec.getterBuilder().apply {
                            addCode("return %L", propertyReference(true, onlyRef = true))
                        }
                            .build()
                    )
                    .build()
            )

            //add companion property
            companionObject.addProperty(
                PropertySpec
                    .builder(generatedCompanionFieldName(e), propertyClass(true))
                    .mutable(false)
                    .getter(
                        FunSpec.getterBuilder().apply {
                            if (annotated) {
                                addCode(
                                    "return %T(null,%L)",
                                    propertyClass(true),
                                    propertyReference(true, propertyDef)
                                )
                            } else {
                                addCode("return %L", propertyReference(true, propertyDef))
                            }
                        }.build()
                    )
                    .build()
            )

            val classPropertyClass: TypeName = propertyClass(false)
            val property = PropertySpec
                .builder(generatedFieldName(e), classPropertyClass)
                .mutable(false)
                .getter(
                    FunSpec.getterBuilder().apply {
                        addCode(
                            "return ${if (annotated) "%L" else "%T"}(this,%L)\n",
                            if (annotated) {
                                generatedClassProperty(type, annotatedCollection, annotatedMap)
                            } else {
                                classPropertyClass
                            },
                            if (annotated || collection || map) propertyReference(false) else propertyDef
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
                .addType(companionObject.build())
                .build()
        )
        fileBuilder.addType(collectionClassBuilder.build())
        fileBuilder.addType(mapClassBuilder.build())

        writeFile(fileBuilder)
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