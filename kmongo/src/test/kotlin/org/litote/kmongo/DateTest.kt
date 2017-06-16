/*
 * Copyright (C) 2016 Litote
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

import org.bson.Document
import org.junit.Test
import org.litote.kmongo.DateTest.DateValue
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class DateTest : KMongoBaseTest<DateValue>() {

    data class DateValue(val date: Date?, val calendar: Calendar?,
                         val localDateTime: LocalDateTime?, val localDate: LocalDate?, val localTime: LocalTime?,
                         var zonedDateTime: ZonedDateTime?, var offsetDateTime: OffsetDateTime?, var offsetTime: OffsetTime?,
                         val instant: Instant?) {

        constructor() : this(UTC)

        constructor(offset: ZoneOffset) : this(Date(), Calendar.getInstance(), LocalDateTime.now(), LocalDate.now(), LocalTime.now(),
                ZonedDateTime.now(offset), OffsetDateTime.now(offset), OffsetTime.now(offset), Instant.now())
    }

    override fun getDefaultCollectionClass(): KClass<DateValue> {
        return DateValue::class
    }

    @Test
    fun testInsertAndLoad() {
        val value = DateValue()
        col.insertOne(value)
        val loadedValue = col.findOne()

        assertEquals(value, loadedValue)
    }

    @Test
    fun testGMT1InsertAndLoad() {
        val offset = ZoneOffset.of("+1")
        val value = DateValue(offset)
        col.insertOne(value)
        val loadedValue = col.findOne()!!
        loadedValue.offsetDateTime = loadedValue.offsetDateTime!!.withOffsetSameInstant(offset)
        loadedValue.offsetTime = loadedValue.offsetTime!!.withOffsetSameInstant(offset)
        loadedValue.zonedDateTime = loadedValue.zonedDateTime!!.withZoneSameInstant(offset)

        assertEquals(value, loadedValue)
    }

    @Test
    fun testInsertJsonAndLoad() {
        val value = DateValue()
        col.insertOne(value.json)
        val loadedValue = col.findOne()

        assertEquals(value, loadedValue)
    }

    @Test
    fun testNullAndLoad() {
        val value = DateValue(null, null, null, null, null, null, null, null, null)
        col.insertOne(value.json)
        val loadedValue = col.findOne()
        assertEquals(value, loadedValue)
    }

    @Test
    fun testUTCDateStorageInMongoWithUTCCurrentTimeZone() {
        testUTCDateStorage(0, "GMT", "00:00:00")
    }

    @Test
    fun testUTCDateStorageInMongoWithGMT1CurrentTimeZone() {
        testUTCDateStorage(1, "GMT+1:00", "01:00:00")
    }

    @Test
    fun testUTCDateStorageInMongoWithGMTminus1CurrentTimeZone() {
        testUTCDateStorage(-1, "GMT-1:00", "23:00:00")
    }

    private fun testUTCDateStorage(modifier: Int, timezone: String, timeDate: String) {
        val defaultTimezone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone(timezone))
        try {
            val value = DateValue()
            col.insertOne(value)
            val loadedValue = col.withDocumentClass<Document>().findOne()!!
            val date = loadedValue.get("date") as Date
            val dateToString = date.toString()
            assertEquals(dateToString, loadedValue.get("calendar").toString())
            assertEquals(dateToString, loadedValue.get("zonedDateTime").toString())
            assertEquals(dateToString, loadedValue.get("offsetDateTime").toString())
            assertEquals(dateToString, loadedValue.get("instant").toString())
            assertEquals(dateToString.substring(11, 19), loadedValue.get("offsetTime").toString().substring(11, 19))

            assertTrue(loadedValue.get("localDate").toString().contains(timeDate))

            val localDateTime = loadedValue.get("localDateTime") as Date
            assertEquals(date.hours, hour(localDateTime.hours - modifier))
            assertEquals(dateToString.substring(13, 19), localDateTime.toString().substring(13, 19))

            val localTime = loadedValue.get("localTime") as Date
            assertEquals(date.hours, hour(localTime.hours - modifier))
            assertEquals(dateToString.substring(13, 19), localTime.toString().substring(13, 19))
        } finally {
            TimeZone.setDefault(defaultTimezone)
        }
    }

    private fun hour(hour: Int): Int = if (hour < 0) 24 + hour else if (hour > 23) hour - 24 else hour
}