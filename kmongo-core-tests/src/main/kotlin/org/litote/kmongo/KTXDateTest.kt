package org.litote.kmongo

import org.litote.kmongo.KTXDateTest.KTXDateValue
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class KTXDateTest : AllCategoriesKMongoBaseTest<KTXDateValue>() {

    @Serializable
    data class KTXDateValue(
        @Contextual
        val localDateTime: LocalDateTime?,
        @Contextual
        val localDate: LocalDate?,
        @Contextual
        val localTime: LocalTime?,
        @Contextual
        val instant: Instant?
    ) {

        constructor() : this(TimeZone.UTC)

        constructor(offset: FixedOffsetTimeZone, instant: Instant = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())) : this(
            instant.toLocalDateTime(offset),
            instant.toLocalDateTime(offset).date,
            instant.toLocalDateTime(offset).time,
            instant
        )
    }

    @Test
    fun testInsertAndLoad() {
        val value = KTXDateValue()
        col.insertOne(value)
        val loadedValue = col.findOne()

        assertEquals(value, loadedValue)
    }

    @Test
    fun testGMT1InsertAndLoad() {
        val offset = FixedOffsetTimeZone(UtcOffset.parse("+01"))
        val value = KTXDateValue(offset)
        col.insertOne(value)
        val loadedValue = col.findOne()!!

        assertEquals(value, loadedValue)
    }

    @Test
    fun testInsertJsonAndLoad() {
        val value = KTXDateValue()
        col.insertOne(value.json)
        val loadedValue = col.findOne()
        assertEquals(value, loadedValue)
    }

    @Test
    fun testInsertAndLoadByValue() {
        val value = KTXDateValue()
        col.insertOne(value)
        assertEquals(value, col.findOne(KTXDateValue::instant eq value.instant))
        assertEquals(value, col.findOne(KTXDateValue::localDate eq value.localDate))
        assertEquals(value, col.findOne(KTXDateValue::localDateTime eq value.localDateTime))
        assertEquals(value, col.findOne(KTXDateValue::localTime eq value.localTime))
    }

    @Test
    fun testNullAndLoad() {
        val value = KTXDateValue(null, null, null, null)
        col.insertOne(value.json)
        val loadedValue = col.findOne()

        assertEquals(value, loadedValue)
    }

    @Test
    fun testDateQuery() {
        val value = KTXDateValue()
        col.insertOne(value)
        val date = value.localDateTime!!
        val one = col.findOne(
            """{"localDateTime":{${MongoOperator.gte}: ${date.date.json}, ${MongoOperator.lt}: ${date.date.plus(
                1,
                DateTimeUnit.DAY
            ).json} } }"""
        )

        assertNotNull(one)
    }

    @Test
    fun testTypedDateQueryQuery() {
        val value = KTXDateValue()
        col.insertOne(value)
        val date = value.instant!!
        val one = col.findOne(KTXDateValue::instant gte date)

        assertNotNull(one)
    }
}