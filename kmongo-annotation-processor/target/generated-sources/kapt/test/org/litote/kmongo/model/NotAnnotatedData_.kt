package org.litote.kmongo.model

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Test: KProperty1<NotAnnotatedData, String?>
    get() = NotAnnotatedData::test
open class NotAnnotatedData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        NotAnnotatedData?>) : KPropertyPath<T, NotAnnotatedData?>(previous,property) {
    val test: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Test)

    companion object {
        val Test: KProperty1<NotAnnotatedData, String?>
            get() = __Test}
}

open class NotAnnotatedData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<NotAnnotatedData>?>) : KCollectionPropertyPath<T, NotAnnotatedData?,
        NotAnnotatedData_<T>>(previous,property) {
    val test: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Test)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NotAnnotatedData_<T> =
            NotAnnotatedData_(this, customProperty(this, additionalPath))}

open class NotAnnotatedData_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        NotAnnotatedData>?>) : KMapPropertyPath<T, K, NotAnnotatedData?,
        NotAnnotatedData_<T>>(previous,property) {
    val test: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Test)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NotAnnotatedData_<T> =
            NotAnnotatedData_(this, customProperty(this, additionalPath))}
