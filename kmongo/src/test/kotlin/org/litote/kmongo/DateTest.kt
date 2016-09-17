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
                         val zonedDateTime: ZonedDateTime?, val offsetDateTime: OffsetDateTime?, var offsetTime: OffsetTime?,
                         val instant: Instant?) {
        constructor() : this(Date(), Calendar.getInstance(), LocalDateTime.now(), LocalDate.now(), LocalTime.now(),
                ZonedDateTime.now(), OffsetDateTime.now(), OffsetTime.now(), Instant.now())
    }

    override fun getDefaultCollectionClass(): KClass<DateValue> {
        return DateValue::class
    }

    @Test
    fun testInsertAndLoad() {
        val value = DateValue()
        col.insertOne(value)
        val loadedValue = col.findOne()
        //set correct offset (now != 1970 !)
        loadedValue!!.offsetTime = loadedValue.offsetTime!!.withOffsetSameInstant(value.offsetTime!!.offset)

        assertEquals(value, loadedValue)
    }

    @Test
    fun testInsertJsonAndLoad() {
        val value = DateValue()
        col.insertOne(value.json)
        val loadedValue = col.findOne()
        //set correct offset (now != 1970 !)
        loadedValue!!.offsetTime = loadedValue.offsetTime!!.withOffsetSameInstant(value.offsetTime!!.offset)

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
    fun testDateStorageInMongo() {
        val defaultTimezone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
        try {
            val value = DateValue()
            col.insertOne(value)
            val loadedValue = col.withDocumentClass<Document>().findOne()!!
            val date = loadedValue.get("date").toString()
            assertEquals(date, loadedValue.get("calendar").toString())
            assertEquals(date, loadedValue.get("localDateTime").toString())
            assertEquals(date, loadedValue.get("zonedDateTime").toString())
            assertEquals(date, loadedValue.get("offsetDateTime").toString())
            assertEquals(date, loadedValue.get("instant").toString())

            assertEquals(date.substring(0, 11), loadedValue.get("localDate").toString().substring(0, 11))
            assertTrue(loadedValue.get("localDate").toString().contains("00:00:00"))

            assertEquals(date.substring(11, 19), loadedValue.get("localTime").toString().substring(11, 19))
            assertEquals(date.substring(11, 19), loadedValue.get("offsetTime").toString().substring(11, 19))
        } finally {
            TimeZone.setDefault(defaultTimezone)
        }
    }
}