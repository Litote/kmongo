/*
 * Copyright (C) 2016/2022 Litote
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

package org.litote.kmongo.issue

import com.mongodb.assertions.Assertions.assertTrue
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.bson.BsonDocument
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.document
import org.litote.kmongo.rem
import org.litote.kmongo.setValue
import kotlin.test.Test
import kotlin.test.assertEquals

sealed class Shape(
    val type: String
) {
    data class Circle(
        val radius: Int
    ) : Shape("Circle")

    data class Square(
        val side: Int
    ) : Shape("Square")
}

data class Box(
    val shape: Shape,
    val color: String
)

/**
 *
 */
class Issue276SubclassTypedQueryTest {

    @Test
    fun subclassQuery() {
        val bson2 = setValue(Box::shape % Shape.Circle::radius, 0)
        assertEquals(
            BsonDocument.parse("""{"$set": {"shape.radius": 0}}"""),
            bson2.document
        )
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun checkDivDoesNotCompile() {
        val kotlinSource = SourceFile.kotlin(
            "Test.kt", """
        import org.litote.kmongo.div
        import org.litote.kmongo.eq

        sealed class Shape(val type: String) {
        data class Circle(val radius: Int) : Shape("Circle")
        data class Square(val side: Int) : Shape("Square")
        }

        data class Box(
                val shape: Shape,
                val color: String
            )

        val bson = Box::color / Shape::type eq "Circle"
    """
        )
        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Cannot infer type for this parameter. Specify it explicitly."))
    }
}
