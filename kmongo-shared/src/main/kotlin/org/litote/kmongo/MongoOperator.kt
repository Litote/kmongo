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

/**
 * List all known mongo operators, used in string templates.
 */
enum class MongoOperator {

    //*******
    //Query and Projection Operators ( https://docs.mongodb.org/manual/reference/operator/query/ )
    //*******

    //Comparison
    eq,
    gt,
    gte,
    lt,
    lte,
    ne,
    `in`,
    nin,
    //Logical
    or,
    and,
    not,
    nor,
    //Element
    exists,
    type,
    //Evaluation
    mod,
    regex,
    text,
    where,
    //Geospatial
    geoWithin,
    geoIntersects,
    near,
    nearSphere,
    //Array
    all,
    elemMatch,
    size,
    //Bitwise
    bitsAllSet,
    bitsAnySet,
    bitsAllClear,
    bitsAnyClear,
    //Comments
    comment,
    //Projection
    `$` {
        override fun toString(): String{
            return "\$"
        }
    },
    /*elemMatch,*/
    meta,
    slice,

    //*******
    //Query modifiers Operators ( https://docs.mongodb.org/v3.0/reference/operator/query-modifier/ )
    //*******

    /*comment,*/
    explain,
    hint,
    maxScan,
    maxTimeMS,
    max,
    min,
    orderby,
    returnKey,
    showDiskLoc,
    snapshot,
    query,
    natural,

    //*******
    //Update Operators ( https://docs.mongodb.org/manual/reference/operator/update/ )
    //*******

    //Field update
    inc,
    mul,
    rename,
    setOnInsert,
    set,
    unset,
    /* min, max,*/
    currentDate,
    //Array
    /*$*/
    addToSet,
    pop,
    pullAll,
    pull, pushAll,
    push,
    //Modifiers
    each,
    /*slice,*/
    sort,
    position,
    //Bitwise
    bit,
    //Isolation
    isolated,

    //*******
    //Aggregation Pipeline Operators ( https://docs.mongodb.org/manual/reference/operator/aggregation/ )
    //*******

    //Stage
    project,
    match,
    redact,
    limit,
    skip,
    unwind,
    group,
    sample,
    /*sort,*/
    geoNear,
    lookup,
    out,
    indexStats,
    //Set expressions
    setEquals,
    setIntersection,
    setUnion,
    setDifference,
    setIsSubset,
    anyElementTrue,
    allElementsTrue,
    //Comparison
    cmp,
    /*eq, gt, gte, lt, lte,ne*/
    //Arithmetic
    abs,
    add,
    ceil,
    divide,
    exp,
    floor,
    ln,
    log,
    log10,
    /*mod,*/
    multiply,
    pow,
    sqrt,
    subtract,
    trunc,
    //String
    concat,
    substr,
    toLower,
    toUpper,
    strcasecmp,
    //Text Search
    /*meta,*/
    //Array
    arrayElemAt,
    concatArrays,
    filter,
    isArray,
    /*size,slice,*/
    //Variable
    map,
    let,
    //Literal
    literal,
    //Date
    dayOfYear,
    dayOfMonth,
    dayOfWeek,
    year,
    month,
    week,
    hour,
    minute,
    second,
    millisecond,
    dateToString,
    //Conditional
    cond,
    ifNull,
    //Accumulators
    sum,
    avg,
    first,
    last,
    /*max,min,push,addToSet,*/
    stdDevPop,
    stdDevSamp,

    //*******
    //MongoDB Extended JSON ( https://docs.mongodb.org/manual/reference/mongodb-extended-json/ )
    //*******

    binary,
    /*type,*/
    date,
    numberLong,
    timestamp,
    /*regex,*/
    options,
    oid,
    ref,
    id,
    undefined,
    minKey,
    maxKey;

/*
 * #%L
 * kmongo-framework
 * %%
 * Copyright (C) 2016 Litote
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

    override fun toString(): String{
        return "\$$name"
    }

}
