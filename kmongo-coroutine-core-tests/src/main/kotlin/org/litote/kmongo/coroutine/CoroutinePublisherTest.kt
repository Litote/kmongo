package org.litote.kmongo.coroutine

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.CoreCategory
import org.reactivestreams.Publisher

@Category(CoreCategory::class)
class CoroutinePublisherTest {

    private val publisher: Publisher<Int> = DoSinglePublisher(1)
    private val coroutine = publisher.coroutine

    @Test
    fun toList() {
        runBlocking {
            assertEquals(listOf(1), coroutine.toList())
        }
    }

    @Test
    fun toFlow() {
        runBlocking {
            assertEquals(listOf(1), coroutine.toFlow().toList())
        }
    }
}
