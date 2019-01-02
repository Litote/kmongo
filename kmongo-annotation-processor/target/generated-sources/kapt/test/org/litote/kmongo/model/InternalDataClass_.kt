package org.litote.kmongo.model

import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __L: KProperty1<InternalDataClass, Long?>
    get() = InternalDataClass::l
internal class InternalDataClass_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        InternalDataClass?>) : KPropertyPath<T, InternalDataClass?>(previous,property) {
    val l: KPropertyPath<T, Long?>
        get() = KPropertyPath<T, Long?>(this,__L)

    companion object {
        val L: KProperty1<InternalDataClass, Long?>
            get() = __L}
}

internal class InternalDataClass_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<InternalDataClass>?>) : KCollectionPropertyPath<T, InternalDataClass?,
        InternalDataClass_<T>>(previous,property) {
    val l: KPropertyPath<T, Long?>
        get() = KPropertyPath<T, Long?>(this,__L)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): InternalDataClass_<T> =
            InternalDataClass_(this, customProperty(this, additionalPath))}

internal class InternalDataClass_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, InternalDataClass>?>) : KMapPropertyPath<T, K, InternalDataClass?,
        InternalDataClass_<T>>(previous,property) {
    val l: KPropertyPath<T, Long?>
        get() = KPropertyPath<T, Long?>(this,__L)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): InternalDataClass_<T> =
            InternalDataClass_(this, customProperty(this, additionalPath))}
