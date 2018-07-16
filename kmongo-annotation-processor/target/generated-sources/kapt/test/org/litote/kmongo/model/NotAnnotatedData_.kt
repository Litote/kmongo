package org.litote.kmongo.model

import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

open class NotAnnotatedData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, NotAnnotatedData?>) : KPropertyPath<T, NotAnnotatedData?>(previous,property) {
    val test: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NotAnnotatedData::test)
    companion object {
        val Test: KProperty1<NotAnnotatedData, String?>
            get() = NotAnnotatedData::test}
}

open class NotAnnotatedData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<NotAnnotatedData>?>) : KCollectionPropertyPath<T, NotAnnotatedData?>(previous,property) {
    val test: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NotAnnotatedData::test)
}
