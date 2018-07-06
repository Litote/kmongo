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
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.platform.JavaToKotlinClassMap

//see https://github.com/square/kotlinpoet/issues/236
internal fun Element.javaToKotlinType(): TypeName =
    asType().asTypeName().javaToKotlinType()

fun Element.asTypeName(): TypeName {
    val annotation = this.getAnnotation(Nullable::class.java)
    val typeName = this.asType().asTypeName()
    return if (annotation != null) typeName.asNullable() else typeName
}

internal fun TypeName.javaToKotlinType(): TypeName {
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

internal val notSupportedModifiers = setOf(Modifier.STATIC, Modifier.TRANSIENT)

/**
 *
 */
internal class KMongoAnnotations(val processingEnv: ProcessingEnvironment) {

    val debug: Boolean = "true" == System.getProperty("org.litote.kmongo.processor.debug")

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

    inline fun <reified T : Annotation, reified R : Annotation> getAnnotatedClasses(roundEnv: RoundEnvironment): MutableSet<Element> {
        val dataElements = roundEnv.getElementsAnnotatedWith(T::class.java)
        val registryElements = getRegistryClasses<R>(roundEnv)

        debug { registryElements }
        debug { dataElements }

        return (dataElements + registryElements).toMutableSet().apply {
            if (isNotEmpty()) {
                log("Found ${T::class.simpleName} classes: $this")
            }
        }
    }

    inline fun <reified T : Annotation> getRegistryClasses(roundEnv: RoundEnvironment) =
        roundEnv
            .getElementsAnnotatedWith(T::class.java)
            .map {
                it.annotationMirrors
                    .first { it.annotationType.toString() == T::class.qualifiedName }
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


    fun enclosedCollectionPackage(type: TypeMirror): String =
        processingEnv.elementUtils.getPackageOf(
            if (type is ArrayType) {
                processingEnv.typeUtils.asElement(type.componentType)
            } else {
                processingEnv.typeUtils.asElement((type as DeclaredType).typeArguments.first())
            }
        ).qualifiedName.toString()

    fun generatedClassProperty(type: TypeMirror, annotatedCollection: Boolean): String =
        if (annotatedCollection) {
            (if (type is ArrayType) {
                (processingEnv.typeUtils.asElement(type.componentType) as TypeElement).simpleName
            } else {
                (processingEnv.typeUtils.asElement((type as DeclaredType).typeArguments.first()) as TypeElement).simpleName
            }).let { "${it}_Col" }
        } else {
            "${processingEnv.typeUtils.asElement(type).simpleName}_"
        }

    fun Throwable.stackTrace(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        printStackTrace(pw)
        return sw.toString()
    }

    fun writeFile(fileBuilder: FileSpec.Builder) {
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
    }

    fun writeFile(outputDirectory:String, file : String, content:String) {
        val directory = Paths.get(
            processingEnv.filer.getResource(
                StandardLocation.SOURCE_OUTPUT,
                "",
                outputDirectory
            ).toUri()
        )
        Files.createDirectories(             directory               )
        val outputPath = directory.resolve(file)
        OutputStreamWriter(Files.newOutputStream(outputPath), StandardCharsets.UTF_8).use { writer -> writer.append(content) }
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

    fun propertyReference(
        classElement: TypeElement,
        property: Element,
        privateHandler: () -> CodeBlock,
        nonPrivateHandler: () -> CodeBlock
    ): CodeBlock {
        return if (classElement
                .enclosedElements
                .firstOrNull { it.simpleName.toString() == "get${property.simpleName.toString().capitalize()}" }
                ?.modifiers
                ?.contains(Modifier.PRIVATE) != false
        ) {
            privateHandler()
        } else {
            nonPrivateHandler()
        }
    }

    fun getPackage(element: Element): String =
        processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()

    fun properties(element: TypeElement): List<VariableElement> =
        element.enclosedElements
            .filterIsInstance<VariableElement>()
            .filter { e -> e.modifiers.none { notSupportedModifiers.contains(it) } }
}