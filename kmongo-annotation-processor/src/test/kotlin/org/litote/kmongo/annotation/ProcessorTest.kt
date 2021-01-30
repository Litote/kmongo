/*
 * Copyright (C) 2016/2021 Litote
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

package org.litote.kmongo.annotation


import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import org.litote.kmongo.KMongoAnnotationProcessor
import java.nio.file.Paths


/**
 * The real tests are the ones that deal with the generated classes
 * Here we test more the base process.
 */
class ProcessorTest {

    @Test
    fun testAnnotationProcessor() {
        val baseDirectory =
            Paths.get(ProcessorTest::class.java.classLoader.getResource("META-INF/services/javax.annotation.processing.Processor").toURI())
                .parent.parent.parent.parent
        val kaptStubsDirectory = baseDirectory
            .resolve("kaptStubs")
            .resolve("test")
            .resolve("org")
            .resolve("litote")
            .resolve("kmongo")
            .resolve("model")
        val javaFiles = (kaptStubsDirectory.toFile().listFiles().filter { it.toString().endsWith(".java") } +
                kaptStubsDirectory.resolve("other").toFile().listFiles().filter { it.toString().endsWith(".java") })
            .map { JavaFileObjects.forResource(it.toURI().toURL()) }
        val compilation: Compilation =
            javac()
                .withProcessors(KMongoAnnotationProcessor())
                .compile(javaFiles)
        //assertThat(compilation).succeededWithoutWarnings()
    }

}