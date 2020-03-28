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

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Variable
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.litote.kmongo.AggregateTypedTest.Article
import org.litote.kmongo.MongoOperator.`in`
import org.litote.kmongo.MongoOperator.and
import org.litote.kmongo.MongoOperator.dateToString
import org.litote.kmongo.MongoOperator.eq
import org.litote.kmongo.MongoOperator.gte
import org.litote.kmongo.model.Friend
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class AggregateTypedTest : AllCategoriesKMongoBaseTest<Article>() {

    @Serializable
    data class Article(
        val title: String,
        val author: String,
        val tags: List<String>,
        @ContextualSerialization
        val date: Instant = Instant.now(),
        val count: Int = 1,
        val ok: Boolean = true
    ) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    @Serializable
    private data class Result(
        @SerialName("_id")
        @BsonId val title: String,
        val averageYear: Double = 0.0,
        val count: Int = 0,
        val friends: List<Friend> = emptyList()
    )

    lateinit var friendCol: MongoCollection<Friend>

    @Before
    fun setup() {
        col.insertOne(Article("Zombie Panic", "Kirsty Mckay", "horror", "virus"))
        col.insertOne(Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead"))
        col.insertOne(Article("World War Z", "Max Brooks", "horror", "virus", "pandemic"))

        friendCol = getCollection()
        friendCol.insertOne(Friend("William"))
        friendCol.insertOne(Friend("John"))
        friendCol.insertOne(Friend("Richard"))
    }

    @After
    fun tearDown() {
        dropCollection<Friend>()
    }

    @Test
    fun canAggregate() {
        val l = col.aggregate<Article>(
            match(Article::author eq "Maberry Jonathan")
        ).toList()
        assertEquals(1, l.size)
    }


    @Test
    fun canAggregateWithMultipleDocuments() {
        val l = col.aggregate<Article>(match(Article::tags contains "virus")).toList()
        assertEquals(2, l.size)
        assertTrue(l.all { it.tags.contains("virus") })
    }

    @Test
    fun canAggregateParameters() {
        val tag = "pandemic"
        val l = col.aggregate<Article>(match(Article::tags contains tag)).toList()
        assertEquals(1, l.size)
        assertEquals("World War Z", l.first().title)
    }

    @Test
    fun canAggregateWithManyMatch() {
        val l =
            col.aggregate<Article>(match(Article::tags contains "virus", Article::tags contains "pandemic")).toList()
        assertEquals(1, l.size)
        assertEquals("World War Z", l.first().title)
    }

    //TODO
    @Ignore
    @Test
    fun `can aggregate complex queries and deserialize in object`() {
        val query = listOf(
            match(
                Article::tags contains "virus"
            ),
            project(
                Article::title from Article::title,
                Article::ok from cond(Article::ok, 1, 0),
                Result::averageYear from year(Article::date)
            ),
            group(
                Article::title,
                Result::count sum Article::ok,
                Result::averageYear avg Result::averageYear
            ),
            sort(
                ascending(
                    Result::title
                )
            )
        )

        val r = col.aggregate<Result>(
            match(
                Article::tags contains "virus"
            ),
            project(
                Article::title from Article::title,
                Article::ok from cond(Article::ok, 1, 0),
                Result::averageYear from year(Article::date)
            ),
            group(
                Article::title,
                Result::count sum Article::ok,
                Result::averageYear avg Result::averageYear
            ),
            sort(
                ascending(
                    Result::title
                )
            )
        )
            .toList()

        assertEquals(2, r.size)
        assertEquals(
            Result("World War Z", LocalDate.now().year.toDouble(), 1),
            r.first()
        )
        assertEquals(
            Result("Zombie Panic", LocalDate.now().year.toDouble(), 1),
            r.last()
        )
    }

    @Test
    fun canAggregateWithManyOperators() {
        val l = col.aggregate<Article>(match(Article::tags contains "virus"), limit(1)).toList()
        assertEquals(1, l.size)
    }

    //TODO
    @Ignore
    @Test
    fun shouldPopulateIds() {
        val l0 = friendCol.aggregate<Friend>(
            project(
                Friend::_id from "\$_id",
                Friend::name from Friend::name
            )
        ).toList()
        assertEquals(3, l0.size)
        assertTrue(l0.all { it._id != null })
        assertTrue(l0.all { it.name != null })
        assertTrue(l0.all { it.address == null })

        val l = friendCol.aggregate<Friend>(
            project(
                Friend::_id to "\$_id",
                Friend::name to Friend::name
            )
        ).toList()
        assertEquals(3, l.size)
        assertTrue(l.all { it._id != null })
        assertTrue(l.all { it.name != null })
        assertTrue(l.all { it.address == null })

        val l2 = friendCol.aggregate<Friend>(project(Friend::_id, Friend::name)).toList()
        assertEquals(3, l2.size)
        assertTrue(l2.all { it._id != null })
        assertTrue(l2.all { it.name != null })
        assertTrue(l2.all { it.address == null })

        val l3 = friendCol.aggregate<Friend>(project(Friend::name)).toList()
        assertEquals(3, l3.size)
        assertTrue(l3.all { it._id != null })
        assertTrue(l3.all { it.name != null })
        assertTrue(l3.all { it.address == null })

        val l4 = friendCol.aggregate<Friend>(project(Friend::_id to false, Friend::name to true)).toList()
        assertEquals(3, l4.size)
        assertTrue(l4.all { it._id == null })
        assertTrue(l4.all { it.name != null })
        assertTrue(l4.all { it.address == null })
    }

    @Test
    fun `use push in group`() {
        val r = col.aggregate<Result>(
            match(
                Article::tags contains "virus"
            ),
            group(
                Article::title, Result::friends.push(Friend::name from Article::author)
            ),
            sort(
                ascending(
                    Result::title
                )
            )
        )
            .toList()
        assertEquals(
            2, r.size
        )
        assertEquals("Max Brooks", r[0].friends[0].name)
        assertEquals("Kirsty Mckay", r[1].friends[0].name)
        assertEquals("World War Z", r[0].title)
        assertEquals("Zombie Panic", r[1].title)
    }

    @Serializable
    data class Answer(val evaluator: String, val alreadyUsed: Boolean, @ContextualSerialization val answerDate: Instant)

    @Serializable
    data class EvaluationsForms(val questions: List<String>)

    @Serializable
    data class EvaluationsFormsWithResults(val questions: List<String>, val results: List<EvaluationRequest>)

    @Serializable
    data class EvaluationsAnswers(val questionId: String, val evaluated: String, val answers: List<Answer>)

    @Serializable
    data class EvaluationRequest(val userId: String, val evaluationDate: String)

    @Test
    fun `lookup test`() {
        val ldapId = "id"
        val fromDate = Instant.now()

        val bson1 = Aggregates.lookup(
            "evaluationsAnswers",
            listOf(
                Variable("questions", EvaluationsForms::questions)
            ),
            listOf(
                match(
                    expr(
                        Projections.computed(
                            "\$and", listOf(
                                Projections.computed("\$in", listOf(EvaluationsAnswers::questionId, "\$\$questions")),
                                Projections.computed("\$eq", listOf(EvaluationsAnswers::evaluated, ldapId))
                            )
                        )
                    )
                ),
                unwind("\$answers"),
                match(
                    expr(
                        Projections.computed(
                            "\$and", listOf(
                                Projections.computed(
                                    "\$eq",
                                    listOf(EvaluationsAnswers::answers.div(Answer::alreadyUsed), false)
                                ),
                                Projections.computed(
                                    "\$gte",
                                    listOf(EvaluationsAnswers::answers.div(Answer::answerDate), fromDate)
                                )
                            )
                        )
                    )
                ),
                Aggregates.group(
                    fields(
                        EvaluationRequest::userId from EvaluationsAnswers::answers.div(Answer::evaluator),
                        EvaluationRequest::evaluationDate from Projections.computed(
                            "\$dateToString", fields(
                                Projections.computed("format", "%Y-%m-%d"),
                                Projections.computed("date", EvaluationsAnswers::answers.div(Answer::answerDate))
                            )
                        )
                    )
                ),
                replaceRoot("_id".projection)
            ),
            "results"
        )

        val bson2 = lookup(
            "evaluationsAnswers",
            listOf(EvaluationsForms::questions.variableDefinition()),
            EvaluationsFormsWithResults::results,
            match(
                expr(
                    and from listOf(
                        `in` from listOf(EvaluationsAnswers::questionId, EvaluationsForms::questions.variable),
                        eq from listOf(EvaluationsAnswers::evaluated, ldapId)
                    )
                )
            ),
            EvaluationsAnswers::answers.unwind(),
            match(
                expr(
                    and from listOf(
                        eq from listOf(EvaluationsAnswers::answers / Answer::alreadyUsed, false),
                        gte from listOf(EvaluationsAnswers::answers / Answer::answerDate, fromDate)
                    )
                )
            ),
            group(
                fields(
                    EvaluationRequest::userId from (EvaluationsAnswers::answers / Answer::evaluator),
                    EvaluationRequest::evaluationDate from (
                            dateToString from (
                                    combine(
                                        "format" from "%Y-%m-%d",
                                        "date" from (EvaluationsAnswers::answers / Answer::answerDate)
                                    )
                                    )
                            )
                )
            ),
            replaceRoot("_id".projection)
        )

        assertEquals(bson1.json, bson2.json)

        database.getCollection<EvaluationsAnswers>("evaluationsAnswers").apply {
            insertOne(EvaluationsAnswers("q1", ldapId, listOf(Answer("userId", false, fromDate.plusSeconds(10)))))
        }
        database.getCollection<EvaluationsForms>().apply {
            insertOne(EvaluationsForms(listOf("q1", "q2")))
            val result1 = aggregate<EvaluationsFormsWithResults>(bson1)
            assertEquals(1, result1.toList().size)
            val result2 = aggregate<EvaluationsFormsWithResults>(bson2)
            assertEquals(1, result2.toList().size)
        }

    }
}