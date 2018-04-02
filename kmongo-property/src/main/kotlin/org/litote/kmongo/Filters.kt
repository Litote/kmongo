/*
 * Copyright (C) 2017 Litote
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

import com.mongodb.client.model.Filters
import com.mongodb.client.model.TextSearchOptions
import com.mongodb.client.model.geojson.Geometry
import com.mongodb.client.model.geojson.Point
import org.bson.BsonType
import org.bson.conversions.Bson
import java.util.regex.Pattern
import kotlin.internal.NoInfer
import kotlin.reflect.KProperty

/**
 * Creates a filter that matches all documents where the value of the property equals the specified value. Note that this doesn't
 * actually generate a $eq operator, as the query language doesn't require it.
 *
 * @param value     the value
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/eq $eq
 */
infix fun <T> KProperty<T>.eq(value: @NoInfer T): Bson = Filters.eq<T>(path(), value)

/**
 * Creates a filter that matches all documents where the value of the property contains the specified value. Note that this doesn't
 * actually generate a $eq operator, as the query language doesn't require it.
 *
 * @param value     the value
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/eq/#op._S_eq
 */
infix fun <T> KProperty<Collection<T>?>.contains(value: @NoInfer T): Bson = Filters.eq<T>(path(), value)

/**
 * Creates a filter that matches all documents where the value of the field name does not equal the specified value.
 *
 * @param value     the value
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/ne $ne
 */
infix fun <T> KProperty<T>.ne(value: @NoInfer T): Bson = Filters.ne<T>(path(), value)

/**
 * Creates a filter that matches all documents where the value of the given property is less than the specified value.
 *
 * @param value     the value
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/lt $lt
 */
infix fun <T> KProperty<T>.lt(item: T): Bson = Filters.lt(path(), item)

/**
 * Creates a filter that matches all documents where the value of the given property is greater than the specified value.
 *
 * @param value     the value
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/gt $gt
 */
infix fun <T> KProperty<T>.gt(value: T): Bson = Filters.gt(path(), value)

/**
 * Creates a filter that matches all documents where the value of the given property is less than or equal to the specified value.
 *
 * @param value     the value
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/lte $lte
 */
infix fun <T> KProperty<T>.lte(value: T): Bson = Filters.lte(path(), value)

/**
 * Creates a filter that matches all documents where the value of the given property is greater than or equal to the specified value.
 *
 * @param value     the value
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/gte $gte
 */
infix fun <T> KProperty<T>.gte(value: T): Bson = Filters.gte(path(), value)

/**
 * Creates a filter that matches all documents where the value of a property equals any value in the list of specified values.
 *
 * @param values    the list of values
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/in $in
 */
infix fun <T> KProperty<T>.`in`(values: Iterable<T>): Bson = Filters.`in`(path(), values)

/**
 * Creates a filter that matches all documents where the value of a property equals any value in the list of specified values.
 *
 * @param values    the list of values
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/in $in
 */
infix fun <T> KProperty<T>.contains(values: Iterable<T>): Bson = `in`(values)

/**
 * Creates a filter that matches all documents where the value of a property does not equal any of the specified values or does not exist.
 *
 * @param values    the list of values
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/nin $nin
 */
infix fun <T> KProperty<T>.nin(values: Iterable<T>): Bson = Filters.nin(path(), values)

/**
 * Creates a filter that performs a logical AND of the provided list of filters.  Note that this will only generate a "$and"
 * operator if absolutely necessary, as the query language implicity ands together all the keys.  In other words, a query expression
 * like:
 *
 * <blockquote><pre>
 * and(eq("x", 1), lt("y", 3))
</pre></blockquote> *
 *
 * will generate a MongoDB query like:
 * <blockquote><pre>
 * {x : 1, y : {$lt : 3}}
</pre></blockquote> *
 *
 * @param filters the list of filters to and together
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/and $and
 */
fun and(filters: Iterable<Bson>): Bson = Filters.and(filters)

/**
 * Creates a filter that performs a logical AND of the provided list of filters.  Note that this will only generate a "$and"
 * operator if absolutely necessary, as the query language implicity ands together all the keys.  In other words, a query expression
 * like:
 *
 * <blockquote><pre>
 * and(eq("x", 1), lt("y", 3))
</pre></blockquote> *
 *
 * will generate a MongoDB query like:
 *
 * <blockquote><pre>
 * {x : 1, y : {$lt : 3}}
</pre></blockquote> *
 *
 * @param filters the list of filters to and together
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/and $and
 */
fun and(vararg filters: Bson?): Bson = Filters.and(*filters.filterNotNull().toTypedArray())

/**
 * Creates a filter that preforms a logical OR of the provided list of filters.
 *
 * @param filters the list of filters to and together
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/or $or
 */
fun or(filters: Iterable<Bson>): Bson = Filters.or(filters)

/**
 * Creates a filter that preforms a logical OR of the provided list of filters.
 *
 * @param filters the list of filters to and together
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/or $or
 */
fun or(vararg filters: Bson?): Bson = Filters.or(*filters.filterNotNull().toTypedArray())

/**
 * Creates a filter that matches all documents that do not match the passed in filter.
 * Requires the field name to passed as part of the value passed in and lifts it to create a valid "$not" query:
 *
 * <blockquote><pre>
 *    not(eq("x", 1))
 * </pre></blockquote>
 *
 * will generate a MongoDB query like:
 * <blockquote><pre>
 *    {x : $not: {$eq : 1}}
 * </pre></blockquote>
 *
 * @param filter the value
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/not $not
 */
fun not(filter: Bson): Bson = Filters.not(filter)

/**
 * Creates a filter that performs a logical NOR operation on all the specified filters.
 *
 * @param filters the list of values
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/nor $nor
 */
fun nor(vararg filters: Bson): Bson = Filters.nor(*filters)

/**
 * Creates a filter that performs a logical NOR operation on all the specified filters.
 *
 * @param filters the list of values
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/nor $nor
 */
fun nor(filters: Iterable<Bson>): Bson = Filters.nor(filters)

/**
 * Creates a filter that matches all documents that contain the given property.
 *
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/exists $exists
 */
fun <T> KProperty<T>.exists(): Bson = Filters.exists(path())

/**
 * Creates a filter that matches all documents that either contain or do not contain the given property, depending on the value of the
 * exists parameter.
 *
 * @param exists    true to check for existence, false to check for absence
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/exists $exists
 */
infix fun <T> KProperty<T>.exists(exists: Boolean): Bson = Filters.exists(path(), exists)

/**
 * Creates a filter that matches all documents where the value of the property is of the specified BSON type.
 *
 * @param type      the BSON type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/type $type
 */
infix fun <T> KProperty<T>.type(type: BsonType): Bson = Filters.type(path(), type)

/**
 * Creates a filter that matches all documents where the value of a property divided by a divisor has the specified remainder (i.e. perform
 * a modulo operation to select documents).
 *
 * @param divisor   the modulus
 * @param remainder the remainder
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/mod $mod
 */
fun <T> KProperty<T>.mod(divisor: Long, remainder: Long): Bson = Filters.mod(path(), divisor, remainder)

/**
 * Creates a filter that matches all documents where the value of the property matches the given regular expression pattern.
 *
 * @param pattern   the pattern
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/regex $regex
 */
infix fun KProperty<String?>.regex(regex: String): Bson = Filters.regex(path(), regex)

/**
 * Creates a filter that matches all documents where the value of the property matches the given regular expression pattern.
 *
 * @param pattern   the pattern
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/regex $regex
 */
infix fun KProperty<String?>.regex(regex: Pattern): Bson = Filters.regex(path(), regex)

/**
 * Creates a filter that matches all documents where the value of the option matches the given regular expression pattern with the given
 * options applied.
 *
 * @param pattern   the pattern
 * @param options   the options
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/regex $regex
 */
fun KProperty<String?>.regex(pattern: String, options: String): Bson = Filters.regex(path(), pattern, options)

/**
 * Creates a filter that matches all documents where the value of the property matches the given regular expression pattern.
 *
 * @param regex   the regex
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/regex $regex
 */
infix fun KProperty<String?>.regex(regex: Regex): Bson = Filters.regex(path(), regex.toPattern())

/**
 * Creates a filter that matches all documents matching the given the search term with the given text search options.
 *
 * @param search the search term
 * @param textSearchOptions the text search options to use
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/text $text
 */
fun text(search: String, textSearchOptions: TextSearchOptions = TextSearchOptions()): Bson =
    Filters.text(search, textSearchOptions)

/**
 * Creates a filter that matches all documents for which the given expression is true.
 *
 * @param javaScriptExpression the JavaScript expression
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/where $where
 */
fun where(javaScriptExpression: String): Bson = Filters.where(javaScriptExpression)

/**
 * Creates a filter that matches all documents that validate against the given JSON schema document.
 *
 * @param expression the aggregation expression
 * @param <TExpression> the expression type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/expr/ $expr
 */
fun <TExpression> expr(expression: TExpression): Bson = Filters.expr(expression)

/**
 * Creates a filter that matches all documents where the value of a property is an array that contains all the specified values.
 *
 * @param values    the list of values
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/all $all
 */
infix fun <T> KProperty<T>.all(values: Iterable<T>): Bson = Filters.all(path(), values)

/**
 * Creates a filter that matches all documents where the value of a property is an array that contains all the specified values.
 *
 * @param values    the list of values
 * @param <TItem>   the value type
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/all $all
 */
fun <T> KProperty<T>.all(vararg values: T): Bson = Filters.all(path(), *values)

/**
 * Creates a filter that matches all documents containing a property that is an array where at least one member of the array matches the
 * given filter.
 *
 * @param filter    the filter to apply to each element
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/elemMatch $elemMatch
 */
infix fun <T> KProperty<Collection<T>>.elemMatch(filter: Bson): Bson = Filters.elemMatch(path(), filter)

/**
 * Creates a filter that matches all documents where the value of a property is an array of the specified size.
 *
 * @param size      the size of the array
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/size $size
 */
infix fun <T> KProperty<T>.size(size: Int): Bson = Filters.size(path(), size)

/**
 * Creates a filter that matches all documents where all of the bit positions are clear in the property.
 *
 * @param fieldName the field name
 * @param bitmask   the bitmask
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/bitsAllClear $bitsAllClear
 */
infix fun <T> KProperty<T>.bitsAllClear(bitmask: Long): Bson = Filters.bitsAllClear(path(), bitmask)

/**
 * Creates a filter that matches all documents where all of the bit positions are set in the property.
 *
 * @param bitmask   the bitmask
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/bitsAllSet $bitsAllSet
 */
infix fun <T> KProperty<T>.bitsAllSet(bitmask: Long): Bson = Filters.bitsAllSet(path(), bitmask)

/**
 * Creates a filter that matches all documents where any of the bit positions are clear in the property.
 *
 * @param fieldName the field name
 * @param bitmask   the bitmask
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/bitsAllClear $bitsAllClear
 */
infix fun <T> KProperty<T>.bitsAnyClear(bitmask: Long): Bson = Filters.bitsAnyClear(path(), bitmask)

/**
 * Creates a filter that matches all documents where any of the bit positions are set in the property.
 *
 * @param bitmask   the bitmask
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/bitsAnySet $bitsAnySet
 */
infix fun <T> KProperty<T>.bitsAnySet(bitmask: Long): Bson = Filters.bitsAnySet(path(), bitmask)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that exists entirely within the specified shape.
 *
 * @param geometry  the bounding GeoJSON geometry object
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/geoWithin/ $geoWithin
 */
infix fun <T> KProperty<T>.geoWithin(geometry: Geometry): Bson = Filters.geoWithin(path(), geometry)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that exists entirely within the specified shape.
 *
 * @param geometry  the bounding GeoJSON geometry object
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/geoWithin/ $geoWithin
 */
infix fun <T> KProperty<T>.geoWithin(geometry: Bson): Bson = Filters.geoWithin(path(), geometry)

/**
 * Creates a filter that matches all documents containing a property with grid coordinates data that exist entirely within the specified
 * box.
 *
 * @param fieldName   the field name
 * @param lowerLeftX  the lower left x coordinate of the box
 * @param lowerLeftY  the lower left y coordinate of the box
 * @param upperRightX the upper left x coordinate of the box
 * @param upperRightY the upper left y coordinate of the box
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/geoWithin/ $geoWithin
 * @mongodb.driver.manual reference/operator/query/box/#op._S_box $box
 */
fun <T> KProperty<T>.geoWithinBox(
    lowerLeftX: Double,
    lowerLeftY: Double,
    upperRightX: Double,
    upperRightY: Double
): Bson = Filters.geoWithinBox(path(), lowerLeftX, lowerLeftY, upperRightX, upperRightY)

/**
 * Creates a filter that matches all documents containing a property with grid coordinates data that exist entirely within the specified
 * polygon.
 *
 * @param points    a list of pairs of x, y coordinates.  Any extra dimensions are ignored
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/geoWithin/ $geoWithin
 * @mongodb.driver.manual reference/operator/query/polygon/#op._S_polygon $polygon
 */
infix fun <T> KProperty<T>.geoWithinPolygon(points: List<List<Double>>): Bson = Filters.geoWithinPolygon(path(), points)

/**
 * Creates a filter that matches all documents containing a property with grid coordinates data that exist entirely within the specified
 * circle.
 *
 * @param x         the x coordinate of the circle
 * @param y         the y coordinate of the circle
 * @param radius    the radius of the circle, as measured in the units used by the coordinate system
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/geoWithin/ $geoWithin
 * @mongodb.driver.manual reference/operator/query/center/#op._S_center $center
 */
fun <T> KProperty<T>.geoWithinCenter(x: Double, y: Double, radius: Double): Bson =
    Filters.geoWithinCenter(path(), x, y, radius)

/**
 * Creates a filter that matches all documents containing a property with geospatial data (GeoJSON or legacy coordinate pairs) that exist
 * entirely within the specified circle, using spherical geometry.  If using longitude and latitude, specify longitude first.
 *
 * @param x         the x coordinate of the circle
 * @param y         the y coordinate of the circle
 * @param radius    the radius of the circle, in radians
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/geoWithin/ $geoWithin
 * @mongodb.driver.manual reference/operator/query/centerSphere/#op._S_centerSphere $centerSphere
 */
fun <T> KProperty<T>.geoWithinCenterSphere(x: Double, y: Double, radius: Double): Bson =
    Filters.geoWithinCenterSphere(path(), x, y, radius)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that intersects with the specified shape.
 *
 * @param geometry  the bounding GeoJSON geometry object
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/geoIntersects/ $geoIntersects
 */
infix fun <T> KProperty<T>.geoIntersects(geometry: Geometry): Bson = Filters.geoIntersects(path(), geometry)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that intersects with the specified shape.
 *
 * @param geometry  the bounding GeoJSON geometry object
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/geoIntersects/ $geoIntersects
 */
infix fun <T> KProperty<T>.geoIntersects(geometry: Bson): Bson = Filters.geoIntersects(path(), geometry)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that is near the specified GeoJSON point.
 *
 * @param geometry    the bounding GeoJSON geometry object
 * @param maxDistance the maximum distance from the point, in meters
 * @param minDistance the minimum distance from the point, in meters
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/near/ $near
 */
fun <T> KProperty<T>.near(geometry: Point, maxDistance: Double, minDistance: Double): Bson =
    Filters.near(path(), geometry, maxDistance, minDistance)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that is near the specified GeoJSON point.
 *
 * @param geometry    the bounding GeoJSON geometry object
 * @param maxDistance the maximum distance from the point, in meters
 * @param minDistance the minimum distance from the point, in meters
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/near/ $near
 */
fun <T> KProperty<T>.near(geometry: Bson, maxDistance: Double, minDistance: Double): Bson =
    Filters.near(path(), geometry, maxDistance, minDistance)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that is near the specified point.
 *
 * @param x           the x coordinate
 * @param y           the y coordinate
 * @param maxDistance the maximum distance from the point, in radians
 * @param minDistance the minimum distance from the point, in radians
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/near/ $near
 */
fun <T> KProperty<T>.near(x: Double, y: Double, maxDistance: Double, minDistance: Double): Bson =
    Filters.near(path(), x, y, maxDistance, minDistance)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that is near the specified GeoJSON point using
 * spherical geometry.
 *
 * @param geometry    the bounding GeoJSON geometry object
 * @param maxDistance the maximum distance from the point, in meters
 * @param minDistance the minimum distance from the point, in meters
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/near/ $near
 */
fun <T> KProperty<T>.nearSphere(geometry: Bson, maxDistance: Double, minDistance: Double): Bson =
    Filters.nearSphere(path(), geometry, maxDistance, minDistance)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that is near the specified GeoJSON point using
 * spherical geometry.
 *
 * @param geometry    the bounding GeoJSON geometry object
 * @param maxDistance the maximum distance from the point, in meters
 * @param minDistance the minimum distance from the point, in meters
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/near/ $near
 */
fun <T> KProperty<T>.nearSphere(geometry: Point, maxDistance: Double, minDistance: Double): Bson =
    Filters.nearSphere(path(), geometry, maxDistance, minDistance)

/**
 * Creates a filter that matches all documents containing a property with geospatial data that is near the specified point using
 * spherical geometry.
 *
 * @param x           the x coordinate
 * @param y           the y coordinate
 * @param maxDistance the maximum distance from the point, in radians
 * @param minDistance the minimum distance from the point, in radians
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/near/ $near
 */
fun <T> KProperty<T>.nearSphere(x: Double, y: Double, maxDistance: Double, minDistance: Double): Bson =
    Filters.nearSphere(path(), x, y, maxDistance, minDistance)

/**
 * Creates a filter that matches all documents that validate against the given JSON schema document.
 *
 * @param schema the JSON schema to validate against
 * @return the filter
 * @mongodb.driver.manual reference/operator/query/jsonSchema/ $jsonSchema
 */
fun jsonSchema(schema: Bson): Bson = Filters.jsonSchema(schema)