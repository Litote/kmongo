package org.litote.kmongo.model

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class SubData2_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, SubData2?>) :
        NotAnnotatedData_<T>(previous,property) {
    val a1: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,SubData2::a1)

    companion object {
        val A1: KProperty1<SubData2, Int?>
            get() = SubData2::a1}
}

class SubData2_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<SubData2>?>) : NotAnnotatedData_Col<T>(previous,property) {
    val a1: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,SubData2::a1)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SubData2_<T> = SubData2_(this,
            customProperty(this, additionalPath))}

class SubData2_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, SubData2>?>)
        : NotAnnotatedData_Map<T, K>(previous,property) {
    val a1: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,SubData2::a1)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SubData2_<T> = SubData2_(this,
            customProperty(this, additionalPath))}
