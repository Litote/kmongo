package org.litote.kmongo.issue

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

open class Issue130ParentGeneric_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Issue130ParentGeneric<*>?>) : KPropertyPath<T, Issue130ParentGeneric<*>?>(previous,property)
        {
    companion object
}

open class Issue130ParentGeneric_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<Issue130ParentGeneric<*>>?>) : KCollectionPropertyPath<T,
        Issue130ParentGeneric<*>?, Issue130ParentGeneric_<T>>(previous,property) {
    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Issue130ParentGeneric_<T> =
            Issue130ParentGeneric_(this, customProperty(this, additionalPath))}

open class Issue130ParentGeneric_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, Issue130ParentGeneric<*>>?>) : KMapPropertyPath<T, K, Issue130ParentGeneric<*>?,
        Issue130ParentGeneric_<T>>(previous,property) {
    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Issue130ParentGeneric_<T> =
            Issue130ParentGeneric_(this, customProperty(this, additionalPath))}
