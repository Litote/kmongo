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
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.platform.JavaToKotlinClassMap

/**
 * TODO check internal private protected on class -> kotlin metadata
 * TODO support nullable generic -> kotlin metadata
 * TODO java9 support
 * TODO map support
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

    private val notSupportedModifiers = setOf(Modifier.STATIC, Modifier.TRANSIENT)

    private val debug: Boolean = "true" == System.getProperty("org.litote.kmongo.processor.debug")

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (debug) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                annotations.toString()
            )
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                processingEnv.options.toString()
            )
        }

        return try {
            processDataClasses(roundEnv) && processJacksonDataClasses(roundEnv) && processNativeDataClasses(roundEnv)
        } catch (e: Throwable) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.stackTrace())
            false
        }
    }

    private fun processJacksonDataClasses(roundEnv: RoundEnvironment): Boolean {
        val classes = getAnnotatedClasses<JacksonData, JacksonDataRegistry>(roundEnv)

        return classes.isNotEmpty()
    }

    private fun processNativeDataClasses(roundEnv: RoundEnvironment): Boolean {
        val classes = getAnnotatedClasses<NativeData, NativeDataRegistry>(roundEnv)

        return classes.isNotEmpty()
    }

    private fun processDataClasses(roundEnv: RoundEnvironment): Boolean {
        val dataClasses = getAnnotatedClasses<Data, DataRegistry>(roundEnv)

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

    private inline fun <reified T : Annotation, reified R : Annotation> getAnnotatedClasses(roundEnv: RoundEnvironment): MutableSet<Element> {
        val dataElements = roundEnv.getElementsAnnotatedWith(T::class.java)
        val registryElements = getRegistryClasses<R>(roundEnv)

        if (debug) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                registryElements.toString()
            )

            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                dataElements.toString()
            )
        }

        return (dataElements + registryElements).toMutableSet().apply {
            if (isNotEmpty()) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.NOTE,
                    "Found ${T::class.simpleName} classes: $this"
                )
            }
        }
    }

    private inline fun <reified T : Annotation> getRegistryClasses(roundEnv: RoundEnvironment) =
        roundEnv
            .getElementsAnnotatedWith(T::class.java)
            .map {
                it.annotationMirrors.filter { it.annotationType.toString() == DataRegistry::class.qualifiedName }
                    .first()
            }
            .flatMap {
                it.elementValues.values.flatMap {
                    @Suppress("UNCHECKED_CAST")
                    (it.value as Iterable<AnnotationValue>).map {
                        processingEnv.typeUtils.asElement(it.value as TypeMirror)
                    }
                }
            }
            .toSet()

    override fun getSupportedSourceVersion(): SourceVersion {
        if (debug) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                SourceVersion.latest().toString()
            )
        }
        return SourceVersion.latest()
    }

    private fun registerSuperclasses(element: TypeElement, dataClasses: MutableSet<Element>) {
        val superclass = element.superclass
        if (superclass is DeclaredType && superclass.toString() != "java.lang.Object") {
            if (debug) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.NOTE,
                    "$element : $superclass - ${superclass.getAnnotation(Data::class.java)}"
                )
            }
            val superElement = superclass.asElement() as TypeElement
            if (!dataClasses.contains(superElement)) {
                dataClasses.add(superElement)
                registerSuperclasses(superElement, dataClasses)
                process(superElement, dataClasses)
            }
        }
    }

    private fun process(element: TypeElement, dataClasses: MutableSet<Element>) {
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

        val collectionSuperclass: TypeName =
            if (superMirrorClass is DeclaredType && superMirrorClass.toString() != "java.lang.Object") {
                val superElement = superMirrorClass.asElement() as TypeElement
                ParameterizedTypeName.get(
                    ClassName(
                        processingEnv.elementUtils.getPackageOf(superElement).qualifiedName.toString(),
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


        if (!element.modifiers.contains(FINAL)) {
            classBuilder.addModifiers(KModifier.OPEN)
            collectionClassBuilder.addModifiers(KModifier.OPEN)
        }

        val companionObject = TypeSpec.companionObjectBuilder()

        for (e in element.enclosedElements) {
            if (e is VariableElement && e.modifiers.none { notSupportedModifiers.contains(it) }) {
                if (debug) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.NOTE,
                        "${e.simpleName}-${e.asType()}"
                    )
                }
                val type = e.asType()
                val returnType = processingEnv.typeUtils.asElement(type) as? TypeElement
                if (type != null) {
                    if (debug) {
                        processingEnv.messager.printMessage(
                            Diagnostic.Kind.NOTE,
                            "$type-annot: ${type.getAnnotation(Data::class.java)}"
                        )
                    }
                }
                val annotatedCollection = type.run {
                    if (this is ArrayType) {
                        dataClasses.contains(processingEnv.typeUtils.asElement(componentType))
                    } else if (this is DeclaredType
                        && processingEnv.typeUtils.isAssignable(
                            processingEnv.typeUtils.erasure(this),
                            processingEnv.elementUtils.getTypeElement("java.util.Collection").asType()
                        )
                    ) {
                        typeArguments.firstOrNull()?.run {
                            dataClasses.contains(processingEnv.typeUtils.asElement(this))
                        } == true
                    } else {
                        false
                    }
                }
                val annotated = returnType?.let { dataClasses.contains(it) } ?: false || annotatedCollection
                val propertyType = e.javaToKotlinType()
                val packageOfReturnType =
                    if (returnType == null) ""
                    else if (annotatedCollection) enclosedCollectionPackage(type)
                    else processingEnv.elementUtils.getPackageOf(returnType).qualifiedName.toString()

                val companionPropertyClass: TypeName =
                    if (annotated) {
                        ParameterizedTypeName.get(
                            ClassName(
                                packageOfReturnType,
                                generatedClassProperty(type, annotatedCollection)
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
                            .firstOrNull { it.simpleName.toString() == "get${e.simpleName.toString().capitalize()}" }
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
                                generatedClassProperty(type, annotatedCollection)
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
                                        generatedClassProperty(type, annotatedCollection)
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
                                        generatedClassProperty(type, annotatedCollection)
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

        //add classes
        fileBuilder.addType(
            classBuilder
                .companionObject(companionObject.build())
                .build()
        )
        fileBuilder.addType(collectionClassBuilder.build())

        val kotlinFile = fileBuilder.build()
        if (debug) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                processingEnv.filer.getResource(
                    StandardLocation.SOURCE_OUTPUT,
                    kotlinFile.packageName,
                    kotlinFile.name
                ).name
            )
        }

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
    private fun Element.javaToKotlinType(): TypeName =
        asType().asTypeName().javaToKotlinType()

    private fun TypeName.javaToKotlinType(): TypeName {
        return if (this is ParameterizedTypeName) {
            ParameterizedTypeName.get(
                rawType.javaToKotlinType() as ClassName,
                *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
            )
        } else {
            val className =
                JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))
                    ?.asSingleFqName()?.asString()

            return if (className == null) {
                this
            } else {
                ClassName.bestGuess(className)
            }
        }
    }

    private fun generatedClassName(element: Element): String =
        "${element.simpleName}_"

    private fun enclosedCollectionPackage(type: TypeMirror): String =
        processingEnv.elementUtils.getPackageOf(
            if (type is ArrayType) {
                processingEnv.typeUtils.asElement(type.componentType)
            } else {
                processingEnv.typeUtils.asElement((type as DeclaredType).typeArguments.first())
            }
        ).qualifiedName.toString()

    private fun generatedClassProperty(type: TypeMirror, annotatedCollection: Boolean): String =
        if (annotatedCollection) {
            (if (type is ArrayType) {
                (processingEnv.typeUtils.asElement(type.componentType) as TypeElement).simpleName
            } else {
                (processingEnv.typeUtils.asElement((type as DeclaredType).typeArguments.first()) as TypeElement).simpleName
            }).let { "${it}_Col" }
        } else {
            "${processingEnv.typeUtils.asElement(type).simpleName}_"
        }

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

    private fun Throwable.stackTrace(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        printStackTrace(pw)
        return sw.toString()
    }

}