package org.litote.kmongo.coroutine

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.CoreCategory
import org.reactivestreams.Publisher

@Category(CoreCategory::class)
class CoroutinePublisherTest {

    val publisher: Publisher<*> = mock()
    val coroutine = publisher.coroutine

    @Test
    fun toList() {
        runBlocking {
            val expected = listOf(1)
            whenever(publisher.toList()).thenReturn(expected)
            assertEquals(expected, coroutine.toList())
            verify(publisher).toList()
        }
    }

    @Test
    fun toFlow() {
        runBlocking {
            val expected = listOf(1).asFlow()
            whenever(publisher.asFlow()).thenReturn(expected)
            assertEquals(expected.toList(), coroutine.toFlow().toList())
            verify(publisher).asFlow()
        }
    }
}
