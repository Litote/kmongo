package org.litote.kmongo.model

import kotlin.Int
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class SubData2_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, SubData2?>) : NotAnnotatedData_<T>(previous,property) {
    val a1: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SubData2::a1)
    companion object {
        val A1: KProperty1<SubData2, Int?>
            get() = SubData2::a1}
}

class SubData2_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<SubData2>?>) : NotAnnotatedData_Col<T>(previous,property) {
    val a1: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SubData2::a1)
}
