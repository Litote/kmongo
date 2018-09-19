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
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

//see https://github.com/square/kotlinpoet/issues/236
internal fun Element.javaToKotlinType(): TypeName =
    asType().asTypeName().javaToKotlinType()

fun Element.asTypeName(): TypeName {
    val annotation = this.getAnnotation(Nullable::class.java)
    val typeName = this.asType().asTypeName()
    return if (annotation != null) typeName.asNullable() else typeName
}

internal fun TypeName.javaToKotlinType(): TypeName {
    return when (this) {
        is ParameterizedTypeName -> {
            val raw = rawType.javaToKotlinType() as ClassName
            if (raw.toString() == "kotlin.Array" && typeArguments.firstOrNull()?.javaToKotlinType().toString() == "kotlin.Byte") {
                ClassName.bestGuess("kotlin.ByteArray")
            } else {
                ParameterizedTypeName.get(
                    raw,
                    *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
                )
            }
        }
        is WildcardTypeName -> {
            if (upperBounds.isNotEmpty()) {
                WildcardTypeName.subtypeOf(upperBounds.first().javaToKotlinType())
            } else {
                WildcardTypeName.supertypeOf(lowerBounds.first().javaToKotlinType())
            }
        }
        else -> {
            //System.err.println(toString())
            //System.err.println(javaClass)
            val className = JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))
                ?.asSingleFqName()?.asString()
            //System.err.println(className.toString())
            if (className == null) {
                this
            } else {
                ClassName.bestGuess(className)
            }
        }
    }
}

internal val notSupportedModifiers = setOf(Modifier.STATIC, Modifier.TRANSIENT)

/**
 *
 */
internal class KMongoAnnotations(val processingEnv: ProcessingEnvironment) {

    private val debug: Boolean = "true" == System.getProperty("org.litote.kmongo.processor.debug")

    fun log(value: Any?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, value.toString())
    }

    fun debug(f: () -> Any?) {
        if (debug) {
            log(f())
        }
    }

    fun logError(t: Throwable) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, t.stackTrace())
    }

    inline fun <reified T : Annotation, reified R : Annotation> getAnnotatedClasses(roundEnv: RoundEnvironment): AnnotatedClassSet {
        val dataElements = roundEnv.getElementsAnnotatedWith(T::class.java)
            .map { AnnotatedClass(it as TypeElement, hasInternalModifier<T>(it), this) }
        val registryElements = getRegistryClasses<R>(roundEnv)

        debug { registryElements }
        debug { dataElements }

        return AnnotatedClassSet(
            (dataElements + registryElements)
                .toMutableSet()
        ).apply {
            if (isNotEmpty()) {
                log("Found ${T::class.simpleName} classes: $this")
            }
        }
    }

    private inline fun <reified T : Annotation> hasInternalModifier(element: Element): Boolean {
        return element
            .annotationMirrors
            .first { it.annotationType.toString() == T::class.qualifiedName }
            .elementValues
            .run {
                keys.find { it.simpleName.toString() == "internal" }
                    ?.let { get(it)?.value as? Boolean }
            } ?: false
    }

    private inline fun <reified T : Annotation> getRegistryClasses(roundEnv: RoundEnvironment): Set<AnnotatedClass> =
        roundEnv
            .getElementsAnnotatedWith(T::class.java)
            .map { element ->
                element.annotationMirrors
                    .first { it.annotationType.toString() == T::class.qualifiedName }
            }
            .flatMap { a ->
                val internal = a.elementValues[a.elementValues.keys.find {
                    it.simpleName.toString() == "internal"
                }]?.value as? Boolean ?: false

                a.elementValues[a.elementValues.keys.first {
                    it.simpleName.toString() == "value"
                }]
                    .let { v ->
                        @Suppress("UNCHECKED_CAST")
                        (v!!.value as Iterable<AnnotationValue>).map {
                            AnnotatedClass(
                                processingEnv.typeUtils.asElement(it.value as TypeMirror) as TypeElement,
                                internal,
                                this
                            )
                        }
                    }
            }
            .toSet()


    fun enclosedCollectionPackage(type: TypeMirror): String =
        processingEnv.elementUtils.getPackageOf(
            if (type is ArrayType) {
                processingEnv.typeUtils.asElement(type.componentType)
            } else {
                processingEnv.typeUtils.asElement((type as DeclaredType).typeArguments.first())
            }
        ).qualifiedName.toString()

    fun enclosedValueMapPackage(type: TypeMirror): String =
        processingEnv.elementUtils.getPackageOf(
            processingEnv.typeUtils.asElement((type as DeclaredType).typeArguments[1])
        ).qualifiedName.toString()

    fun firstTypeArgument(element: Element): TypeName =
        (element.javaToKotlinType() as ParameterizedTypeName).typeArguments.first()

    fun secondTypeArgument(element: Element): TypeName =
        (element.javaToKotlinType() as ParameterizedTypeName).typeArguments[1]

    fun generatedClassProperty(type: TypeMirror, annotatedCollection: Boolean, annotatedMap: Boolean = false): String =
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

    fun mapKeyClass(type: TypeMirror, annotatedMap: Boolean): TypeName? =
        if (annotatedMap) (processingEnv.typeUtils.asElement((type as DeclaredType).typeArguments[0]) as TypeElement).asTypeName()
        else null

    fun Throwable.stackTrace(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        printStackTrace(pw)
        return sw.toString()
    }

    fun writeFile(fileBuilder: FileSpec.Builder) {
        try {
            val kotlinFile = fileBuilder.build()
            debug {
                processingEnv.filer.getResource(
                    StandardLocation.SOURCE_OUTPUT,
                    kotlinFile.packageName,
                    kotlinFile.name
                ).name
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
        } catch (e: Exception) {
            logError(e)
        }
    }

    fun writeFile(outputDirectory: String, file: String, content: String) {
        try {
            val directory = Paths.get(
                processingEnv.filer.getResource(
                    StandardLocation.SOURCE_OUTPUT,
                    "",
                    outputDirectory
                ).toUri()
            )
            Files.createDirectories(directory)
            val outputPath = directory.resolve(file)
            OutputStreamWriter(Files.newOutputStream(outputPath), StandardCharsets.UTF_8).use { writer ->
                writer.append(
                    content
                )
            }
        } catch (e: Exception) {
            logError(e)
        }
    }

    fun findByProperty(sourceClassName: TypeName, targetElement: TypeName, propertyName: String): CodeBlock =
        CodeBlock.builder().add(
            "org.litote.kmongo.property.findProperty<%1T,%2T>(%3S)",
            sourceClassName,
            targetElement,
            propertyName
        ).build()

    fun findPropertyValue(
        sourceClassName: TypeName,
        targetElement: TypeName,
        owner: String,
        propertyName: String
    ): CodeBlock =
        CodeBlock.builder().add(
            "org.litote.kmongo.property.findPropertyValue<%1T,%2T>(%3L, %4S)",
            sourceClassName,
            targetElement,
            owner,
            propertyName
        ).build()

    fun setPropertyValue(
        sourceClassName: TypeName,
        targetElement: TypeName,
        owner: String,
        propertyName: String,
        newValue: String
    ): CodeBlock =
        CodeBlock.builder().add(
            "org.litote.kmongo.property.setPropertyValue<%1T,%2T>(%3L, %4S, %5L)",
            sourceClassName,
            targetElement,
            owner,
            propertyName,
            newValue
        ).build()

}