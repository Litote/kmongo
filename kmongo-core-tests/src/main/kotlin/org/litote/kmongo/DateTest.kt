/*
 * Copyright (C) 2016/2020 Litote
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

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.DateTest.DateValue
import java.time.Instant
import java.time.Instant.now
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 *
 */
class DateTest : AllCategoriesKMongoBaseTest<DateValue>() {

    @Serializable
    data class DateValue(
        @ContextualSerialization
        val date: Date?,
        @ContextualSerialization
        val calendar: Calendar?,
        @ContextualSerialization
        val localDateTime: LocalDateTime?,
        @ContextualSerialization
        val localDate: LocalDate?,
        @ContextualSerialization
        val localTime: LocalTime?,
        @ContextualSerialization
        var zonedDateTime: ZonedDateTime?,
        @ContextualSerialization
        var offsetDateTime: OffsetDateTime?,
        @ContextualSerialization
        var offsetTime: OffsetTime?,
        @ContextualSerialization
        val instant: Instant?
    ) {

        constructor() : this(UTC)

        constructor(offset: ZoneOffset, instant: Instant = now()) : this(
            Date.from(instant),
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = Date.from(instant) },
            LocalDateTime.ofInstant(instant, offset).truncatedTo(ChronoUnit.MILLIS),
            LocalDateTime.ofInstant(instant, offset).toLocalDate(),
            LocalDateTime.ofInstant(instant, offset).toLocalTime().truncatedTo(ChronoUnit.MILLIS),
            ZonedDateTime.ofInstant(instant, offset).truncatedTo(ChronoUnit.MILLIS),
            OffsetDateTime.ofInstant(instant, offset).truncatedTo(ChronoUnit.MILLIS),
            OffsetTime.ofInstant(instant, offset).truncatedTo(ChronoUnit.MILLIS),
            instant.truncatedTo(ChronoUnit.MILLIS)
        )
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
    fun testInsertAndLoadByValue() {
        val value = DateValue()
        col.insertOne(value)
        assertEquals(value, col.findOne(value::instant eq value.instant))
        assertEquals(value, col.findOne(value::date eq value.date))
        assertEquals(value, col.findOne(value::calendar eq value.calendar))
        assertEquals(value, col.findOne(value::localDate eq value.localDate))
        assertEquals(value, col.findOne(value::localDateTime eq value.localDateTime))
        assertEquals(value, col.findOne(value::localTime eq value.localTime))
        assertEquals(value, col.findOne(value::offsetDateTime eq value.offsetDateTime))
        assertEquals(value, col.findOne(value::offsetTime eq value.offsetTime))
        assertEquals(value, col.findOne(value::zonedDateTime eq value.zonedDateTime))
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
        testUTCDateStorage("GMT", "00:00:00")
    }

    @Test
    fun testUTCDateStorageInMongoWithGMT1CurrentTimeZone() {
        testUTCDateStorage("GMT+1:00", "01:00:00")
    }

    @Test
    fun testUTCDateStorageInMongoWithGMTminus1CurrentTimeZone() {
        testUTCDateStorage("GMT-1:00", "23:00:00")
    }

    private fun testUTCDateStorage(timezone: String, timeDate: String) {
        val defaultTimezone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone(timezone))
        try {
            val value = DateValue()
            col.insertOne(value)
            assertNotNull(col.findOne())
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
            val calendarDateTime = Calendar.getInstance()
            calendarDateTime.time = localDateTime

            val calendar = Calendar.getInstance()
            calendar.time = date

            assertEquals(
                calendar.get(Calendar.HOUR_OF_DAY),
                hour(calendarDateTime.get(Calendar.HOUR_OF_DAY))
            )
            assertEquals(dateToString.substring(13, 19), localDateTime.toString().substring(13, 19))

            val localTime = loadedValue.get("localTime") as Date
            val calendarTime = Calendar.getInstance()
            calendarTime.time = localTime
            assertEquals(calendar.get(Calendar.HOUR_OF_DAY), hour(calendarTime.get(Calendar.HOUR_OF_DAY)))
            assertEquals(dateToString.substring(13, 19), localTime.toString().substring(13, 19))
        } finally {
            TimeZone.setDefault(defaultTimezone)
        }
    }

    private fun hour(hour: Int): Int = if (hour < 0) 24 + hour else if (hour > 23) hour - 24 else hour
}