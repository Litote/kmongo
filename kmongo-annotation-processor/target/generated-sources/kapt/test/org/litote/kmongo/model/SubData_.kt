package org.litote.kmongo.model

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class SubData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, SubData?>) :
        TestData_<T>(previous,property) {
    val s: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,org.litote.kreflect.findProperty<SubData,String?>("s"))

    companion object {
        val S: KProperty1<SubData, String?>
            get() = org.litote.kreflect.findProperty<SubData,String?>("s")}
}

class SubData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<SubData>?>)
        : TestData_Col<T>(previous,property) {
    val s: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,org.litote.kreflect.findProperty<SubData,String?>("s"))

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SubData_<T> = SubData_(this,
            customProperty(this, additionalPath))}

class SubData_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, SubData>?>) :
        TestData_Map<T, K>(previous,property) {
    val s: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,org.litote.kreflect.findProperty<SubData,String?>("s"))

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SubData_<T> = SubData_(this,
            customProperty(this, additionalPath))}
