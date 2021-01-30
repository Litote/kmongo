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

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.BsonField
import com.mongodb.client.model.BucketAutoOptions
import com.mongodb.client.model.BucketOptions
import com.mongodb.client.model.Facet
import com.mongodb.client.model.Field
import com.mongodb.client.model.Filters
import com.mongodb.client.model.GraphLookupOptions
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UnwindOptions
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Variable
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil.encodeValue
import java.time.temporal.TemporalAccessor
import kotlin.reflect.KProperty

/**
 * Creates an $addFields pipeline stage
 *
 * @param fields        the fields to add
 * @return the $addFields pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/addFields/ $addFields
 */
fun addFields(vararg fields: Field<*>): Bson = Aggregates.addFields(*fields)

/**
 * Creates an $addFields pipeline stage
 *
 * @param fields        the fields to add
 * @return the $addFields pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/addFields/ $addFields
 */
fun addFields(fields: List<Field<*>>): Bson = Aggregates.addFields(fields)

/**
 * Creates a $bucket pipeline stage
 *
 * @param <TExpression> the groupBy expression type
 * @param <Boundary>    the boundary type
 * @param groupBy       the criteria to group By
 * @param boundaries    the boundaries of the buckets
 * @param options       the optional values for the $bucket stage
 * @return the $bucket pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/bucket/ $bucket
 */
fun <TExpression, Boundary> bucket(
    groupBy: TExpression,
    boundaries: List<Boundary>,
    options: BucketOptions = BucketOptions()
): Bson =
    Aggregates.bucket(groupBy, boundaries, options)

/**
 * Creates a $bucketAuto pipeline stage
 *
 * @param <TExpression> the groupBy expression type
 * @param groupBy       the criteria to group By
 * @param buckets       the number of the buckets
 * @param options       the optional values for the $bucketAuto stage
 * @return the $bucketAuto pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/bucketAuto/ $bucketAuto
 */
fun <TExpression> bucketAuto(
    groupBy: TExpression,
    buckets: Int,
    options: BucketAutoOptions = BucketAutoOptions()
): Bson = Aggregates.bucketAuto(groupBy, buckets, options)

/**
 * Creates a $count pipeline stage using the named field to store the result
 *
 * @param field the field in which to store the count
 * @return the $count pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/count/ $count
 */
fun <T> KProperty<T>.count(): Bson = Aggregates.count(path())

/**
 * Creates a $match pipeline stage for the specified filter
 *
 * @param filter the filter to match
 * @return the $match pipeline stage
 * @see Filters
 *
 * @mongodb.driver.manual reference/operator/aggregation/match/ $match
 */
fun match(filter: Bson): Bson = Aggregates.match(filter)

/**
 * Creates a $match pipeline stage with $and on each filter.
 * @param getProjection the getProjection
 * @return the $project pipeline stage
 */
fun match(vararg filters: Bson): Bson {
    return Aggregates.match(and(*filters))
}

/**
 * Produce a bson document with the specified elements.
 *
 * @param elements the element of the document (key:value)
 * @return the document as Bson
 */
fun document(vararg elements: Bson?): Bson = document(elements.filterNotNull())

/**
 * Produce a bson document with the specified elements.
 *
 * @param elements the element of the document (key:value)
 * @return the document as Bson
 */
fun document(elements: Collection<Bson>): Bson =
    if (elements.isEmpty()) EMPTY_BSON else combineFilters(Updates::combine, elements)

/**
 * Creates a $project pipeline stage for the specified getProjection
 *
 * @param projection the getProjection
 * @return the $project pipeline stage
 * @see Projections
 *
 * @mongodb.driver.manual reference/operator/aggregation/project/ $project
 */
fun project(projection: Bson): Bson = Aggregates.project(projection)

/**
 * Creates a $project pipeline stage for all specified properties
 *
 * @param properties the properties to project
 * @return the $project pipeline stage
 * @see Projections
 *
 * @mongodb.driver.manual reference/operator/aggregation/project/ $project
 */
fun project(vararg properties: KProperty<*>): Bson {
    return Aggregates.project(document(properties.map { it from true }))
}

/**
 * Creates a $project pipeline stage for all specified properties
 *
 * @param properties the properties to project
 * @return the $project pipeline stage
 * @see Projections
 *
 * @mongodb.driver.manual reference/operator/aggregation/project/ $project
 */
fun project(vararg properties: Pair<KProperty<*>, Any?>): Bson {
    return Aggregates.project(document(properties.map { it.first from it.second }))
}

/**
 * Creates a $project pipeline stage for all specified properties
 *
 * @param projections the properties to project
 * @return the $project pipeline stage
 * @see Projections
 *
 * @mongodb.driver.manual reference/operator/aggregation/project/ $project
 */
fun project(vararg projections: Bson): Bson {
    return Aggregates.project(document(*projections))
}

/**
 * Creates a $project pipeline stage for all specified properties
 *
 * @param properties the properties to project
 * @return the $project pipeline stage
 * @see Projections
 *
 * @mongodb.driver.manual reference/operator/aggregation/project/ $project
 */
fun project(properties: Map<out KProperty<*>, Any?>): Bson {
    return Aggregates.project(document(properties.map { it.key from it.value }))
}

/**
 * Creates a $sort pipeline stage for the specified sort specification
 *
 * @param sort the sort specification
 * @return the $sort pipeline stage
 * @see Sorts
 *
 * @mongodb.driver.manual reference/operator/aggregation/sort/#sort-aggregation $sort
 */
fun sort(sort: Bson): Bson = Aggregates.sort(sort)

/**
 * Creates a $sortByCount pipeline stage for the specified filter
 *
 * @param <TExpression> the expression type
 * @param filter        the filter specification
 * @return the $sortByCount pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/sortByCount/ $sortByCount
 */
fun <TExpression> sortByCount(filter: TExpression): Bson = Aggregates.sortByCount(filter)

/**
 * Creates a $skip pipeline stage
 *
 * @param skip the number of documents to skip
 * @return the $skip pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/skip/ $skip
 */
fun skip(skip: Int): Bson = Aggregates.skip(skip)

/**
 * Creates a $limit pipeline stage for the specified filter
 *
 * @param limit the limit
 * @return the $limit pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/limit/  $limit
 */
fun limit(limit: Int): Bson = Aggregates.limit(limit)

/**
 * Creates a $lookup pipeline stage for the specified filter
 *
 * @param from         the name of the collection in the same database to perform the join with.
 * @param localField   specifies the field from the local collection to match values against.
 * @param foreignField specifies the field in the from collection to match values against.
 * @param newAs           the name of the new array field to add to the input documents.
 * @return the $lookup pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/lookup/ $lookup
 */
fun lookup(from: String, localField: String, foreignField: String, newAs: String): Bson =
    Aggregates.lookup(from, localField, foreignField, newAs)

/**
 * Creates a facet pipeline stage
 *
 * @param facets the facets to use
 * @return the new pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/facet/ $facet
 */
fun facet(facets: List<Facet>): Bson = Aggregates.facet(facets)

/**
 * Creates a facet pipeline stage
 *
 * @param facets the facets to use
 * @return the new pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/facet/ $facet
 */
fun facet(vararg facets: Facet): Bson = Aggregates.facet(*facets)

/**
 * Creates a graphLookup pipeline stage for the specified filter
 *
 * @param <TExpression>    the expression type
 * @param from             the collection to query
 * @param startWith        the expression to start the graph lookup with
 * @param connectFromField the from field
 * @param connectToField   the to field
 * @param fieldAs               name of field in output document
 * @param options          optional values for the graphLookup
 * @return the $graphLookup pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/graphLookup/ $graphLookup
 */
fun <TExpression> graphLookup(
    from: String,
    startWith: TExpression,
    connectFromField: String,
    connectToField: String,
    fieldAs: String,
    options: GraphLookupOptions = GraphLookupOptions()
): Bson = Aggregates.graphLookup(from, startWith, connectFromField, connectToField, fieldAs, options)

/**
 * Creates a $group pipeline stage for the specified filter
 *
 * @param <TExpression>     the expression type
 * @param id                the id expression for the group
 * @param fieldAccumulators zero or more field accumulator pairs
 * @return the $group pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/group/ $group
 * @mongodb.driver.manual meta/aggregation-quick-reference/#aggregation-expressions Expressions
 */
fun <TExpression> group(id: TExpression, vararg fieldAccumulators: BsonField): Bson =
    Aggregates.group(id, *fieldAccumulators)

/**
 * Creates a $group pipeline stage for the specified filter
 *
 * @param <TExpression>     the expression type
 * @param id                the id expression for the group
 * @param fieldAccumulators zero or more field accumulator pairs
 * @return the $group pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/group/ $group
 * @mongodb.driver.manual meta/aggregation-quick-reference/#aggregation-expressions Expressions
 */
fun <TExpression> group(id: TExpression, fieldAccumulators: List<BsonField>): Bson =
    Aggregates.group(id, fieldAccumulators)

/**
 * Creates a $unwind pipeline stage for the specified field name, which must be prefixed by a `'$'` sign.
 *
 * @param fieldName     the field name, prefixed by a `'$' sign`
 * @param unwindOptions options for the unwind pipeline stage
 * @return the $unwind pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/unwind/ $unwind
 */
fun unwind(fieldName: String, unwindOptions: UnwindOptions = UnwindOptions()): Bson =
    Aggregates.unwind(fieldName, unwindOptions)

/**
 * Creates a $unwind pipeline stage for the specified field name, which must be prefixed by a `'$'` sign.
 *
 * @param fieldName     the field name, prefixed by a `'$' sign`
 * @param unwindOptions options for the unwind pipeline stage
 * @return the $unwind pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/unwind/ $unwind
 */
fun <T> KProperty<T>.unwind(unwindOptions: UnwindOptions = UnwindOptions()): Bson =
    unwind(projection, unwindOptions)

/**
 * Creates a $out pipeline stage for the specified filter
 *
 * @param collectionName the collection name
 * @return the $out pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/out/  $out
 */
fun out(collectionName: String): Bson = Aggregates.out(collectionName)

/**
 * Creates a $replaceRoot pipeline stage
 *
 * @param <TExpression> the new root type
 * @param value         the new root value
 * @return the $replaceRoot pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/replaceRoot/ $replaceRoot
 */
fun <TExpression> replaceRoot(value: TExpression): Bson = Aggregates.replaceRoot(value)

/**
 * Creates a $sample pipeline stage with the specified sample size
 *
 * @param size the sample size
 * @return the $sample pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/sample/  $sample
 */
fun sample(size: Int): Bson = Aggregates.sample(size)

private class CondExpression<BooleanExpression : Any, ThenExpression : Any, ElseExpression : Any>(
    val booleanExpression: BooleanExpression?,
    val thenExpression: ThenExpression?,
    val elseExpression: ElseExpression?
) : Bson {

    override fun <TDocument> toBsonDocument(
        documentClass: Class<TDocument>,
        codecRegistry: CodecRegistry
    ): BsonDocument {
        val writer = BsonDocumentWriter(BsonDocument())

        writer.writeStartDocument()
        writer.writeName("\$cond")
        writer.writeStartArray()
        encodeValue(writer, booleanExpression, codecRegistry)
        encodeValue(writer, thenExpression, codecRegistry)
        encodeValue(writer, elseExpression, codecRegistry)
        writer.writeEndArray()
        writer.writeEndDocument()

        return writer.document
    }
}

/**
 * Returns a $cond expression.
 *
 * @param booleanExpression the boolean expression
 * @param thenExpression the then expression
 * @param elseExpression the else expression
 */
fun <BooleanExpression : Any, ThenExpression : Any, ElseExpression : Any> cond(
    booleanExpression: BooleanExpression,
    thenExpression: ThenExpression,
    elseExpression: ElseExpression
): Bson = CondExpression(booleanExpression, thenExpression, elseExpression)

/**
 * Builds $dayOfYear expression for this property .
 */
fun dayOfYear(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.dayOfYear.from(property)

/**
 * Builds $dayOfMonth expression for this property .
 */
fun dayOfMonth(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.dayOfMonth.from(property)

/**
 * Builds $dayOfWeek expression for this property .
 */
fun dayOfWeek(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.dayOfWeek.from(property)

/**
 * Builds $year expression for this property .
 */
fun year(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.year.from(property)

/**
 * Builds $hour expression for this property .
 */
fun hour(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.hour.from(property)

/**
 * Builds $millisecond expression for this property .
 */
fun millisecond(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.millisecond.from(property)

/**
 * Builds $minute expression for this property .
 */
fun minute(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.minute.from(property)

/**
 * Builds $month expression for this property .
 */
fun month(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.month.from(property)

/**
 * Builds $second expression for this property .
 */
fun second(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.second.from(property)

/**
 * Builds $week expression for this property .
 */
fun week(property: KProperty<TemporalAccessor?>): Bson = MongoOperator.week.from(property)

/**
 * Creates a $lookup pipeline stage, joining the current collection with the one specified in from using the given pipeline
 *
 * @param <TExpression> the Variable value expression type
 * @param from           the name of the collection in the same database to perform the join with.
 * @param let            the variables to use in the pipeline field stages.
 * @param resultProperty the name of the new array field to add to the input documents.
 * @param pipeline       the pipeline to run on the joined collection.
 * @return the $lookup pipeline stage
 * @mongodb.driver.manual reference/operator/aggregation/lookup/ $lookup
 * @mongodb.server.release 3.6
 * @since 3.7
 */
fun lookup(
    from: String,
    let: List<Variable<out Any>>? = null,
    resultProperty: KProperty<Any?>,
    vararg pipeline: Bson
): Bson =
    @Suppress("UNCHECKED_CAST")
    Aggregates.lookup(from, let as? List<Variable<Any>>, pipeline.toList(), resultProperty.path())

/**
 * Defines a [Variable] for the lookup operator.
 */
fun KProperty<*>.variableDefinition(name: String = path()): Variable<String> = Variable(name, projection)

/**
 * Defines a [Variable] projection (ie $$name)
 */
val KProperty<*>.variable: String get() = path().variable

/**
 * Defines a [Variable] projection (ie $$"myString"")
 */
val String.variable: String get() = "\$\$$this"

