package org.litote.kmongo.model

import kotlin.Double
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Price: KProperty1<SimpleReferenced2Data, Double?>
    get() = SimpleReferenced2Data::price
class SimpleReferenced2Data_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        SimpleReferenced2Data?>) : KPropertyPath<T, SimpleReferenced2Data?>(previous,property) {
    val price: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__Price)

    companion object {
        val Price: KProperty1<SimpleReferenced2Data, Double?>
            get() = __Price}
}

class SimpleReferenced2Data_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<SimpleReferenced2Data>?>) : KCollectionPropertyPath<T, SimpleReferenced2Data?,
        SimpleReferenced2Data_<T>>(previous,property) {
    val price: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__Price)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SimpleReferenced2Data_<T> =
            SimpleReferenced2Data_(this, customProperty(this, additionalPath))}

class SimpleReferenced2Data_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        SimpleReferenced2Data>?>) : KMapPropertyPath<T, K, SimpleReferenced2Data?,
        SimpleReferenced2Data_<T>>(previous,property) {
    val price: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__Price)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SimpleReferenced2Data_<T> =
            SimpleReferenced2Data_(this, customProperty(this, additionalPath))}
