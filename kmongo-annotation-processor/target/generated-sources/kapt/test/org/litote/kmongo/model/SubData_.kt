package org.litote.kmongo.model

import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class SubData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, SubData?>) : TestData_<T>(previous,property) {
    val s: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,org.litote.kmongo.property.findProperty<SubData,String?>("s"))
    companion object {
        val S: KProperty1<SubData, String?>
            get() = org.litote.kmongo.property.findProperty<SubData,String?>("s")}
}

class SubData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<SubData>?>) : TestData_Col<T>(previous,property) {
    val s: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,org.litote.kmongo.property.findProperty<SubData,String?>("s"))
}
