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

import org.bson.codecs.pojo.annotations.BsonId
import org.junit.Test
import org.litote.kmongo.MongoOperator.date
import org.litote.kmongo.MongoOperator.set
import java.time.Instant
import kotlin.test.assertEquals

/**
 *
 */
class MappingTest {

    data class Project(val reports: List<ProjectReport>)
    data class ProjectReport(@BsonId val key: String, val points: List<ProjectReportPoint>)
    data class ProjectReportPoint(@BsonId val key: String, val published: Instant)

    data class ReportProjection(val report: ProjectReport)
    data class PointProjection(val point: ProjectReportPoint)

    @Test
    fun testFindAndUpdate() {
        val now = Instant.now()
        assertEquals(
            """{"$set": {"reports.${'$'}[report].points.${'$'}[point].published": {"$date": "$now"}}}""",
            setValue(
                (Project::reports.filteredPosOp("report") / ProjectReport::points)
                    .filteredPosOp("point") / ProjectReportPoint::published,
                now
            ).json
        )

        assertEquals(
            listOf("""{"report._id": "a"}""", """{"point._id": "a"}"""),
            listOf(
                ReportProjection::report / ProjectReport::key eq "a",
                PointProjection::point / ProjectReportPoint::key eq "a",
            ).map { it.json }
        )


    }
}