package org.litote.kmongo.model

import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class InternalDataClass_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, InternalDataClass?>) : KPropertyPath<T, InternalDataClass?>(previous,property) {
    val l: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,InternalDataClass::l)
    companion object {
        val L: KProperty1<InternalDataClass, Long?>
            get() = InternalDataClass::l}
}

internal class InternalDataClass_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<InternalDataClass>?>) : KCollectionPropertyPath<T, InternalDataClass?, InternalDataClass_<T>>(previous,property) {
    val l: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,InternalDataClass::l)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): InternalDataClass_<T> = InternalDataClass_(this, customProperty(this, additionalPath))}
